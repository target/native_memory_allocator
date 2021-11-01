package com.target.oss.nativememoryallocator.integrationtests

import com.google.common.testing.FakeTicker
import com.target.oss.nativememoryallocator.allocator.NativeMemoryAllocatorBuilder
import com.target.oss.nativememoryallocator.map.NativeMemoryMap
import com.target.oss.nativememoryallocator.map.NativeMemoryMapBackend
import com.target.oss.nativememoryallocator.map.NativeMemoryMapBuilder
import io.mockk.clearAllMocks
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
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

    @BeforeEach
    fun before() {
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
        assertEquals(NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER, putResult1)

        val putResult2 = nativeMemoryMap.put(2, TestCacheValue("2345"))
        assertEquals(NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER, putResult2)

        // await asynchronous eviction due to max size
        awaitCondition { nativeMemoryMap.get(1) == null }
        awaitCondition { nativeMemoryMap.size == 1 }
        awaitCondition { nativeMemoryMap.stats.caffeineStats?.evictionCount() == 1L }

        assertEquals(1, nativeMemoryMap.size)
        assertEquals(null, nativeMemoryMap.get(1))
        assertEquals(TestCacheValue("2345"), nativeMemoryMap.get(2))
        assertEquals(1L, nativeMemoryMap.stats.caffeineStats?.evictionCount())

        // advance fakeTicker by 6 seconds to trigger expireAfterAccess TTL
        fakeTicker.advance(6, TimeUnit.SECONDS)

        // await asynchronous eviction due to expireAfterAccess TTL
        awaitCondition { nativeMemoryMap.get(2) == null }
        awaitCondition { nativeMemoryMap.size == 0 }
        awaitCondition { nativeMemoryMap.stats.caffeineStats?.evictionCount() == 2L }

        assertEquals(0, nativeMemoryMap.size)
        assertEquals(null, nativeMemoryMap.get(1))
        assertEquals(null, nativeMemoryMap.get(2))
        assertEquals(2L, nativeMemoryMap.stats.caffeineStats?.evictionCount())

        val operationCounters = nativeMemoryMap.operationCounters
        assertNull(operationCounters)
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
        assertEquals(NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER, putResult1)

        val putResult2 = nativeMemoryMap.put(2, TestCacheValue("2345"))
        assertEquals(NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER, putResult2)

        // await asynchronous eviction due to max size
        awaitCondition { nativeMemoryMap.get(1) == null }
        awaitCondition { nativeMemoryMap.size == 1 }
        awaitCondition { nativeMemoryMap.stats.caffeineStats?.evictionCount() == 1L }

        assertEquals(1, nativeMemoryMap.size)
        assertEquals(null, nativeMemoryMap.get(1))
        assertEquals(TestCacheValue("2345"), nativeMemoryMap.get(2))
        assertEquals(1L, nativeMemoryMap.stats.caffeineStats?.evictionCount())

        // advance fakeTicker by 6 seconds to trigger expireAfterAccess TTL
        fakeTicker.advance(6, TimeUnit.SECONDS)

        // await asynchronous eviction due to expireAfterAccess TTL
        awaitCondition { nativeMemoryMap.get(2) == null }
        awaitCondition { nativeMemoryMap.size == 0 }
        awaitCondition { nativeMemoryMap.stats.caffeineStats?.evictionCount() == 2L }

        assertEquals(0, nativeMemoryMap.size)
        assertEquals(null, nativeMemoryMap.get(1))
        assertEquals(null, nativeMemoryMap.get(2))
        assertFalse(nativeMemoryMap.delete(1))
        assertFalse(nativeMemoryMap.delete(2))
        assertEquals(2L, nativeMemoryMap.stats.caffeineStats?.evictionCount())

        val operationCounters = nativeMemoryMap.operationCounters
        assertNotNull(operationCounters)
        assertEquals(0, operationCounters?.numPutsNoChanges?.get())
        assertEquals(0, operationCounters?.numPutsFreedBuffer?.get())
        assertEquals(0, operationCounters?.numPutsReusedBuffer?.get())
        assertEquals(2, operationCounters?.numPutsNewBuffer?.get())
        assertEquals(0, operationCounters?.numDeletesFreedBuffer?.get())
        assertEquals(2, operationCounters?.numDeletesNoChange?.get())
        // exact values are not known due to awaitCondition
        assertTrue((operationCounters?.numGetsNullValue?.get() ?: 0) >= 5)
        assertTrue((operationCounters?.numGetsNonNullValue?.get() ?: 0) >= 1)
    }
}