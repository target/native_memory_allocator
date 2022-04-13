package com.target.nativememoryallocator.examples.map.offheap

import com.target.nativememoryallocator.allocator.NativeMemoryAllocatorBuilder
import com.target.nativememoryallocator.examples.map.utils.CacheObject
import com.target.nativememoryallocator.examples.map.utils.CacheObjectSerializer
import com.target.nativememoryallocator.examples.map.utils.buildRandomString
import com.target.nativememoryallocator.map.NativeMemoryMapBackend
import com.target.nativememoryallocator.map.NativeMemoryMapBuilder
import kotlinx.coroutines.*
import mu.KotlinLogging
import kotlin.random.Random

private val logger = KotlinLogging.logger {}


/**
 * Demo application that puts 20,000 [CacheObject] instances into a [NativeMemoryMap].
 *
 * This demo uses the ConcurrentHashMap backend for [NativeMemoryMap].
 *
 * Each [CacheObject] instances contains a random string of length 500KB.
 *
 * This is a total of 10 GB of data in off-heap memory.
 *
 * OnHeap is the same application using normal on-heap storage for comparison.
 */
private class OffHeap {

    private val numEntries = 20_000

    private val randomIndex = Random.nextInt(0, numEntries)

    private val nativeMemoryAllocator = NativeMemoryAllocatorBuilder(
        pageSizeBytes = 4_096, // 4 KB
        nativeMemorySizeBytes = (20L * 1_024L * 1_024L * 1_024L), // 20 GB
    ).build()

    private val nativeMemoryMap = NativeMemoryMapBuilder<Int, CacheObject>(
        valueSerializer = CacheObjectSerializer(),
        nativeMemoryAllocator = nativeMemoryAllocator,
        backend = NativeMemoryMapBackend.CONCURRENT_HASH_MAP,
    ).build()

    private fun putValueIntoMap(i: Int) {
        if ((i % 100) == 0) {
            logger.info { "put i = $i" }
        }
        val value = buildRandomString(length = 500 * 1_024)
        if (i == randomIndex) {
            logger.info { "put randomIndex = $randomIndex value.length = ${value.length}" }
            logger.info { "value.substring(0,20) = ${value.substring(0, 20)}" }
        }
        nativeMemoryMap.put(
            key = i,
            value = CacheObject(
                s = value,
            ),
        )
    }

    suspend fun run() {
        logger.info { "begin run randomIndex = $randomIndex" }

        coroutineScope {
            (0 until numEntries).forEach { i ->
                launch {
                    putValueIntoMap(i = i)
                }
            }
        }

        logger.info { "nativeMemoryMap.size = ${nativeMemoryMap.size}" }
        logger.info { "nativeMemoryAllocator.nativeMemoryAllocatorMetadata = ${nativeMemoryAllocator.nativeMemoryAllocatorMetadata}" }

        val randomIndexValue = nativeMemoryMap.get(key = randomIndex)
        randomIndexValue?.let {
            logger.info { "get randomIndex = $randomIndex" }
            logger.info { "randomIndexValue.s.length = ${it.s.length}" }
            logger.info { "randomIndexValue.s.substring(0,20) = ${it.s.substring(0, 20)}" }
        }

        while (true) {
            delay(1_000)
        }
    }

}

suspend fun main() {
    withContext(Dispatchers.Default) {
        OffHeap().run()
    }
}
