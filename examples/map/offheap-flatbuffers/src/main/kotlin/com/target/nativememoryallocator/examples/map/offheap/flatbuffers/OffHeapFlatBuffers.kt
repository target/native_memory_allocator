package com.target.nativememoryallocator.examples.map.offheap.flatbuffers

import com.target.nativememoryallocator.allocator.NativeMemoryAllocatorBuilder
import com.target.nativememoryallocator.examples.map.offheap.flatbuffers.model.DemoCacheObject
import com.target.nativememoryallocator.examples.map.offheap.flatbuffers.model.DemoCacheObjectListEntry
import com.target.nativememoryallocator.examples.map.offheap.flatbuffers.serializer.DemoCacheObjectSerializer
import com.target.nativememoryallocator.examples.map.utils.buildRandomString
import com.target.nativememoryallocator.map.NativeMemoryMapBackend
import com.target.nativememoryallocator.map.NativeMemoryMapBuilder
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.Random

private val logger = KotlinLogging.logger {}


/**
 * Demo application that puts 20,000 [DemoCacheObject] instances into a [NativeMemoryMap].
 *
 * This demo uses the ConcurrentHashMap backend for [NativeMemoryMap].
 *
 * Each [DemoCacheObject] instance contains a list of 2,000 [DemoCacheObjectListEntry]s.
 *
 * This is a total of ~11 GB of data in off-heap memory.
 */
private class OffHeapFlatBuffers {

    private val numMapEntries = 20_000

    private val numEntriesPerObject = 2_000

    private val randomIndex = Random.nextInt(0, numMapEntries)

    private var randomPutValue: DemoCacheObject? = null

    private val nativeMemoryAllocator = NativeMemoryAllocatorBuilder(
        pageSizeBytes = 4_096, // 4 KB
        nativeMemorySizeBytes = (20L * 1_024L * 1_024L * 1_024L), // 20 GB
    ).build()

    private val nativeMemoryMap = NativeMemoryMapBuilder<Int, DemoCacheObject>(
        valueSerializer = DemoCacheObjectSerializer(),
        nativeMemoryAllocator = nativeMemoryAllocator,
        backend = NativeMemoryMapBackend.CONCURRENT_HASH_MAP,
    ).build()

    private fun buildDemoCacheObject(i: Int) =
        DemoCacheObject(
            id = i,
            entryList = (0 until numEntriesPerObject).map { j ->
                DemoCacheObjectListEntry(
                    id = j,
                    booleanField = ThreadLocalRandom.current().nextBoolean(),
                    stringField = buildRandomString(length = 250),
                )
            }
        )

    private fun putValueIntoMap(i: Int) {
        if ((i % 100) == 0) {
            logger.info { "put i = $i" }
        }
        val demoCacheObject = buildDemoCacheObject(i = i)
        if (i == randomIndex) {
            logger.info { "put randomIndex = $randomIndex demoCacheObject.entryList.size = ${demoCacheObject.entryList.size}" }
            logger.info {
                "substring = ${demoCacheObject.entryList[0].stringField.substring(0, 20)}"
            }
            randomPutValue = demoCacheObject
        }
        nativeMemoryMap.put(
            key = i,
            value = demoCacheObject,
        )
    }

    suspend fun run() {
        logger.info { "begin run randomIndex = $randomIndex" }

        coroutineScope {
            (0 until numMapEntries).forEach { i ->
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
            logger.info { "randomIndexValue.entryList.size = ${it.entryList.size}" }
            logger.info { "substring = ${it.entryList[0].stringField.substring(0, 20)}" }
            logger.info { "randomIndexValue == randomPutValue = ${it == randomPutValue}" }
        }

        while (true) {
            delay(1_000)
        }
    }
}

suspend fun main() {
    withContext(Dispatchers.Default) {
        OffHeapFlatBuffers().run()
    }
}