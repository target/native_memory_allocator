package com.target.nativememoryallocator.allocator

/**
 * Metadata for [NativeMemoryAllocator].
 *
 * @property pageSizeBytes Page size in bytes.
 * @property nextFreePageIndex The index of the next free page.  Initially 0 after [NativeMemoryAllocator] is created.
 * @property numFreePages Number of free pages.
 * @property totalNumPages Total number of free and used pages.
 * @property numUsedPages Number of used pages.
 * @property numAllocationExceptions Number of exceptions since the [NativeMemoryAllocator] was created where there are not enough free pages to satisfy the request.
 * @property numFreeExceptions Number of exceptions since the [NativeMemoryAllocator] was created where pages are freed multiple times.
 * @property nativeMemorySizeBytes Total number of used and free pages.
 */
data class NativeMemoryAllocatorMetadata(
    val pageSizeBytes: Int,
    val nextFreePageIndex: Int,
    val numFreePages: Int,
    val totalNumPages: Int,
    val numUsedPages: Int,
    val numAllocationExceptions: Int,
    val numFreeExceptions: Int,
    val nativeMemorySizeBytes: Long,
)