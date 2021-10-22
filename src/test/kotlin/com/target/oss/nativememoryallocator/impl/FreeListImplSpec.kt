package com.target.oss.nativememoryallocator.impl

import io.mockk.clearAllMocks
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature

class FreeListSpec : Spek({
    Feature("FreeList") {
        Scenario("test initialization") {
            val baseNativeMemoryPointer = 0x80000000
            val pageSizeBytes = 4_096
            val totalNumPages = 100

            val expectedFreePageArray = LongArray(totalNumPages) { pageNumber ->
                baseNativeMemoryPointer + (pageNumber * pageSizeBytes)
            }

            lateinit var freeList: FreeListImpl

            When("construct freeList") {
                freeList = FreeListImpl(
                    baseNativeMemoryPointer = baseNativeMemoryPointer,
                    pageSizeBytes = pageSizeBytes,
                    totalNumPages = totalNumPages,
                )
            }
            Then("initial freeList state is correct") {
                assertEquals(totalNumPages, freeList.freePageArray().size)

                assertTrue(expectedFreePageArray.contentEquals(freeList.freePageArray()))

                assertEquals(0, freeList.nextFreePageIndex())
                assertEquals(totalNumPages, freeList.totalNumPages)
                assertEquals(totalNumPages, freeList.numFreePages())
                assertEquals(0, freeList.numUsedPages())
            }
            clearAllMocks()
        }
        Scenario("test allocation of 1 page") {
            val baseNativeMemoryPointer = 0x80000000
            val pageSizeBytes = 4_096
            val totalNumPages = 100

            val expectedFreePageArray = LongArray(totalNumPages) { pageNumber ->
                baseNativeMemoryPointer + (pageNumber * pageSizeBytes)
            }
            expectedFreePageArray[0] = 0

            lateinit var freeList: FreeListImpl
            lateinit var retVal: ArrayList<NativeMemoryPage>

            Given("setup freeList") {
                freeList = FreeListImpl(
                    baseNativeMemoryPointer = baseNativeMemoryPointer,
                    pageSizeBytes = pageSizeBytes,
                    totalNumPages = totalNumPages,
                )
            }
            When("allocate 1 page") {
                retVal = freeList.allocatePages(1)
            }
            Then("freeList state is correct") {
                assertEquals(
                    arrayListOf(
                        NativeMemoryPage(
                            startAddress = baseNativeMemoryPointer,
                        )
                    ), retVal)

                assertTrue(expectedFreePageArray.contentEquals(freeList.freePageArray()))

                assertEquals(1, freeList.nextFreePageIndex())
                assertEquals(totalNumPages, freeList.totalNumPages)
                assertEquals(totalNumPages - 1, freeList.numFreePages())
                assertEquals(1, freeList.numUsedPages())
            }
            clearAllMocks()
        }
        Scenario("test allocation of 101 pages sequentially") {
            val baseNativeMemoryPointer = 0x80000000
            val pageSizeBytes = 4_096
            val totalNumPages = 100

            lateinit var freeList: FreeListImpl

            val expectedFreePageArray = LongArray(totalNumPages) { 0 }

            val pagesAllocated = arrayListOf<NativeMemoryPage>()
            var exceptionsCaught = 0

            val expectedAllocations = (0 until totalNumPages).map {
                NativeMemoryPage(baseNativeMemoryPointer + (pageSizeBytes * it))
            }

            Given("setup freeList") {
                freeList = FreeListImpl(
                    baseNativeMemoryPointer = baseNativeMemoryPointer,
                    pageSizeBytes = pageSizeBytes,
                    totalNumPages = totalNumPages,
                )
            }
            When("try to allocate 101 pages") {
                (0 until 101).forEach { _ ->
                    try {
                        pagesAllocated.addAll(freeList.allocatePages(1))
                    } catch (e: IllegalStateException) {
                        exceptionsCaught += 1
                    }
                }
            }
            Then("freeList state is correct") {
                assertEquals(expectedAllocations, pagesAllocated)
                assertEquals(1, exceptionsCaught)

                assertTrue(expectedFreePageArray.contentEquals(freeList.freePageArray()))

                assertEquals(100, freeList.nextFreePageIndex())
                assertEquals(totalNumPages, freeList.totalNumPages)
                assertEquals(0, freeList.numFreePages())
                assertEquals(100, freeList.numUsedPages())
            }
            clearAllMocks()
        }
        Scenario("test allocation of 101 pages non-sequentially") {
            val baseNativeMemoryPointer = 0x80000000
            val pageSizeBytes = 4_096
            val totalNumPages = 100

            lateinit var freeList: FreeListImpl

            val pagesAllocated = arrayListOf<NativeMemoryPage>()
            var exceptionsCaught = 0

            val expectedAllocations = emptyList<NativeMemoryPage>()

            val expectedFreePageArray = LongArray(totalNumPages) { pageNumber ->
                baseNativeMemoryPointer + (pageNumber * pageSizeBytes)
            }

            Given("setup freeList") {
                freeList = FreeListImpl(
                    baseNativeMemoryPointer = baseNativeMemoryPointer,
                    pageSizeBytes = pageSizeBytes,
                    totalNumPages = totalNumPages,
                )
            }
            When("try to allocate 101 pages") {
                try {
                    pagesAllocated.addAll(freeList.allocatePages(101))
                } catch (e: IllegalStateException) {
                    exceptionsCaught += 1
                }
            }
            Then("freeList state is correct") {
                assertEquals(expectedAllocations, pagesAllocated)
                assertEquals(1, exceptionsCaught)

                // all pages allocated are freed
                assertTrue(expectedFreePageArray.contentEquals(freeList.freePageArray()))
                assertEquals(0, freeList.nextFreePageIndex())
                assertEquals(totalNumPages, freeList.totalNumPages)
                assertEquals(100, freeList.numFreePages())
                assertEquals(0, freeList.numUsedPages())
            }
            clearAllMocks()
        }
        Scenario("test allocation of 100 pages then free 100 pages") {
            val baseNativeMemoryPointer = 0x80000000
            val pageSizeBytes = 4_096
            val totalNumPages = 100

            lateinit var freeList: FreeListImpl

            val pagesAllocated = arrayListOf<NativeMemoryPage>()

            val expectedAllocations = (0 until totalNumPages).map {
                NativeMemoryPage(baseNativeMemoryPointer + (pageSizeBytes * it))
            }

            Given("setup freeList") {
                freeList = FreeListImpl(
                    baseNativeMemoryPointer = baseNativeMemoryPointer,
                    pageSizeBytes = pageSizeBytes,
                    totalNumPages = totalNumPages,
                )
            }
            When("allocate 100 pages") {
                pagesAllocated.addAll(freeList.allocatePages(100))
            }
            Then("freeList state is correct") {
                assertEquals(expectedAllocations, pagesAllocated)

                val expectedFreePageArray = LongArray(totalNumPages) { 0 }
                assertTrue(expectedFreePageArray.contentEquals(freeList.freePageArray()))
                assertEquals(100, freeList.nextFreePageIndex())
                assertEquals(totalNumPages, freeList.totalNumPages)
                assertEquals(0, freeList.numFreePages())
                assertEquals(100, freeList.numUsedPages())
            }
            When("free 100 pages") {
                freeList.freePages(pagesAllocated)
            }
            Then("freeList state is correct") {
                val expectedFreePageArray = LongArray(totalNumPages) { pageNumber ->
                    baseNativeMemoryPointer + (pageNumber * pageSizeBytes)
                }
                assertTrue(expectedFreePageArray.contentEquals(freeList.freePageArray()))
                assertEquals(0, freeList.nextFreePageIndex())
                assertEquals(totalNumPages, freeList.totalNumPages)
                assertEquals(totalNumPages, freeList.numFreePages())
                assertEquals(0, freeList.numUsedPages())
            }
            clearAllMocks()
        }
        Scenario("test allocation of 2 pages then free 1 page") {
            val baseNativeMemoryPointer = 0x80000000
            val pageSizeBytes = 4_096
            val totalNumPages = 100

            lateinit var freeList: FreeListImpl

            val pagesAllocated = arrayListOf<NativeMemoryPage>()

            val expectedAllocations = (0 until 2).map {
                NativeMemoryPage(baseNativeMemoryPointer + (pageSizeBytes * it))
            }

            Given("setup freeList") {
                freeList = FreeListImpl(
                    baseNativeMemoryPointer = baseNativeMemoryPointer,
                    pageSizeBytes = pageSizeBytes,
                    totalNumPages = totalNumPages,
                )
            }
            When("allocate 2 pages") {
                pagesAllocated.addAll(freeList.allocatePages(2))
            }
            Then("freeList state is correct") {
                assertEquals(expectedAllocations, pagesAllocated)

                val expectedFreePageArray = LongArray(totalNumPages) { pageNumber ->
                    baseNativeMemoryPointer + (pageNumber * pageSizeBytes)
                }
                expectedFreePageArray[0] = 0
                expectedFreePageArray[1] = 0
                assertTrue(expectedFreePageArray.contentEquals(freeList.freePageArray()))

                assertEquals(2, freeList.nextFreePageIndex())
                assertEquals(totalNumPages, freeList.totalNumPages)
                assertEquals(98, freeList.numFreePages())
                assertEquals(2, freeList.numUsedPages())
            }
            When("free 1 page") {
                freeList.freePages(listOf(pagesAllocated[0]))
            }
            Then("freeList state is correct") {
                assertEquals(expectedAllocations, pagesAllocated)

                val expectedFreePageArray = LongArray(totalNumPages) { pageNumber ->
                    baseNativeMemoryPointer + (pageNumber * pageSizeBytes)
                }
                expectedFreePageArray[1] = expectedFreePageArray[0]
                expectedFreePageArray[0] = 0
                assertTrue(expectedFreePageArray.contentEquals(freeList.freePageArray()))

                assertEquals(1, freeList.nextFreePageIndex())
                assertEquals(totalNumPages, freeList.totalNumPages)
                assertEquals(99, freeList.numFreePages())
                assertEquals(1, freeList.numUsedPages())
            }
            clearAllMocks()
        }
    }

})
