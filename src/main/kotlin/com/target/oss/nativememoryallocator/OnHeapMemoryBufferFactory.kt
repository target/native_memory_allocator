package com.target.oss.nativememoryallocator

import com.target.oss.nativememoryallocator.impl.OnHeapMemoryBufferImpl

object OnHeapMemoryBufferFactory {

    fun newOnHeapMemoryBuffer(initialCapacityBytes: Int): OnHeapMemoryBuffer =
        OnHeapMemoryBufferImpl(initialCapacityBytes = initialCapacityBytes)

}