package com.target.oss.nativememoryallocator.allocator

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Metadata for [NativeMemoryAllocator] with jackson annotations.
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
    @JsonProperty("page_size_bytes")
    val pageSizeBytes: Int,

    @JsonProperty("next_free_page_index")
    val nextFreePageIndex: Int,

    @JsonProperty("num_free_pages")
    val numFreePages: Int,

    @JsonProperty("total_num_pages")
    val totalNumPages: Int,

    @JsonProperty("num_used_pages")
    val numUsedPages: Int,

    @JsonProperty("num_allocation_exceptions")
    val numAllocationExceptions: Int,

    @JsonProperty("num_free_exceptions")
    val numFreeExceptions: Int,

    @JsonProperty("native_memory_size_bytes")
    val nativeMemorySizeBytes: Long,
)