package com.target.nativememoryallocator.integrationtests

import com.target.nativememoryallocator.allocator.NativeMemoryAllocatorBuilder
import com.target.nativememoryallocator.map.NativeMemoryMap
import com.target.nativememoryallocator.map.NativeMemoryMapBackend
import com.target.nativememoryallocator.map.NativeMemoryMapBuilder
import com.target.nativememoryallocator.map.NativeMemoryMapStats
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

private val logger = KotlinLogging.logger {}

class ConcurrentHashMapScenario {

    @AfterEach
    fun afterEach() {
        clearAllMocks()
    }

    @Test
    fun concurrentHashMapNoOperationCountersScenarioTest() {
        logger.info { "begin concurrentHashMapNoOperationCountersScenarioTest" }

        val nativeMemoryAllocator = NativeMemoryAllocatorBuilder(
            pageSizeBytes = 4_096,
            nativeMemorySizeBytes = (1L * 1024L * 1024L * 1024L),//1gb
        ).build()

        val nativeMemoryMap = NativeMemoryMapBuilder<Int, TestCacheValue>(
            valueSerializer = TestCacheValueSerializer(),
            nativeMemoryAllocator = nativeMemoryAllocator,
            backend = NativeMemoryMapBackend.CONCURRENT_HASH_MAP,
        ).build()

        val putResult1 = nativeMemoryMap.put(1, TestCacheValue("1234"))
        putResult1 shouldBe NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER

        val putResult2 = nativeMemoryMap.put(2, TestCacheValue("2345"))
        putResult2 shouldBe NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER

        nativeMemoryMap.size shouldBe 2
        nativeMemoryMap.stats shouldBe NativeMemoryMapStats()

        logger.info { "nativeMemoryMap.get(1) = ${nativeMemoryMap.get(1)}" }
        logger.info { "nativeMemoryMap.get(2) = ${nativeMemoryMap.get(2)}" }

        nativeMemoryMap.get(1) shouldBe TestCacheValue("1234")
        nativeMemoryMap.get(2) shouldBe TestCacheValue("2345")

        nativeMemoryMap.delete(1)

        nativeMemoryMap.size shouldBe 1
        nativeMemoryMap.get(1) shouldBe null
        nativeMemoryMap.get(2) shouldBe TestCacheValue("2345")

        nativeMemoryMap.operationCounters shouldBe null
    }

    @Test
    fun concurrentHashMapScenarioTest() {
        logger.info { "begin concurrentHashMapScenarioTest" }

        val nativeMemoryAllocator = NativeMemoryAllocatorBuilder(
            pageSizeBytes = 4_096,
            nativeMemorySizeBytes = (1L * 1024L * 1024L * 1024L),//1gb
        ).build()

        val nativeMemoryMap = NativeMemoryMapBuilder<Int, TestCacheValue>(
            valueSerializer = TestCacheValueSerializer(),
            nativeMemoryAllocator = nativeMemoryAllocator,
            enableOperationCounters = true,
            backend = NativeMemoryMapBackend.CONCURRENT_HASH_MAP,
        ).build()

        val putResult1 = nativeMemoryMap.put(1, TestCacheValue("1234"))
        putResult1 shouldBe NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER

        val putResult2 = nativeMemoryMap.put(2, TestCacheValue("2345"))
        putResult2 shouldBe NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER

        nativeMemoryMap.size shouldBe 2
        nativeMemoryMap.stats shouldBe NativeMemoryMapStats()

        logger.info { "nativeMemoryMap.get(1) = ${nativeMemoryMap.get(1)}" }
        logger.info { "nativeMemoryMap.get(2) = ${nativeMemoryMap.get(2)}" }

        nativeMemoryMap.get(1) shouldBe TestCacheValue("1234")
        nativeMemoryMap.get(2) shouldBe TestCacheValue("2345")

        nativeMemoryMap.delete(1) shouldBe true

        nativeMemoryMap.size shouldBe 1
        nativeMemoryMap.get(1) shouldBe null
        nativeMemoryMap.get(2) shouldBe TestCacheValue("2345")

        val operationCounters = nativeMemoryMap.operationCounters
        operationCounters shouldNotBe null

        operationCounters?.numPutsNoChange?.toLong() shouldBe 0
        operationCounters?.numPutsFreedBuffer?.toLong() shouldBe 0
        operationCounters?.numPutsReusedBuffer?.toLong() shouldBe 0
        operationCounters?.numPutsNewBuffer?.toLong() shouldBe 2
        operationCounters?.numDeletesFreedBuffer?.toLong() shouldBe 1
        operationCounters?.numDeletesNoChange?.toLong() shouldBe 0
        operationCounters?.numGetsNonNullValue?.toLong() shouldBe 5
        operationCounters?.numGetsNullValue?.toLong() shouldBe 1
    }
}