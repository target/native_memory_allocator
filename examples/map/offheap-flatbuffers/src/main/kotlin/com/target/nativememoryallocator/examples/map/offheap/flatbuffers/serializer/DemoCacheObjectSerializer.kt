package com.target.nativememoryallocator.examples.map.offheap.flatbuffers.serializer

import com.target.nativememoryallocator.buffer.OnHeapMemoryBuffer
import com.target.nativememoryallocator.examples.map.offheap.flatbuffers.generated.FlatBufferDemoCacheObject
import com.target.nativememoryallocator.examples.map.offheap.flatbuffers.mapper.FlatBuffersMapper
import com.target.nativememoryallocator.examples.map.offheap.flatbuffers.model.DemoCacheObject
import com.target.nativememoryallocator.map.NativeMemoryMapSerializer

/**
 * A [NativeMemoryMapSerializer] for [DemoCacheObject].
 *
 * This serializer converts to [FlatBufferDemoCacheObject] using [FlatBuffersMapper].
 *
 * The FlatBuffers byte array is stored in off-heap storage in the [NativeMemoryMap].
 */
class DemoCacheObjectSerializer : NativeMemoryMapSerializer<DemoCacheObject> {

    override fun serializeToByteArray(value: DemoCacheObject): ByteArray {
        val flatBufferDemoCacheObject = FlatBuffersMapper.demoCacheObjectToFlatBuffer(
            demoCacheObject = value,
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