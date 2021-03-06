package com.target.nativememoryallocator.allocator.impl

import com.target.nativememoryallocator.allocator.NativeMemoryAllocatorMetadata
import com.target.nativememoryallocator.buffer.NativeMemoryBuffer
import com.target.nativememoryallocator.unsafe.UnsafeContainer
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import sun.misc.Unsafe

class NativeMemoryAllocatorImplSpec : Spek({
    Feature("NativeMemoryAllocatorImpl") {
        Scenario("test validateNativeMemoryAllocatorInitialParameters") {
            var pageSizeBytes = 0
            var nativeMemorySizeBytes = 0L

            When("negative pageSizeBytes") {
                pageSizeBytes = -1
                nativeMemorySizeBytes = 1L * 1024L * 1024L * 1024L
            }
            Then("validateNativeMemoryAllocatorInitialParameters throws IllegalArgumentException") {
                assertThrows(IllegalArgumentException::class.java) {
                    validateNativeMemoryAllocatorInitialParameters(
                        pageSizeBytes = pageSizeBytes,
                        nativeMemorySizeBytes = nativeMemorySizeBytes,
                    )
                }
            }
            When("zero pageSizeBytes") {
                pageSizeBytes = 0
                nativeMemorySizeBytes = 1L * 1024L * 1024L * 1024L
            }
            Then("validateNativeMemoryAllocatorInitialParameters throws IllegalArgumentException") {
                assertThrows(IllegalArgumentException::class.java) {
                    validateNativeMemoryAllocatorInitialParameters(
                        pageSizeBytes = pageSizeBytes,
                        nativeMemorySizeBytes = nativeMemorySizeBytes,
                    )
                }
            }
            When("positive pageSizeBytes") {
                pageSizeBytes = 1
                nativeMemorySizeBytes = 1L * 1024L * 1024L * 1024L
            }
            Then("validateNativeMemoryAllocatorInitialParameters does not throw") {
                validateNativeMemoryAllocatorInitialParameters(
                    pageSizeBytes = pageSizeBytes,
                    nativeMemorySizeBytes = nativeMemorySizeBytes,
                )
            }
            When("max pageSizeBytes") {
                pageSizeBytes = Int.MAX_VALUE
                nativeMemorySizeBytes = Int.MAX_VALUE.toLong()
            }
            Then("validateNativeMemoryAllocatorInitialParameters does not throw") {
                validateNativeMemoryAllocatorInitialParameters(
                    pageSizeBytes = pageSizeBytes,
                    nativeMemorySizeBytes = nativeMemorySizeBytes,
                )
            }
            When("negative nativeMemorySizeBytes") {
                pageSizeBytes = 1
                nativeMemorySizeBytes = -1L
            }
            Then("validateNativeMemoryAllocatorInitialParameters throws IllegalArgumentException") {
                assertThrows(IllegalArgumentException::class.java) {
                    validateNativeMemoryAllocatorInitialParameters(
                        pageSizeBytes = pageSizeBytes,
                        nativeMemorySizeBytes = nativeMemorySizeBytes,
                    )
                }
            }
            When("zero nativeMemorySizeBytes") {
                pageSizeBytes = 1
                nativeMemorySizeBytes = 0L
            }
            Then("validateNativeMemoryAllocatorInitialParameters throws IllegalArgumentException") {
                assertThrows(IllegalArgumentException::class.java) {
                    validateNativeMemoryAllocatorInitialParameters(
                        pageSizeBytes = pageSizeBytes,
                        nativeMemorySizeBytes = nativeMemorySizeBytes,
                    )
                }
            }
            When("positive nativeMemorySizeBytes") {
                pageSizeBytes = 1
                nativeMemorySizeBytes = 1L
            }
            Then("validateNativeMemoryAllocatorInitialParameters does not throw") {
                validateNativeMemoryAllocatorInitialParameters(
                    pageSizeBytes = pageSizeBytes,
                    nativeMemorySizeBytes = nativeMemorySizeBytes,
                )
            }
            When("positive nativeMemorySizeBytes") {
                pageSizeBytes = 4 * 1024
                nativeMemorySizeBytes = (1L * 1024L * 1024L * 1024L)
            }
            Then("validateNativeMemoryAllocatorInitialParameters does not throw") {
                validateNativeMemoryAllocatorInitialParameters(
                    pageSizeBytes = pageSizeBytes,
                    nativeMemorySizeBytes = nativeMemorySizeBytes,
                )
            }
            When("nativeMemorySizeBytes not evenly divisible by pageSizeBytes") {
                pageSizeBytes = 4 * 1024
                nativeMemorySizeBytes = (1L * 1024L * 1024L * 1024L) + 1L
            }
            Then("validateNativeMemoryAllocatorInitialParameters throws IllegalArgumentException") {
                assertThrows(IllegalArgumentException::class.java) {
                    validateNativeMemoryAllocatorInitialParameters(
                        pageSizeBytes = pageSizeBytes,
                        nativeMemorySizeBytes = nativeMemorySizeBytes,
                    )
                }
            }
            When("invalid totalNumPages") {
                pageSizeBytes = 1
                nativeMemorySizeBytes = (20L * 1024L * 1024L * 1024L)
            }
            Then("validateNativeMemoryAllocatorInitialParameters throws IllegalArgumentException") {
                assertThrows(IllegalArgumentException::class.java) {
                    validateNativeMemoryAllocatorInitialParameters(
                        pageSizeBytes = pageSizeBytes,
                        nativeMemorySizeBytes = nativeMemorySizeBytes,
                    )
                }
            }
        }
        Scenario("test initialization") {
            lateinit var mockUnsafe: Unsafe
            val pageSizeBytes = 4_096 // 4kb
            val nativeMemorySizeBytes = 1L * 1024 * 1024 * 1024 // 1gb
            val expectedNumPages = (nativeMemorySizeBytes / pageSizeBytes).toInt()
            val mockNativeMemoryPointer = 0x80000000
            lateinit var nativeMemoryAllocator: NativeMemoryAllocatorImpl

            Given("setup unsafe") {
                mockUnsafe = mockk()
                mockkObject(UnsafeContainer)
                every {
                    UnsafeContainer.unsafe
                } returns mockUnsafe

                every {
                    mockUnsafe.allocateMemory(nativeMemorySizeBytes)
                } returns mockNativeMemoryPointer
            }
            When("construct nativeMemoryAllocator") {
                nativeMemoryAllocator = NativeMemoryAllocatorImpl(
                    pageSizeBytes = pageSizeBytes,
                    nativeMemorySizeBytes = nativeMemorySizeBytes,
                    zeroNativeMemoryOnStartup = false,
                )
            }
            Then("initial nativeMemoryAllocator is correct") {
                assertEquals(mockNativeMemoryPointer, nativeMemoryAllocator.baseNativeMemoryPointer())
                assertEquals(expectedNumPages, nativeMemoryAllocator.numFreePages)
                assertEquals(expectedNumPages, nativeMemoryAllocator.totalNumPages)
                assertEquals(0, nativeMemoryAllocator.numUsedPages)
                assertEquals(0, nativeMemoryAllocator.numAllocationExceptions)
                assertEquals(0, nativeMemoryAllocator.numFreeExceptions)

                val expectedNativeMemoryAllocatorMetadata = NativeMemoryAllocatorMetadata(
                    pageSizeBytes = pageSizeBytes,
                    nextFreePageIndex = 0,
                    numFreePages = expectedNumPages,
                    totalNumPages = expectedNumPages,
                    numUsedPages = 0,
                    numAllocationExceptions = 0,
                    numFreeExceptions = 0,
                    nativeMemorySizeBytes = nativeMemorySizeBytes,
                )
                assertEquals(expectedNativeMemoryAllocatorMetadata, nativeMemoryAllocator.nativeMemoryAllocatorMetadata)

                verify(exactly = 1) {
                    mockUnsafe.allocateMemory(nativeMemorySizeBytes)
                }

                verify(exactly = 0) {
                    mockUnsafe.setMemory(any(), any(), any())
                }
            }
            clearAllMocks()
        }
        Scenario("test initialization with zeroNativeMemoryOnStartup = true") {
            lateinit var mockUnsafe: Unsafe
            val pageSizeBytes = 4_096 // 4kb
            val nativeMemorySizeBytes = 1L * 1024 * 1024 * 1024 // 1gb
            val expectedNumPages = (nativeMemorySizeBytes / pageSizeBytes).toInt()
            val mockNativeMemoryPointer = 0x80000000
            lateinit var nativeMemoryAllocator: NativeMemoryAllocatorImpl

            Given("setup unsafe") {
                mockUnsafe = mockk()
                mockkObject(UnsafeContainer)
                every {
                    UnsafeContainer.unsafe
                } returns mockUnsafe

                every {
                    mockUnsafe.allocateMemory(nativeMemorySizeBytes)
                } returns mockNativeMemoryPointer

                every {
                    mockUnsafe.setMemory(mockNativeMemoryPointer, nativeMemorySizeBytes, 0)
                } returns Unit
            }
            When("construct nativeMemoryAllocator") {
                nativeMemoryAllocator = NativeMemoryAllocatorImpl(
                    pageSizeBytes = pageSizeBytes,
                    nativeMemorySizeBytes = nativeMemorySizeBytes,
                    zeroNativeMemoryOnStartup = true,
                )
            }
            Then("initial nativeMemoryAllocator is correct") {
                assertEquals(mockNativeMemoryPointer, nativeMemoryAllocator.baseNativeMemoryPointer())
                assertEquals(expectedNumPages, nativeMemoryAllocator.numFreePages)
                assertEquals(expectedNumPages, nativeMemoryAllocator.totalNumPages)
                assertEquals(0, nativeMemoryAllocator.numUsedPages)
                assertEquals(0, nativeMemoryAllocator.numAllocationExceptions)
                assertEquals(0, nativeMemoryAllocator.numFreeExceptions)

                val expectedNativeMemoryAllocatorMetadata = NativeMemoryAllocatorMetadata(
                    pageSizeBytes = pageSizeBytes,
                    nextFreePageIndex = 0,
                    numFreePages = expectedNumPages,
                    totalNumPages = expectedNumPages,
                    numUsedPages = 0,
                    numAllocationExceptions = 0,
                    numFreeExceptions = 0,
                    nativeMemorySizeBytes = nativeMemorySizeBytes,
                )
                assertEquals(expectedNativeMemoryAllocatorMetadata, nativeMemoryAllocator.nativeMemoryAllocatorMetadata)

                verify(exactly = 1) {
                    mockUnsafe.allocateMemory(nativeMemorySizeBytes)
                }

                verify(exactly = 1) {
                    mockUnsafe.setMemory(mockNativeMemoryPointer, nativeMemorySizeBytes, 0)
                }
            }
            clearAllMocks()
        }
        Scenario("test initialization invalid nativeMemorySizeBytes") {
            lateinit var mockUnsafe: Unsafe
            val pageSizeBytes = 4_096 // 4kb
            val nativeMemorySizeBytes = (1L * 1024 * 1024 * 1024) + 1 // 1gb + 1 byte
            val mockNativeMemoryPointer = 0x80000000
            var exceptionsCaught = 0

            Given("setup unsafe") {
                mockUnsafe = mockk()
                mockkObject(UnsafeContainer)
                every {
                    UnsafeContainer.unsafe
                } returns mockUnsafe

                every {
                    mockUnsafe.allocateMemory(any())
                } returns mockNativeMemoryPointer
            }
            When("construct nativeMemoryAllocator") {
                try {
                    NativeMemoryAllocatorImpl(
                        pageSizeBytes = pageSizeBytes,
                        nativeMemorySizeBytes = nativeMemorySizeBytes,
                        zeroNativeMemoryOnStartup = false,
                    )
                } catch (e: IllegalArgumentException) {
                    exceptionsCaught += 1
                }
            }
            Then("initial nativeMemoryAllocator is correct") {
                assertEquals(1, exceptionsCaught)

                verify(exactly = 0) {
                    mockUnsafe.allocateMemory(any())
                }
            }
            clearAllMocks()
        }
        Scenario("test allocation of 100 bytes, then free, then double free, then resize freed buffer") {
            lateinit var mockUnsafe: Unsafe
            val pageSizeBytes = 4_096 // 4kb
            val nativeMemorySizeBytes = 1L * 1024 * 1024 * 1024 // 1gb
            val totalNumPages = (nativeMemorySizeBytes / pageSizeBytes).toInt()
            val mockNativeMemoryPointer = 0x80000000
            lateinit var nativeMemoryAllocator: NativeMemoryAllocatorImpl
            lateinit var buffer: NativeMemoryBuffer
            var doubleFreeExceptions = 0
            var resizeExceptions = 0

            Given("setup unsafe") {
                mockUnsafe = mockk()
                mockkObject(UnsafeContainer)
                every {
                    UnsafeContainer.unsafe
                } returns mockUnsafe

                every {
                    mockUnsafe.allocateMemory(nativeMemorySizeBytes)
                } returns mockNativeMemoryPointer
            }
            When("construct nativeMemoryAllocator and allocate a buffer") {
                nativeMemoryAllocator = NativeMemoryAllocatorImpl(
                    pageSizeBytes = pageSizeBytes,
                    nativeMemorySizeBytes = nativeMemorySizeBytes,
                    zeroNativeMemoryOnStartup = false,
                )

                buffer = nativeMemoryAllocator.allocateNativeMemoryBuffer(
                    capacityBytes = 100,
                )
            }
            Then("buffer and nativeMemoryAllocator state are correct") {
                verify(exactly = 1) {
                    mockUnsafe.allocateMemory(nativeMemorySizeBytes)
                }

                assertEquals(mockNativeMemoryPointer, nativeMemoryAllocator.baseNativeMemoryPointer())
                assertEquals(totalNumPages - 1, nativeMemoryAllocator.numFreePages)
                assertEquals(totalNumPages, nativeMemoryAllocator.totalNumPages)
                assertEquals(1, nativeMemoryAllocator.numUsedPages)

                val expectedNativeMemoryAllocatorMetadata = NativeMemoryAllocatorMetadata(
                    pageSizeBytes = pageSizeBytes,
                    nextFreePageIndex = 1,
                    numFreePages = totalNumPages - 1,
                    totalNumPages = totalNumPages,
                    numUsedPages = 1,
                    numAllocationExceptions = 0,
                    numFreeExceptions = 0,
                    nativeMemorySizeBytes = nativeMemorySizeBytes,
                )
                assertEquals(expectedNativeMemoryAllocatorMetadata, nativeMemoryAllocator.nativeMemoryAllocatorMetadata)

                assertEquals(pageSizeBytes, buffer.pageSizeBytes)
                assertEquals(100, buffer.capacityBytes)
                assertEquals(false, buffer.freed)
                assertEquals(1, buffer.numPages)
                assertEquals(0, nativeMemoryAllocator.numAllocationExceptions)
                assertEquals(0, nativeMemoryAllocator.numFreeExceptions)

            }
            When("free buffer") {
                nativeMemoryAllocator.freeNativeMemoryBuffer(
                    buffer = buffer,
                )
            }
            Then("buffer and nativeMemoryAllocator state are correct") {
                assertEquals(mockNativeMemoryPointer, nativeMemoryAllocator.baseNativeMemoryPointer())
                assertEquals(totalNumPages, nativeMemoryAllocator.numFreePages)
                assertEquals(totalNumPages, nativeMemoryAllocator.totalNumPages)
                assertEquals(0, nativeMemoryAllocator.numUsedPages)
                assertEquals(0, nativeMemoryAllocator.numAllocationExceptions)
                assertEquals(0, nativeMemoryAllocator.numFreeExceptions)

                assertEquals(pageSizeBytes, buffer.pageSizeBytes)
                assertEquals(0, buffer.capacityBytes)
                assertEquals(true, buffer.freed)
                assertEquals(0, buffer.numPages)
            }
            When("free buffer again") {
                try {
                    nativeMemoryAllocator.freeNativeMemoryBuffer(
                        buffer = buffer,
                    )
                } catch (e: IllegalStateException) {
                    doubleFreeExceptions += 1
                }
            }
            Then("double free exception thrown, buffer and nativeMemoryAllocator state are correct") {
                assertEquals(1, doubleFreeExceptions)

                assertEquals(mockNativeMemoryPointer, nativeMemoryAllocator.baseNativeMemoryPointer())
                assertEquals(totalNumPages, nativeMemoryAllocator.numFreePages)
                assertEquals(totalNumPages, nativeMemoryAllocator.totalNumPages)
                assertEquals(0, nativeMemoryAllocator.numUsedPages)
                assertEquals(0, nativeMemoryAllocator.numAllocationExceptions)
                assertEquals(0, nativeMemoryAllocator.numFreeExceptions)

                assertEquals(pageSizeBytes, buffer.pageSizeBytes)
                assertEquals(0, buffer.capacityBytes)
                assertEquals(true, buffer.freed)
                assertEquals(0, buffer.numPages)
            }
            When("resize buffer") {
                try {
                    nativeMemoryAllocator.resizeNativeMemoryBuffer(
                        buffer = buffer,
                        newCapacityBytes = 100,
                    )
                } catch (e: IllegalStateException) {
                    resizeExceptions += 1
                }
            }
            Then("resize freed exception thrown, buffer and nativeMemoryAllocator state are correct") {
                assertEquals(1, resizeExceptions)

                assertEquals(mockNativeMemoryPointer, nativeMemoryAllocator.baseNativeMemoryPointer())
                assertEquals(totalNumPages, nativeMemoryAllocator.numFreePages)
                assertEquals(totalNumPages, nativeMemoryAllocator.totalNumPages)
                assertEquals(0, nativeMemoryAllocator.numUsedPages)
                assertEquals(0, nativeMemoryAllocator.numAllocationExceptions)
                assertEquals(0, nativeMemoryAllocator.numFreeExceptions)

                assertEquals(pageSizeBytes, buffer.pageSizeBytes)
                assertEquals(0, buffer.capacityBytes)
                assertEquals(true, buffer.freed)
                assertEquals(0, buffer.numPages)
            }
            clearAllMocks()
        }
        Scenario("test allocation of 10_000 bytes, then resize scenarios, then free") {
            lateinit var mockUnsafe: Unsafe
            val pageSizeBytes = 4_096 // 4kb
            val nativeMemorySizeBytes = 1L * 1024 * 1024 * 1024 // 1gb
            val totalNumPages = (nativeMemorySizeBytes / pageSizeBytes).toInt()
            val mockNativeMemoryPointer = 0x80000000
            lateinit var nativeMemoryAllocator: NativeMemoryAllocatorImpl
            lateinit var buffer: NativeMemoryBuffer

            Given("setup unsafe") {
                mockUnsafe = mockk()
                every {
                    UnsafeContainer.unsafe
                } returns mockUnsafe

                every {
                    mockUnsafe.allocateMemory(nativeMemorySizeBytes)
                } returns mockNativeMemoryPointer
            }
            When("construct nativeMemoryAllocator and allocate a buffer") {
                nativeMemoryAllocator = NativeMemoryAllocatorImpl(
                    pageSizeBytes = pageSizeBytes,
                    nativeMemorySizeBytes = nativeMemorySizeBytes,
                    zeroNativeMemoryOnStartup = false,
                )

                buffer = nativeMemoryAllocator.allocateNativeMemoryBuffer(
                    capacityBytes = 10_000,
                )
            }
            Then("buffer and nativeMemoryAllocator state are correct") {
                verify(exactly = 1) {
                    mockUnsafe.allocateMemory(nativeMemorySizeBytes)
                }

                assertEquals(mockNativeMemoryPointer, nativeMemoryAllocator.baseNativeMemoryPointer())
                assertEquals(totalNumPages - 3, nativeMemoryAllocator.numFreePages)
                assertEquals(totalNumPages, nativeMemoryAllocator.totalNumPages)
                assertEquals(3, nativeMemoryAllocator.numUsedPages)
                assertEquals(0, nativeMemoryAllocator.numAllocationExceptions)
                assertEquals(0, nativeMemoryAllocator.numFreeExceptions)

                assertEquals(pageSizeBytes, buffer.pageSizeBytes)
                assertEquals(10_000, buffer.capacityBytes)
                assertEquals(false, buffer.freed)
                assertEquals(3, buffer.numPages)
            }
            When("resize to same capacity") {
                nativeMemoryAllocator.resizeNativeMemoryBuffer(
                    buffer = buffer,
                    newCapacityBytes = 10_000,
                )
            }
            Then("buffer and nativeMemoryAllocator state are correct") {
                assertEquals(mockNativeMemoryPointer, nativeMemoryAllocator.baseNativeMemoryPointer())
                assertEquals(totalNumPages - 3, nativeMemoryAllocator.numFreePages)
                assertEquals(totalNumPages, nativeMemoryAllocator.totalNumPages)
                assertEquals(3, nativeMemoryAllocator.numUsedPages)
                assertEquals(0, nativeMemoryAllocator.numAllocationExceptions)
                assertEquals(0, nativeMemoryAllocator.numFreeExceptions)

                assertEquals(pageSizeBytes, buffer.pageSizeBytes)
                assertEquals(10_000, buffer.capacityBytes)
                assertEquals(false, buffer.freed)
                assertEquals(3, buffer.numPages)
            }
            When("resize to new capacity, same number of pages") {
                nativeMemoryAllocator.resizeNativeMemoryBuffer(
                    buffer = buffer,
                    newCapacityBytes = 10_500,
                )
            }
            Then("buffer and nativeMemoryAllocator state are correct") {
                assertEquals(mockNativeMemoryPointer, nativeMemoryAllocator.baseNativeMemoryPointer())
                assertEquals(totalNumPages - 3, nativeMemoryAllocator.numFreePages)
                assertEquals(totalNumPages, nativeMemoryAllocator.totalNumPages)
                assertEquals(3, nativeMemoryAllocator.numUsedPages)
                assertEquals(0, nativeMemoryAllocator.numAllocationExceptions)
                assertEquals(0, nativeMemoryAllocator.numFreeExceptions)

                assertEquals(pageSizeBytes, buffer.pageSizeBytes)
                assertEquals(10_500, buffer.capacityBytes)
                assertEquals(false, buffer.freed)
                assertEquals(3, buffer.numPages)
            }
            When("resize to new capacity, more pages") {
                nativeMemoryAllocator.resizeNativeMemoryBuffer(
                    buffer = buffer,
                    newCapacityBytes = 17_000,
                )
            }
            Then("buffer and nativeMemoryAllocator state are correct") {
                assertEquals(mockNativeMemoryPointer, nativeMemoryAllocator.baseNativeMemoryPointer())
                assertEquals(totalNumPages - 5, nativeMemoryAllocator.numFreePages)
                assertEquals(totalNumPages, nativeMemoryAllocator.totalNumPages)
                assertEquals(5, nativeMemoryAllocator.numUsedPages)
                assertEquals(0, nativeMemoryAllocator.numAllocationExceptions)
                assertEquals(0, nativeMemoryAllocator.numFreeExceptions)

                assertEquals(pageSizeBytes, buffer.pageSizeBytes)
                assertEquals(17_000, buffer.capacityBytes)
                assertEquals(false, buffer.freed)
                assertEquals(5, buffer.numPages)
            }
            When("resize to new capacity, fewer pages") {
                nativeMemoryAllocator.resizeNativeMemoryBuffer(
                    buffer = buffer,
                    newCapacityBytes = 4_097,
                )
            }
            Then("buffer and nativeMemoryAllocator state are correct") {
                assertEquals(mockNativeMemoryPointer, nativeMemoryAllocator.baseNativeMemoryPointer())
                assertEquals(totalNumPages - 2, nativeMemoryAllocator.numFreePages)
                assertEquals(totalNumPages, nativeMemoryAllocator.totalNumPages)
                assertEquals(2, nativeMemoryAllocator.numUsedPages)
                assertEquals(0, nativeMemoryAllocator.numAllocationExceptions)
                assertEquals(0, nativeMemoryAllocator.numFreeExceptions)

                assertEquals(pageSizeBytes, buffer.pageSizeBytes)
                assertEquals(4_097, buffer.capacityBytes)
                assertEquals(false, buffer.freed)
                assertEquals(2, buffer.numPages)
            }
            When("free buffer") {
                nativeMemoryAllocator.freeNativeMemoryBuffer(buffer)
            }
            Then("buffer and nativeMemoryAllocator state are correct") {
                assertEquals(mockNativeMemoryPointer, nativeMemoryAllocator.baseNativeMemoryPointer())
                assertEquals(totalNumPages, nativeMemoryAllocator.numFreePages)
                assertEquals(totalNumPages, nativeMemoryAllocator.totalNumPages)
                assertEquals(0, nativeMemoryAllocator.numUsedPages)

                assertEquals(pageSizeBytes, buffer.pageSizeBytes)
                assertEquals(0, buffer.capacityBytes)
                assertEquals(true, buffer.freed)
                assertEquals(0, buffer.numPages)
            }
            clearAllMocks()
        }
        Scenario("test allocation of 5_000 bytes, then resize out of memory, then free") {
            lateinit var mockUnsafe: Unsafe
            val pageSizeBytes = 4_096 // 4kb
            val nativeMemorySizeBytes = 1L * 1024 * 1024 * 1024 // 1gb
            val totalNumPages = (nativeMemorySizeBytes / pageSizeBytes).toInt()
            val mockNativeMemoryPointer = 0x80000000
            lateinit var nativeMemoryAllocator: NativeMemoryAllocatorImpl
            lateinit var buffer: NativeMemoryBuffer
            var resizeExceptions = 0

            Given("setup unsafe") {
                mockUnsafe = mockk()
                every {
                    UnsafeContainer.unsafe
                } returns mockUnsafe

                every {
                    mockUnsafe.allocateMemory(nativeMemorySizeBytes)
                } returns mockNativeMemoryPointer
            }
            When("construct nativeMemoryAllocator and allocate a buffer") {
                nativeMemoryAllocator = NativeMemoryAllocatorImpl(
                    pageSizeBytes = pageSizeBytes,
                    nativeMemorySizeBytes = nativeMemorySizeBytes,
                    zeroNativeMemoryOnStartup = false,
                )

                buffer = nativeMemoryAllocator.allocateNativeMemoryBuffer(
                    capacityBytes = 5_000,
                )
            }
            Then("buffer and nativeMemoryAllocator state are correct") {
                verify(exactly = 1) {
                    mockUnsafe.allocateMemory(nativeMemorySizeBytes)
                }

                assertEquals(mockNativeMemoryPointer, nativeMemoryAllocator.baseNativeMemoryPointer())
                assertEquals(totalNumPages - 2, nativeMemoryAllocator.numFreePages)
                assertEquals(totalNumPages, nativeMemoryAllocator.totalNumPages)
                assertEquals(2, nativeMemoryAllocator.numUsedPages)
                assertEquals(0, nativeMemoryAllocator.numAllocationExceptions)
                assertEquals(0, nativeMemoryAllocator.numFreeExceptions)

                assertEquals(pageSizeBytes, buffer.pageSizeBytes)
                assertEquals(5_000, buffer.capacityBytes)
                assertEquals(false, buffer.freed)
                assertEquals(2, buffer.numPages)
            }
            When("resize buffer to max capacity") {
                nativeMemoryAllocator.resizeNativeMemoryBuffer(
                    buffer = buffer,
                    newCapacityBytes = nativeMemorySizeBytes.toInt(), //1gb
                )
            }
            Then("buffer and nativeMemoryAllocator state are correct") {
                assertEquals(mockNativeMemoryPointer, nativeMemoryAllocator.baseNativeMemoryPointer())
                assertEquals(0, nativeMemoryAllocator.numFreePages)
                assertEquals(totalNumPages, nativeMemoryAllocator.totalNumPages)
                assertEquals(totalNumPages, nativeMemoryAllocator.numUsedPages)
                assertEquals(0, nativeMemoryAllocator.numAllocationExceptions)
                assertEquals(0, nativeMemoryAllocator.numFreeExceptions)

                assertEquals(pageSizeBytes, buffer.pageSizeBytes)
                assertEquals(nativeMemorySizeBytes.toInt(), buffer.capacityBytes)
                assertEquals(false, buffer.freed)
                assertEquals(totalNumPages, buffer.numPages)
            }
            When("resize buffer to 1 page") {
                nativeMemoryAllocator.resizeNativeMemoryBuffer(
                    buffer = buffer,
                    newCapacityBytes = 4_096,
                )
            }
            Then("buffer and nativeMemoryAllocator state are correct") {
                assertEquals(mockNativeMemoryPointer, nativeMemoryAllocator.baseNativeMemoryPointer())
                assertEquals(totalNumPages - 1, nativeMemoryAllocator.numFreePages)
                assertEquals(totalNumPages, nativeMemoryAllocator.totalNumPages)
                assertEquals(1, nativeMemoryAllocator.numUsedPages)
                assertEquals(0, nativeMemoryAllocator.numAllocationExceptions)
                assertEquals(0, nativeMemoryAllocator.numFreeExceptions)

                assertEquals(pageSizeBytes, buffer.pageSizeBytes)
                assertEquals(4_096, buffer.capacityBytes)
                assertEquals(false, buffer.freed)
                assertEquals(1, buffer.numPages)
            }
            When("resize buffer 1 byte more than max capacity") {
                try {
                    nativeMemoryAllocator.resizeNativeMemoryBuffer(
                        buffer = buffer,
                        newCapacityBytes = nativeMemorySizeBytes.toInt() + 1,
                    )
                } catch (e: IllegalStateException) {
                    resizeExceptions += 1
                }
            }
            Then("resize throws exception, buffer and nativeMemoryAllocator state are correct") {
                assertEquals(1, resizeExceptions)
                assertEquals(mockNativeMemoryPointer, nativeMemoryAllocator.baseNativeMemoryPointer())
                assertEquals(totalNumPages - 1, nativeMemoryAllocator.numFreePages)
                assertEquals(totalNumPages, nativeMemoryAllocator.totalNumPages)
                assertEquals(1, nativeMemoryAllocator.numUsedPages)
                assertEquals(1, nativeMemoryAllocator.numAllocationExceptions)
                assertEquals(0, nativeMemoryAllocator.numFreeExceptions)

                val expectedNativeMemoryAllocatorMetadata = NativeMemoryAllocatorMetadata(
                    pageSizeBytes = pageSizeBytes,
                    nextFreePageIndex = 1,
                    numFreePages = totalNumPages - 1,
                    totalNumPages = totalNumPages,
                    numUsedPages = 1,
                    numAllocationExceptions = 1,
                    numFreeExceptions = 0,
                    nativeMemorySizeBytes = nativeMemorySizeBytes,
                )
                assertEquals(expectedNativeMemoryAllocatorMetadata, nativeMemoryAllocator.nativeMemoryAllocatorMetadata)


                assertEquals(pageSizeBytes, buffer.pageSizeBytes)
                assertEquals(4_096, buffer.capacityBytes)
                assertEquals(false, buffer.freed)
                assertEquals(1, buffer.numPages)
            }
            When("free buffer") {
                nativeMemoryAllocator.freeNativeMemoryBuffer(
                    buffer = buffer,
                )
            }
            Then("buffer and nativeMemoryAllocator state are correct") {
                assertEquals(mockNativeMemoryPointer, nativeMemoryAllocator.baseNativeMemoryPointer())
                assertEquals(totalNumPages, nativeMemoryAllocator.numFreePages)
                assertEquals(totalNumPages, nativeMemoryAllocator.totalNumPages)
                assertEquals(0, nativeMemoryAllocator.numUsedPages)
                assertEquals(1, nativeMemoryAllocator.numAllocationExceptions)
                assertEquals(0, nativeMemoryAllocator.numFreeExceptions)

                assertEquals(pageSizeBytes, buffer.pageSizeBytes)
                assertEquals(0, buffer.capacityBytes)
                assertEquals(true, buffer.freed)
                assertEquals(0, buffer.numPages)
            }
            clearAllMocks()
        }
        Scenario("test allocation of negative capacity") {
            lateinit var mockUnsafe: Unsafe
            val pageSizeBytes = 4_096 // 4kb
            val nativeMemorySizeBytes = 1L * 1024 * 1024 * 1024 // 1gb
            val totalNumPages = (nativeMemorySizeBytes / pageSizeBytes).toInt()
            val mockNativeMemoryPointer = 0x80000000
            lateinit var nativeMemoryAllocator: NativeMemoryAllocatorImpl
            var buffer: NativeMemoryBuffer? = null
            var allocateNativeMemoryBufferExceptions = 0

            Given("setup unsafe") {
                mockUnsafe = mockk()
                every {
                    UnsafeContainer.unsafe
                } returns mockUnsafe

                every {
                    mockUnsafe.allocateMemory(nativeMemorySizeBytes)
                } returns mockNativeMemoryPointer
            }
            When("construct nativeMemoryAllocator and allocate a buffer") {
                nativeMemoryAllocator = NativeMemoryAllocatorImpl(
                    pageSizeBytes = pageSizeBytes,
                    nativeMemorySizeBytes = nativeMemorySizeBytes,
                    zeroNativeMemoryOnStartup = false,
                )

                try {
                    buffer = nativeMemoryAllocator.allocateNativeMemoryBuffer(
                        capacityBytes = -1,
                    )
                } catch (e: IllegalArgumentException) {
                    ++allocateNativeMemoryBufferExceptions
                }
            }
            Then("buffer and nativeMemoryAllocator state are correct") {
                verify(exactly = 1) {
                    mockUnsafe.allocateMemory(nativeMemorySizeBytes)
                }

                assertEquals(1, allocateNativeMemoryBufferExceptions)
                assertEquals(null, buffer)

                assertEquals(mockNativeMemoryPointer, nativeMemoryAllocator.baseNativeMemoryPointer())
                assertEquals(totalNumPages, nativeMemoryAllocator.numFreePages)
                assertEquals(totalNumPages, nativeMemoryAllocator.totalNumPages)
                assertEquals(0, nativeMemoryAllocator.numUsedPages)
                assertEquals(0, nativeMemoryAllocator.numAllocationExceptions)
                assertEquals(0, nativeMemoryAllocator.numFreeExceptions)
            }
            clearAllMocks()
        }
        Scenario("test allocation of 10_000 bytes, then resize to negative value") {
            lateinit var mockUnsafe: Unsafe
            val pageSizeBytes = 4_096 // 4kb
            val nativeMemorySizeBytes = 1L * 1024 * 1024 * 1024 // 1gb
            val totalNumPages = (nativeMemorySizeBytes / pageSizeBytes).toInt()
            val mockNativeMemoryPointer = 0x80000000
            lateinit var nativeMemoryAllocator: NativeMemoryAllocatorImpl
            lateinit var buffer: NativeMemoryBuffer
            var resizeNativeMemoryBufferExceptions = 0

            Given("setup unsafe") {
                mockUnsafe = mockk()
                every {
                    UnsafeContainer.unsafe
                } returns mockUnsafe

                every {
                    mockUnsafe.allocateMemory(nativeMemorySizeBytes)
                } returns mockNativeMemoryPointer
            }
            When("construct nativeMemoryAllocator and allocate a buffer") {
                nativeMemoryAllocator = NativeMemoryAllocatorImpl(
                    pageSizeBytes = pageSizeBytes,
                    nativeMemorySizeBytes = nativeMemorySizeBytes,
                    zeroNativeMemoryOnStartup = false,
                )

                buffer = nativeMemoryAllocator.allocateNativeMemoryBuffer(
                    capacityBytes = 10_000,
                )
            }
            Then("buffer and nativeMemoryAllocator state are correct") {
                verify(exactly = 1) {
                    mockUnsafe.allocateMemory(nativeMemorySizeBytes)
                }

                assertEquals(mockNativeMemoryPointer, nativeMemoryAllocator.baseNativeMemoryPointer())
                assertEquals(totalNumPages - 3, nativeMemoryAllocator.numFreePages)
                assertEquals(totalNumPages, nativeMemoryAllocator.totalNumPages)
                assertEquals(3, nativeMemoryAllocator.numUsedPages)
                assertEquals(0, nativeMemoryAllocator.numAllocationExceptions)
                assertEquals(0, nativeMemoryAllocator.numFreeExceptions)

                assertEquals(pageSizeBytes, buffer.pageSizeBytes)
                assertEquals(10_000, buffer.capacityBytes)
                assertEquals(false, buffer.freed)
                assertEquals(3, buffer.numPages)
            }
            When("resize to -1") {
                try {
                    nativeMemoryAllocator.resizeNativeMemoryBuffer(
                        buffer = buffer,
                        newCapacityBytes = -1,
                    )
                } catch (e: IllegalArgumentException) {
                    ++resizeNativeMemoryBufferExceptions
                }
            }
            Then("buffer and nativeMemoryAllocator state are correct") {
                assertEquals(1, resizeNativeMemoryBufferExceptions)
                assertEquals(mockNativeMemoryPointer, nativeMemoryAllocator.baseNativeMemoryPointer())
                assertEquals(totalNumPages - 3, nativeMemoryAllocator.numFreePages)
                assertEquals(totalNumPages, nativeMemoryAllocator.totalNumPages)
                assertEquals(3, nativeMemoryAllocator.numUsedPages)
                assertEquals(0, nativeMemoryAllocator.numAllocationExceptions)
                assertEquals(0, nativeMemoryAllocator.numFreeExceptions)

                assertEquals(pageSizeBytes, buffer.pageSizeBytes)
                assertEquals(10_000, buffer.capacityBytes)
                assertEquals(false, buffer.freed)
                assertEquals(3, buffer.numPages)
            }
            clearAllMocks()
        }
    }
})