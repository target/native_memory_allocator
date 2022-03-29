package com.target.oss.nativememoryallocator.allocator

import com.target.oss.nativememoryallocator.allocator.impl.NativeMemoryAllocatorImpl

/**
 * Builder for [NativeMemoryAllocator].
 *
 * @property pageSizeBytes Page size in bytes.
 * @property nativeMemorySizeBytes Total native memory size in bytes.
 * @property zeroNativeMemoryOnStartup If true [NativeMemoryAllocator] writes zeros to the block of native memory it allocates on construction.
 */
data class NativeMemoryAllocatorBuilder(
    val pageSizeBytes: Int,
    val nativeMemorySizeBytes: Long,
    val zeroNativeMemoryOnStartup: Boolean = false,
) {

    /**
     * Build a [NativeMemoryAllocator] with the specified properties.
     * @return [NativeMemoryAllocator] instance.
     */
    fun build(): NativeMemoryAllocator =
        NativeMemoryAllocatorImpl(
            pageSizeBytes = pageSizeBytes,
            nativeMemorySizeBytes = nativeMemorySizeBytes,
            zeroNativeMemoryOnStartup = zeroNativeMemoryOnStartup
        )

}