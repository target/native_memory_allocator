package com.target.oss.nativememoryallocator.buffer.impl

import com.target.oss.nativememoryallocator.buffer.OnHeapMemoryBuffer
import java.nio.ByteBuffer

class OnHeapMemoryBufferImpl(
    initialCapacityBytes: Int,
) : OnHeapMemoryBuffer {

    override var array = ByteArray(initialCapacityBytes.coerceAtLeast(minimumValue = 2))

    private var readableBytes = 0

    override fun getReadableBytes(): Int = readableBytes

    override fun setReadableBytes(newReadableBytes: Int) {
        readableBytes = newReadableBytes

        if (readableBytes > array.size) {
            var newArraySize = array.size * 2
            while (newArraySize < readableBytes) {
                newArraySize *= 2
            }
            array = ByteArray(newArraySize)
        }
    }

    override fun asByteBuffer(): ByteBuffer =
        ByteBuffer.wrap(array, 0, readableBytes)

    override fun toTrimmedArray(): ByteArray =
        array.sliceArray(0 until readableBytes)

    override fun toString(): String {
        return "OnHeapMemoryBufferImpl(readableBytes=$readableBytes, array.size=${array.size}, array=$array)"
    }

}