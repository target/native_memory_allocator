package com.target.oss.nativememoryallocator.allocator

import com.target.oss.nativememoryallocator.allocator.impl.NativeMemoryAllocatorImpl

data class NativeMemoryAllocatorBuilder(
    val pageSizeBytes: Int,
    val nativeMemorySizeBytes: Long,
    val zeroNativeMemoryOnStartup: Boolean = false,
) {

    fun build(): NativeMemoryAllocator =
        NativeMemoryAllocatorImpl(
            pageSizeBytes = pageSizeBytes,
            nativeMemorySizeBytes = nativeMemorySizeBytes,
            zeroNativeMemoryOnStartup = zeroNativeMemoryOnStartup
        )

}