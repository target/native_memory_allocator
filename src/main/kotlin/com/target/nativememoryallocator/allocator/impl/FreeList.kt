package com.target.nativememoryallocator.allocator.impl

import com.target.nativememoryallocator.unsafe.NativeMemoryPage

/**
 * An array of MemoryPages that are available for allocation.
 * Pages are allocated by removing from the left side of the array,
 * and free pages are put back onto the left side of the array (LIFO order).
 *
 * The FreeList is synchronized so multiple threads can use NativeMemoryAllocator concurrently.
 */
internal interface FreeList {

    /**
     * The total number of pages managed by the FreeList.
     */
    val totalNumPages: Int

    /**
     * The index of the next free page.  Initially 0 after FreeList is created.
     */
    fun nextFreePageIndex(): Int

    /**
     * The number of free pages.  Initially equal to totalNumPages after FreeList is created.
     */
    fun numFreePages(): Int

    /**
     * The number of used pages.  Initially 0 after FreeList is created.
     */
    fun numUsedPages(): Int

    /**
     * The number of exceptions during allocatePages that have occurred since the FreeList was created.
     *
     * Exceptions in allocatePages occur when the FreeList does not have enough free pages to satisfy the request.
     */
    fun numAllocationExceptions(): Int

    /**
     * The number of exceptions during freePages that have occurred since the FreeList was created.
     *
     * Exceptions in freePages occur when pages are freed multiple times.
     */
    fun numFreeExceptions(): Int

    /**
     * Allocate a List of [NativeMemoryPage] from the free list.
     *
     * @param numPagesToAllocate number of pages to allocate.
     * @return a List of [NativeMemoryPage]
     * @throws [IllegalStateException] if too few pages are free to satisfy the request.
     */
    fun allocatePages(numPagesToAllocate: Int): ArrayList<NativeMemoryPage>

    /**
     * Return a List of [NativeMemoryPage] to the free list.
     *
     * @param pages a List of [NativeMemoryPage]
     * @throws [IllegalStateException] if the free list does not have enough capacity to store pages.  This likely means pages were freed multiple times.
     */
    fun freePages(pages: List<NativeMemoryPage>)
}