package com.target.oss.nativememoryallocator.buffer

import com.target.oss.nativememoryallocator.buffer.impl.OnHeapMemoryBufferImpl

/**
 * A factory for [OnHeapMemoryBuffer].
 */
object OnHeapMemoryBufferFactory {

    /**
     * Construct an [OnHeapMemoryBuffer] with specified [initialCapacityBytes].
     *
     * @param initialCapacityBytes initial capacity in bytes.
     */
    fun newOnHeapMemoryBuffer(initialCapacityBytes: Int): OnHeapMemoryBuffer =
        OnHeapMemoryBufferImpl(initialCapacityBytes = initialCapacityBytes)

}