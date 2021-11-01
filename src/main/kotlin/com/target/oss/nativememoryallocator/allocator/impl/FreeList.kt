package com.target.oss.nativememoryallocator.allocator.impl

import com.target.oss.nativememoryallocator.unsafe.NativeMemoryPage

// A FreeList is a list of MemoryPages that are available for allocation.
// Pages are allocated by removing from the left side of the array,
// and free pages are put back onto the left side of the array (LIFO order).
// The FreeList is synchronized so multiple threads can use NativeMemoryAllocator concurrently.
interface FreeList {

    val totalNumPages: Int

    fun nextFreePageIndex(): Int

    fun numFreePages(): Int

    fun numUsedPages(): Int

    fun numAllocationExceptions(): Int

    fun numFreeExceptions(): Int

    fun allocatePages(numPagesToAllocate: Int): ArrayList<NativeMemoryPage>

    fun freePages(pages: List<NativeMemoryPage>)
}