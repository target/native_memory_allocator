package com.target.integrationtests

import com.google.common.testing.FakeTicker
import com.target.nativememoryallocator.allocator.NativeMemoryAllocatorBuilder
import com.target.nativememoryallocator.map.NativeMemoryMap
import com.target.nativememoryallocator.map.NativeMemoryMapBackend
import com.target.nativememoryallocator.map.NativeMemoryMapBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

private fun awaitCondition(
    delayTimeMS: Long = 100,
    maxTries: Int = 10,
    testCondition: () -> Boolean,
) {
    for (i in 0 until maxTries) {
        val condition = testCondition()
        if (condition) {
            return
        } else {
            Thread.sleep(delayTimeMS)
        }
    }
    throw IllegalStateException("condition is false after $maxTries tries")
}

class CaffeineScenario {

    @AfterEach
    fun afterEach() {
        clearAllMocks()
    }

    @Test
    fun caffeineNoOperationCountersScenarioTest() {
        logger.info { "begin caffeineNoOperationCountersScenarioTest" }

        val nativeMemoryAllocator = NativeMemoryAllocatorBuilder(
            pageSizeBytes = 4_096,
            nativeMemorySizeBytes = (1L * 1024L * 1024L * 1024L),//1gb
        ).build()

        val fakeTicker = FakeTicker()

        val nativeMemoryMap = NativeMemoryMapBuilder<Int, TestCacheValue>(
            valueSerializer = TestCacheValueSerializer(),
            nativeMemoryAllocator = nativeMemoryAllocator,
            backend = NativeMemoryMapBackend.CAFFEINE,
            caffeineConfigFunction = { caffeine ->
                caffeine
                    .expireAfterAccess(5, TimeUnit.SECONDS)
                    .maximumSize(1)
                    .recordStats()
                    .ticker(fakeTicker::read)
            }
        ).build()

        val putResult1 = nativeMemoryMap.put(1, TestCacheValue("1234"))
        putResult1 shouldBe NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER

        val putResult2 = nativeMemoryMap.put(2, TestCacheValue("2345"))
        putResult2 shouldBe NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER

        // await asynchronous eviction due to max size
        awaitCondition { nativeMemoryMap.get(1) == null }
        awaitCondition { nativeMemoryMap.size == 1 }
        awaitCondition { nativeMemoryMap.stats.caffeineStats?.evictionCount() == 1L }

        nativeMemoryMap.size shouldBe 1
        nativeMemoryMap.get(1) shouldBe null
        nativeMemoryMap.get(2) shouldBe TestCacheValue("2345")
        nativeMemoryMap.stats.caffeineStats?.evictionCount() shouldBe 1L

        // advance fakeTicker by 6 seconds to trigger expireAfterAccess TTL
        fakeTicker.advance(6, TimeUnit.SECONDS)

        // await asynchronous eviction due to expireAfterAccess TTL
        awaitCondition { nativeMemoryMap.get(2) == null }
        awaitCondition { nativeMemoryMap.size == 0 }
        awaitCondition { nativeMemoryMap.stats.caffeineStats?.evictionCount() == 2L }

        nativeMemoryMap.size shouldBe 0
        nativeMemoryMap.get(1) shouldBe null
        nativeMemoryMap.get(2) shouldBe null
        nativeMemoryMap.stats.caffeineStats?.evictionCount() shouldBe 2L

        nativeMemoryMap.operationCounters shouldBe null
    }

    @Test
    fun caffeineScenarioTest() {
        logger.info { "begin caffeineScenarioTest" }

        val nativeMemoryAllocator = NativeMemoryAllocatorBuilder(
            pageSizeBytes = 4_096,
            nativeMemorySizeBytes = (1L * 1024L * 1024L * 1024L),//1gb
        ).build()

        val fakeTicker = FakeTicker()

        val nativeMemoryMap = NativeMemoryMapBuilder<Int, TestCacheValue>(
            valueSerializer = TestCacheValueSerializer(),
            nativeMemoryAllocator = nativeMemoryAllocator,
            enableOperationCounters = true,
            backend = NativeMemoryMapBackend.CAFFEINE,
            caffeineConfigFunction = { caffeine ->
                caffeine
                    .expireAfterAccess(5, TimeUnit.SECONDS)
                    .maximumSize(1)
                    .recordStats()
                    .ticker(fakeTicker::read)
            }
        ).build()

        val putResult1 = nativeMemoryMap.put(1, TestCacheValue("1234"))
        putResult1 shouldBe NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER

        val putResult2 = nativeMemoryMap.put(2, TestCacheValue("2345"))
        putResult2 shouldBe NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER

        // await asynchronous eviction due to max size
        awaitCondition { nativeMemoryMap.get(1) == null }
        awaitCondition { nativeMemoryMap.size == 1 }
        awaitCondition { nativeMemoryMap.stats.caffeineStats?.evictionCount() == 1L }

        nativeMemoryMap.size shouldBe 1
        nativeMemoryMap.get(1) shouldBe null
        nativeMemoryMap.get(2) shouldBe TestCacheValue("2345")
        nativeMemoryMap.stats.caffeineStats?.evictionCount() shouldBe 1L

        // advance fakeTicker by 6 seconds to trigger expireAfterAccess TTL
        fakeTicker.advance(6, TimeUnit.SECONDS)

        // await asynchronous eviction due to expireAfterAccess TTL
        awaitCondition { nativeMemoryMap.get(2) == null }
        awaitCondition { nativeMemoryMap.size == 0 }
        awaitCondition { nativeMemoryMap.stats.caffeineStats?.evictionCount() == 2L }

        nativeMemoryMap.size shouldBe 0
        nativeMemoryMap.get(1) shouldBe null
        nativeMemoryMap.get(2) shouldBe null
        nativeMemoryMap.delete(1) shouldBe false
        nativeMemoryMap.delete(2) shouldBe false
        nativeMemoryMap.stats.caffeineStats?.evictionCount() shouldBe 2L

        val operationCounters = nativeMemoryMap.operationCounters
        operationCounters shouldNotBe null

        operationCounters?.numPutsNoChange?.toLong() shouldBe 0L
        operationCounters?.numPutsFreedBuffer?.toLong() shouldBe 0L
        operationCounters?.numPutsReusedBuffer?.toLong() shouldBe 0L
        operationCounters?.numPutsNewBuffer?.toLong() shouldBe 2L
        operationCounters?.numDeletesFreedBuffer?.toLong() shouldBe 0L
        operationCounters?.numDeletesNoChange?.toLong() shouldBe 2L
        // exact values are not known due to awaitCondition
        (operationCounters?.numGetsNullValue?.toLong() ?: 0) shouldBeGreaterThanOrEqual 5
        (operationCounters?.numGetsNonNullValue?.toLong() ?: 0) shouldBeGreaterThanOrEqual 1
    }
}