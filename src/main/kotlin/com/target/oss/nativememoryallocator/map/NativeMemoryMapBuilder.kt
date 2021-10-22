package com.target.oss.nativememoryallocator.map

import com.target.oss.nativememoryallocator.allocator.NativeMemoryAllocator
import com.target.oss.nativememoryallocator.map.impl.NativeMemoryMapImpl

data class NativeMemoryMapBuilder<KEY_TYPE, VALUE_TYPE>(
    val valueSerializer: NativeMemoryMapSerializer<VALUE_TYPE>,
    val nativeMemoryAllocator: NativeMemoryAllocator,
    val useThreadLocalOnHeapReadBuffer: Boolean = true,
    val threadLocalOnHeapReadBufferInitialCapacityBytes: Int = (256 * 1024),
) {

    fun build(): NativeMemoryMap<KEY_TYPE, VALUE_TYPE> =
        NativeMemoryMapImpl(
            valueSerializer = valueSerializer,
            nativeMemoryAllocator = nativeMemoryAllocator,
            useThreadLocalOnHeapReadBuffer = useThreadLocalOnHeapReadBuffer,
            threadLocalOnHeapReadBufferInitialCapacityBytes = threadLocalOnHeapReadBufferInitialCapacityBytes,
        )

}