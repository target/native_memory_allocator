package com.target.oss.nativememoryallocator.buffer

import com.target.oss.nativememoryallocator.buffer.impl.OnHeapMemoryBufferImpl

object OnHeapMemoryBufferFactory {

    fun newOnHeapMemoryBuffer(initialCapacityBytes: Int): OnHeapMemoryBuffer =
        OnHeapMemoryBufferImpl(initialCapacityBytes = initialCapacityBytes)

}