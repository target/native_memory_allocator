package com.target.oss.nativememoryallocator.allocator

import com.fasterxml.jackson.annotation.JsonProperty

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