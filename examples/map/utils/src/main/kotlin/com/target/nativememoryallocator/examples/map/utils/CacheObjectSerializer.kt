package com.target.nativememoryallocator.examples.map.utils

import com.target.nativememoryallocator.buffer.OnHeapMemoryBuffer
import com.target.nativememoryallocator.map.NativeMemoryMapSerializer

/**
 * A [NativeMemoryMapSerializer] for [CacheObject].
 *
 * Used when storing [CacheObject] in a [NativeMemoryMap].
 */
class CacheObjectSerializer : NativeMemoryMapSerializer<CacheObject> {

    override fun deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer: OnHeapMemoryBuffer): CacheObject {
        return CacheObject(
            s = String(onHeapMemoryBuffer.toTrimmedArray()),
        )
    }

    override fun serializeToByteArray(value: CacheObject): ByteArray {
        return value.s.toByteArray()
    }

}