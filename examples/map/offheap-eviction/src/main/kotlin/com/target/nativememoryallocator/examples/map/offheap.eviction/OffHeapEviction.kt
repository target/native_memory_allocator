package com.target.nativememoryallocator.examples.map.offheap.eviction

import com.target.nativememoryallocator.allocator.NativeMemoryAllocatorBuilder
import com.target.nativememoryallocator.examples.map.utils.CacheObject
import com.target.nativememoryallocator.examples.map.utils.CacheObjectSerializer
import com.target.nativememoryallocator.examples.map.utils.buildRandomString
import com.target.nativememoryallocator.map.NativeMemoryMapBackend
import com.target.nativememoryallocator.map.NativeMemoryMapBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

private val logger = KotlinLogging.logger {}


/**
 * Same as OffHeap but uses the Caffeine backend with maximumSize of 10,000 entries.
 */
private class OffHeapEviction {

    private val numEntries = 20_000

    private val randomIndex = Random.nextInt(0, numEntries)

    private val nativeMemoryAllocator = NativeMemoryAllocatorBuilder(
        pageSizeBytes = 4_096, // 4 KB
        nativeMemorySizeBytes = (20L * 1_024L * 1_024L * 1_024L), // 20 GB
    ).build()

    private val nativeMemoryMap = NativeMemoryMapBuilder<Int, CacheObject>(
        valueSerializer = CacheObjectSerializer(),
        nativeMemoryAllocator = nativeMemoryAllocator,
        backend = NativeMemoryMapBackend.CAFFEINE,
        caffeineConfigFunction = { caffeineConfigBuilder ->
            caffeineConfigBuilder
                .maximumSize(10_000)
                .recordStats()
        }
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
        logger.info { "nativeMemoryMap.hottestKeys(10) = ${nativeMemoryMap.hottestKeys(numKeys = 10)}" }
        logger.info { "nativeMemoryMap.coldestKeys(10) = ${nativeMemoryMap.coldestKeys(numKeys = 10)}" }

        val randomIndexValue = nativeMemoryMap.get(key = randomIndex)
        randomIndexValue?.let {
            logger.info { "get randomIndex = $randomIndex" }
            logger.info { "randomIndexValue.s.length = ${it.s.length}" }
            logger.info { "randomIndexValue.s.substring(0,20) = ${it.s.substring(0, 20)}" }
        }

        logger.info { "caffeine eviction count = ${nativeMemoryMap.stats.caffeineStats?.evictionCount()}" }

        while (true) {
            delay(1_000)
        }
    }

}

suspend fun main() {
    withContext(Dispatchers.Default) {
        OffHeapEviction().run()
    }
}
