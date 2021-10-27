package com.target.oss.nativememoryallocator.integrationtests

import com.target.oss.nativememoryallocator.allocator.NativeMemoryAllocatorBuilder
import com.target.oss.nativememoryallocator.map.NativeMemoryMap
import com.target.oss.nativememoryallocator.map.NativeMemoryMapBackend
import com.target.oss.nativememoryallocator.map.NativeMemoryMapBuilder
import com.target.oss.nativememoryallocator.map.NativeMemoryMapStats
import io.mockk.clearAllMocks
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private val logger = KotlinLogging.logger {}

class ConcurrentHashMapScenario {

    @BeforeEach
    fun before() {
        clearAllMocks()
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
            backend = NativeMemoryMapBackend.CONCURRENT_HASH_MAP,
        ).build()

        val putResult1 = nativeMemoryMap.put(1, TestCacheValue("1234"))
        assertEquals(NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER, putResult1)

        val putResult2 = nativeMemoryMap.put(2, TestCacheValue("2345"))
        assertEquals(NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER, putResult2)

        assertEquals(2, nativeMemoryMap.size)
        assertEquals(NativeMemoryMapStats(), nativeMemoryMap.stats)

        logger.info { "nativeMemoryMap.get(1) = ${nativeMemoryMap.get(1)}" }
        logger.info { "nativeMemoryMap.get(2) = ${nativeMemoryMap.get(2)}" }

        assertEquals(TestCacheValue("1234"), nativeMemoryMap.get(1))
        assertEquals(TestCacheValue("2345"), nativeMemoryMap.get(2))

        nativeMemoryMap.delete(1)

        assertEquals(1, nativeMemoryMap.size)
        assertEquals(null, nativeMemoryMap.get(1))
        assertEquals(TestCacheValue("2345"), nativeMemoryMap.get(2))
    }
}