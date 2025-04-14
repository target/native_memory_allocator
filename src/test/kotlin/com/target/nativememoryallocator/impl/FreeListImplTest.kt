package com.target.nativememoryallocator.impl

import com.target.nativememoryallocator.allocator.impl.FreeListImpl
import com.target.nativememoryallocator.unsafe.NativeMemoryPage
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FreeListImplTest {

    @Test
    fun `test initialization`() {
        val baseNativeMemoryPointer = 0x80000000
        val pageSizeBytes = 4_096
        val totalNumPages = 100

        val expectedFreePageArray = LongArray(totalNumPages) { pageNumber ->
            baseNativeMemoryPointer + (pageNumber * pageSizeBytes)
        }

        val freeList = FreeListImpl(
            baseNativeMemoryPointer = baseNativeMemoryPointer,
            pageSizeBytes = pageSizeBytes,
            totalNumPages = totalNumPages,
        )

        freeList.freePageArray().size shouldBe totalNumPages
        freeList.freePageArray() shouldBe expectedFreePageArray
        freeList.nextFreePageIndex() shouldBe 0
        freeList.totalNumPages shouldBe totalNumPages
        freeList.numFreePages() shouldBe totalNumPages
        freeList.numUsedPages() shouldBe 0
        freeList.numAllocationExceptions() shouldBe 0
        freeList.numFreeExceptions() shouldBe 0
    }

    @Test
    fun `test allocation of 1 page`() {
        val baseNativeMemoryPointer = 0x80000000
        val pageSizeBytes = 4_096
        val totalNumPages = 100

        val expectedFreePageArray = LongArray(totalNumPages) { pageNumber ->
            baseNativeMemoryPointer + (pageNumber * pageSizeBytes)
        }
        expectedFreePageArray[0] = 0

        val freeList = FreeListImpl(
            baseNativeMemoryPointer = baseNativeMemoryPointer,
            pageSizeBytes = pageSizeBytes,
            totalNumPages = totalNumPages,
        )

        val retVal = freeList.allocatePages(1)

        retVal shouldBe arrayListOf(
            NativeMemoryPage(
                startAddress = baseNativeMemoryPointer,
            )
        )
        freeList.freePageArray().size shouldBe totalNumPages
        freeList.freePageArray() shouldBe expectedFreePageArray
        freeList.nextFreePageIndex() shouldBe 1
        freeList.totalNumPages shouldBe totalNumPages
        freeList.numFreePages() shouldBe totalNumPages - 1
        freeList.numUsedPages() shouldBe 1
        freeList.numAllocationExceptions() shouldBe 0
        freeList.numFreeExceptions() shouldBe 0
    }

    @Test
    fun `test allocation of 101 pages sequentially`() {
        val baseNativeMemoryPointer = 0x80000000
        val pageSizeBytes = 4_096
        val totalNumPages = 100

        val expectedFreePageArray = LongArray(totalNumPages) { 0 }

        val pagesAllocated = arrayListOf<NativeMemoryPage>()
        var exceptionsCaught = 0

        val expectedAllocations = (0 until totalNumPages).map {
            NativeMemoryPage(baseNativeMemoryPointer + (pageSizeBytes * it))
        }

        val freeList = FreeListImpl(
            baseNativeMemoryPointer = baseNativeMemoryPointer,
            pageSizeBytes = pageSizeBytes,
            totalNumPages = totalNumPages,
        )
        (0 until 101).forEach { _ ->
            try {
                pagesAllocated.addAll(freeList.allocatePages(1))
            } catch (e: IllegalStateException) {
                exceptionsCaught += 1
            }
        }

        pagesAllocated shouldBe expectedAllocations
        exceptionsCaught shouldBe 1
        freeList.freePageArray() shouldBe expectedFreePageArray
        freeList.nextFreePageIndex() shouldBe 100
        freeList.totalNumPages shouldBe totalNumPages
        freeList.numFreePages() shouldBe 0
        freeList.numUsedPages() shouldBe 100
        freeList.numAllocationExceptions() shouldBe 1
        freeList.numFreeExceptions() shouldBe 0
    }
}