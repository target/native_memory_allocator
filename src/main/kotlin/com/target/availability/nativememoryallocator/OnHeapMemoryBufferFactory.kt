package com.target.availability.nativememoryallocator

import com.target.availability.nativememoryallocator.impl.OnHeapMemoryBufferImpl

object OnHeapMemoryBufferFactory {

    fun newOnHeapMemoryBuffer(initialCapacityBytes: Int): OnHeapMemoryBuffer =
        OnHeapMemoryBufferImpl(initialCapacityBytes = initialCapacityBytes)

}