package com.target.oss.nativememoryallocator.allocator

import com.target.oss.nativememoryallocator.buffer.NativeMemoryBuffer

/**
 * An allocator for NativeMemoryBuffers.
 *
 * NativeMemoryAllocator has methods similar to malloc, realloc, and free in C.
 *
 * All properties and methods in NativeMemoryAllocator are safe for use by multiple threads.
 *
 * However, a single NativeMemoryBuffer may not be concurrently allocated/freed/resized
 * by multiple threads.
 */
interface NativeMemoryAllocator {

    /**
     * Page size in bytes.
     */
    val pageSizeBytes: Int

    /**
     * Total native memory size in bytes.
     */
    val nativeMemorySizeBytes: Long

    /**
     * Number of free pages.
     */
    val numFreePages: Int

    /**
     * Total number of free and used pages.
     */
    val totalNumPages: Int

    /**
     * Number of used pages.
     */
    val numUsedPages: Int

    /**
     * The number of exceptions during allocation that have occurred since the NativeMemoryAllocator was created.
     *
     * These exceptions occur when there are not enough free pages to satisfy the request.
     */
    val numAllocationExceptions: Int

    /**
     * The number of exceptions during freeNativeMemoryBuffer that have occurred since the NativeMemoryAllocator was created.
     *
     * These exceptions occur when pages are freed multiple times.
     */
    val numFreeExceptions: Int

    /**
     * Get the current NativeMemoryAllocatorMetadata.
     */
    val nativeMemoryAllocatorMetadata: NativeMemoryAllocatorMetadata

    /**
     * Allocate a [NativeMemoryBuffer] with capacity large enough to hold [capacityBytes] bytes.
     *
     * Similar to the malloc() function in C.
     *
     * @param capacityBytes capacity in bytes to allocate
     * @return NativeMemoryBuffer
     * @throws [IllegalArgumentException] if capacityBytes is negative.
     * @throws [IllegalStateException] if too few pages are free to satisfy the request.
     */
    fun allocateNativeMemoryBuffer(capacityBytes: Int): NativeMemoryBuffer

    /**
     * Free a [NativeMemoryBuffer], making its pages available for future allocations.
     *
     * Similar to the free() function in C.
     *
     * @param buffer a [NativeMemoryBuffer] that was previously returned by allocateNativeMemoryBuffer.
     * @throws [IllegalStateException] if [buffer] has already been freed.
     */
    fun freeNativeMemoryBuffer(buffer: NativeMemoryBuffer)

    /**
     * Reallocate a [NativeMemoryBuffer] to change its capacity.  Capacity may be increased or decreased.
     * Pages will be allocated or freed as needed.
     *
     * Similar to the realloc() function in C.
     *
     * @param buffer a [NativeMemoryBuffer] that was previously returned by allocateNativeMemoryBuffer.
     * @param newCapacityBytes new capacity in bytes
     * @throws [IllegalArgumentException] if newCapacityBytes is negative.
     * @throws [IllegalStateException] if [buffer] has already been freed, or if too few pages are free to satisfy the request.
     */
    fun resizeNativeMemoryBuffer(buffer: NativeMemoryBuffer, newCapacityBytes: Int)
}