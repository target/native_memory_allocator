package com.target.nativememoryallocator.examples.map.offheap.flatbuffers

import com.target.nativememoryallocator.allocator.NativeMemoryAllocatorBuilder
import com.target.nativememoryallocator.buffer.OnHeapMemoryBuffer
import com.target.nativememoryallocator.examples.map.offheap.flatbuffers.generated.FlatBufferDemoCacheObject
import com.target.nativememoryallocator.examples.map.offheap.flatbuffers.mapper.FlatBuffersMapper
import com.target.nativememoryallocator.examples.map.offheap.flatbuffers.model.DemoCacheObject
import com.target.nativememoryallocator.examples.map.offheap.flatbuffers.model.DemoCacheObjectListEntry
import com.target.nativememoryallocator.map.NativeMemoryMapBackend
import com.target.nativememoryallocator.map.NativeMemoryMapBuilder
import com.target.nativememoryallocator.map.NativeMemoryMapSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

private class DemoCacheObjectSerializer : NativeMemoryMapSerializer<DemoCacheObject> {

    override fun serializeToByteArray(demoCacheObject: DemoCacheObject): ByteArray {
        val flatBufferDemoCacheObject = FlatBuffersMapper.demoCacheObjectToFlatBuffer(
            demoCacheObject = demoCacheObject,
        )

        return flatBufferDemoCacheObject.byteBuffer.array()
    }

    override fun deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer: OnHeapMemoryBuffer): DemoCacheObject {
        val flatBufferDemoCacheObject = FlatBufferDemoCacheObject.getRootAsFlatBufferDemoCacheObject(
            onHeapMemoryBuffer.asByteBuffer(),
        )

        return FlatBuffersMapper.flatBufferToDemoCacheObject(
            flatBufferDemoCacheObject = flatBufferDemoCacheObject,
        )
    }

}

private class OffHeapFlatbuffers {

    private val nativeMemoryAllocator = NativeMemoryAllocatorBuilder(
        pageSizeBytes = 4_096, // 4 KB
        nativeMemorySizeBytes = (20L * 1_024L * 1_024L * 1_024L), // 20 GB
    ).build()

    private val nativeMemoryMap = NativeMemoryMapBuilder<Int, DemoCacheObject>(
        valueSerializer = DemoCacheObjectSerializer(),
        nativeMemoryAllocator = nativeMemoryAllocator,
        backend = NativeMemoryMapBackend.CONCURRENT_HASH_MAP,
    ).build()

    suspend fun run() {
        logger.info { "begin run" }

        val demoCacheObject = DemoCacheObject(
            id = 1,
            entryList = listOf(
                DemoCacheObjectListEntry(
                    id = 2,
                    booleanField = true,
                    stringField = "hello flatbuffers and nma",
                )
            )
        )

        val putResult = nativeMemoryMap.put(
            key = 1,
            value = demoCacheObject,
        )

        val getResult = nativeMemoryMap.get(
            key = 1,
        )

        logger.info { "putResult = $putResult getResult = $getResult demoCacheObject == getResult ${demoCacheObject == getResult}" }

        logger.info { "end run" }
    }
}

suspend fun main() {
    withContext(Dispatchers.Default) {
        OffHeapFlatbuffers().run()
    }
}