package com.target.availability.nativememoryallocator.impl

import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class FreeListImpl(
    baseNativeMemoryPointer: Long,
    pageSizeBytes: Int,
    override val totalNumPages: Int,
) : FreeList {

    // array of long freePageStartAddresses
    private val freePageArray: LongArray

    private var nextFreePageIndex: Int

    private var numFreePages: Int

    init {
        logger.info { "start creating $totalNumPages free pages" }

        freePageArray = LongArray(totalNumPages) { pageNumber ->
            baseNativeMemoryPointer + (pageNumber * pageSizeBytes.toLong())
        }
        nextFreePageIndex = 0
        numFreePages = totalNumPages

        logger.info { "nextFreePageIndex = $nextFreePageIndex numFreePages = $numFreePages" }
    }

    // for unit test only
    fun freePageArray(): LongArray = freePageArray

    @Synchronized
    override fun nextFreePageIndex(): Int = nextFreePageIndex

    @Synchronized
    override fun numFreePages(): Int = numFreePages

    @Synchronized
    override fun numUsedPages(): Int = totalNumPages - numFreePages()

    private fun allocatePage(): NativeMemoryPage? =
        if (numFreePages <= 0) {
            null
        } else {
            val pageIndex = nextFreePageIndex
            val pageStartAddress = freePageArray[pageIndex]
            freePageArray[pageIndex] = 0
            nextFreePageIndex += 1
            numFreePages -= 1
            NativeMemoryPage(startAddress = pageStartAddress)
        }

    private fun freePage(nativeMemoryPage: NativeMemoryPage) {
        if (numFreePages >= totalNumPages) {
            throw IllegalStateException("numFreePages = $numFreePages >= totalNumPages = $totalNumPages")
        }
        nextFreePageIndex -= 1
        numFreePages += 1
        freePageArray[nextFreePageIndex] = nativeMemoryPage.startAddress
    }

    @Synchronized
    override fun allocatePages(numPagesToAllocate: Int): ArrayList<NativeMemoryPage> {
        val allocatedPages = ArrayList<NativeMemoryPage>(numPagesToAllocate)

        (0 until numPagesToAllocate).mapNotNullTo(allocatedPages) {
            allocatePage()
        }

        if (allocatedPages.size != numPagesToAllocate) {
            freePages(allocatedPages)
            throw IllegalStateException("unable to allocate all pages, numPagesToAllocate = $numPagesToAllocate allocatedPages.size = ${allocatedPages.size}")
        }

        return allocatedPages
    }

    @Synchronized
    override fun freePages(pages: List<NativeMemoryPage>) {
        pages.asReversed().forEach { page -> freePage(page) }
    }
}