package com.target.oss.nativememoryallocator.allocator

import com.target.oss.nativememoryallocator.buffer.NativeMemoryBuffer

// A NativeMemoryAllocator is an allocator for NativeMemoryBuffers.
// NativeMemoryAllocator has methods similar to malloc, realloc, and free in C.
// Methods in NativeMemoryAllocator are safe for use by multiple threads.
// However a single NativeMemoryBuffer may not be concurrently allocated/freed/resized
// by multiple threads.
interface NativeMemoryAllocator {

    val pageSizeBytes: Int

    val nativeMemorySizeBytes: Long

    val numFreePages: Int

    val totalNumPages: Int

    val numUsedPages: Int

    val numAllocationExceptions: Int

    val numFreeExceptions: Int

    val nativeMemoryAllocatorMetadata: NativeMemoryAllocatorMetadata

    // malloc
    fun allocateNativeMemoryBuffer(capacityBytes: Int): NativeMemoryBuffer

    // free
    fun freeNativeMemoryBuffer(buffer: NativeMemoryBuffer)

    // realloc
    fun resizeNativeMemoryBuffer(buffer: NativeMemoryBuffer, newCapacityBytes: Int)
}