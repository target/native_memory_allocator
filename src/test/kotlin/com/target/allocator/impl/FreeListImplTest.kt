package com.target.allocator.impl

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

    @Test
    fun `test allocation of 101 pages non-sequentially`() {
        val baseNativeMemoryPointer = 0x80000000
        val pageSizeBytes = 4_096
        val totalNumPages = 100

        val freeList = FreeListImpl(
            baseNativeMemoryPointer = baseNativeMemoryPointer,
            pageSizeBytes = pageSizeBytes,
            totalNumPages = totalNumPages,
        )

        val pagesAllocated = arrayListOf<NativeMemoryPage>()
        var exceptionsCaught = 0

        val expectedAllocations = emptyList<NativeMemoryPage>()

        try {
            pagesAllocated.addAll(freeList.allocatePages(101))
        } catch (e: IllegalStateException) {
            exceptionsCaught += 1
        }

        val expectedFreePageArray = LongArray(totalNumPages) { pageNumber ->
            baseNativeMemoryPointer + (pageNumber * pageSizeBytes)
        }.reversedArray()

        pagesAllocated shouldBe expectedAllocations
        exceptionsCaught shouldBe 1

        // all pages allocated are freed
        freeList.freePageArray() shouldBe expectedFreePageArray
        freeList.nextFreePageIndex() shouldBe 0
        freeList.totalNumPages shouldBe totalNumPages
        freeList.numFreePages() shouldBe 100
        freeList.numUsedPages() shouldBe 0
        freeList.numAllocationExceptions() shouldBe 1
        freeList.numFreeExceptions() shouldBe 0
    }

    @Test
    fun `test allocation of 100 pages then free 100 pages`() {
        val baseNativeMemoryPointer = 0x80000000
        val pageSizeBytes = 4_096
        val totalNumPages = 100

        val freeList = FreeListImpl(
            baseNativeMemoryPointer = baseNativeMemoryPointer,
            pageSizeBytes = pageSizeBytes,
            totalNumPages = totalNumPages,
        )

        val pagesAllocated = arrayListOf<NativeMemoryPage>()

        val expectedAllocations = (0 until totalNumPages).map {
            NativeMemoryPage(baseNativeMemoryPointer + (pageSizeBytes * it))
        }

        pagesAllocated.addAll(freeList.allocatePages(100))

        pagesAllocated shouldBe expectedAllocations
        var expectedFreePageArray = LongArray(totalNumPages) { 0 }
        freeList.freePageArray() shouldBe expectedFreePageArray
        freeList.nextFreePageIndex() shouldBe 100
        freeList.totalNumPages shouldBe totalNumPages
        freeList.numFreePages() shouldBe 0
        freeList.numUsedPages() shouldBe 100
        freeList.numAllocationExceptions() shouldBe 0
        freeList.numFreeExceptions() shouldBe 0

        freeList.freePages(pagesAllocated)

        expectedFreePageArray = LongArray(totalNumPages) { pageNumber ->
            baseNativeMemoryPointer + (pageNumber * pageSizeBytes)
        }.reversedArray()
        freeList.freePageArray() shouldBe expectedFreePageArray
        freeList.nextFreePageIndex() shouldBe 0
        freeList.totalNumPages shouldBe totalNumPages
        freeList.numFreePages() shouldBe totalNumPages
        freeList.numUsedPages() shouldBe 0
        freeList.numAllocationExceptions() shouldBe 0
        freeList.numFreeExceptions() shouldBe 0
    }

    @Test
    fun `test allocation of 2 pages then free 1 page`() {
        val baseNativeMemoryPointer = 0x80000000
        val pageSizeBytes = 4_096
        val totalNumPages = 100

        val pagesAllocated = arrayListOf<NativeMemoryPage>()

        val expectedAllocations = (0 until 2).map {
            NativeMemoryPage(baseNativeMemoryPointer + (pageSizeBytes * it))
        }

        val freeList = FreeListImpl(
            baseNativeMemoryPointer = baseNativeMemoryPointer,
            pageSizeBytes = pageSizeBytes,
            totalNumPages = totalNumPages,
        )
        pagesAllocated.addAll(freeList.allocatePages(2))

        pagesAllocated shouldBe expectedAllocations

        val expectedFreePageArray = LongArray(totalNumPages) { pageNumber ->
            baseNativeMemoryPointer + (pageNumber * pageSizeBytes)
        }
        expectedFreePageArray[0] = 0
        expectedFreePageArray[1] = 0

        freeList.freePageArray() shouldBe expectedFreePageArray
        freeList.nextFreePageIndex() shouldBe 2
        freeList.totalNumPages shouldBe totalNumPages
        freeList.numFreePages() shouldBe totalNumPages - 2
        freeList.numUsedPages() shouldBe 2
    }

    @Test
    fun `test allocation of 2 pages then free 3 pages`() {
        val baseNativeMemoryPointer = 0x80000000
        val pageSizeBytes = 4_096
        val totalNumPages = 100

        val pagesAllocated = arrayListOf<NativeMemoryPage>()
        var numFreeExceptions = 0

        val expectedAllocations = (0 until 2).map {
            NativeMemoryPage(baseNativeMemoryPointer + (pageSizeBytes * it))
        }

        val freeList = FreeListImpl(
            baseNativeMemoryPointer = baseNativeMemoryPointer,
            pageSizeBytes = pageSizeBytes,
            totalNumPages = totalNumPages,
        )

        pagesAllocated.addAll(freeList.allocatePages(2))

        pagesAllocated shouldBe expectedAllocations
        var expectedFreePageArray = LongArray(totalNumPages) { pageNumber ->
            baseNativeMemoryPointer + (pageNumber * pageSizeBytes)
        }
        expectedFreePageArray[0] = 0
        expectedFreePageArray[1] = 0

        freeList.freePageArray() shouldBe expectedFreePageArray
        freeList.nextFreePageIndex() shouldBe 2
        freeList.totalNumPages shouldBe totalNumPages
        freeList.numFreePages() shouldBe totalNumPages - 2
        freeList.numUsedPages() shouldBe 2

        freeList.freePages(listOf(pagesAllocated[1], pagesAllocated[0]))

        try {
            freeList.freePages(listOf(pagesAllocated[0]))
        } catch (e: IllegalStateException) {
            numFreeExceptions++
        }

        pagesAllocated shouldBe expectedAllocations

        expectedFreePageArray = LongArray(totalNumPages) { pageNumber ->
            baseNativeMemoryPointer + (pageNumber * pageSizeBytes)
        }

        freeList.freePageArray() shouldBe expectedFreePageArray

        freeList.nextFreePageIndex() shouldBe 0
        freeList.totalNumPages shouldBe totalNumPages
        freeList.numFreePages() shouldBe 100
        freeList.numUsedPages() shouldBe 0
        freeList.numAllocationExceptions() shouldBe 0
        freeList.numFreeExceptions() shouldBe 1
    }
}