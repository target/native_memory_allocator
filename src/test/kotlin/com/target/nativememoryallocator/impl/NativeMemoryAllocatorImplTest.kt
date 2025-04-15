package com.target.nativememoryallocator.impl

import com.target.nativememoryallocator.allocator.NativeMemoryAllocatorMetadata
import com.target.nativememoryallocator.allocator.impl.NativeMemoryAllocatorImpl
import com.target.nativememoryallocator.allocator.impl.validateNativeMemoryAllocatorInitialParameters
import com.target.nativememoryallocator.unsafe.UnsafeContainer
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import sun.misc.Unsafe

class NativeMemoryAllocatorImplTest {

    private val mockUnsafe = mockk<Unsafe>()

    @BeforeEach
    fun beforeEach() {
        mockkObject(UnsafeContainer)

        every { UnsafeContainer.unsafe } returns mockUnsafe
    }

    @AfterEach
    fun afterEach() {
        clearAllMocks()
    }

    @Test
    fun `test validateNativeMemoryAllocatorInitialParameters`() {
        var pageSizeBytes = 0
        var nativeMemorySizeBytes = 0L

        // negative pageSizeBytes
        pageSizeBytes = -1
        nativeMemorySizeBytes = 1L * 1024L * 1024L * 1024L

        shouldThrow<IllegalArgumentException> {
            validateNativeMemoryAllocatorInitialParameters(
                pageSizeBytes = pageSizeBytes,
                nativeMemorySizeBytes = nativeMemorySizeBytes,
            )
        }

        // zero pageSizeBytes
        pageSizeBytes = 0
        nativeMemorySizeBytes = 1L * 1024L * 1024L * 1024L

        shouldThrow<IllegalArgumentException> {
            validateNativeMemoryAllocatorInitialParameters(
                pageSizeBytes = pageSizeBytes,
                nativeMemorySizeBytes = nativeMemorySizeBytes,
            )
        }

        // positive pageSizeBytes
        pageSizeBytes = 1
        nativeMemorySizeBytes = 1L * 1024L * 1024L * 1024L

        shouldNotThrowAny {
            validateNativeMemoryAllocatorInitialParameters(
                pageSizeBytes = pageSizeBytes,
                nativeMemorySizeBytes = nativeMemorySizeBytes,
            )
        }

        // max pageSizeBytes
        pageSizeBytes = Int.MAX_VALUE
        nativeMemorySizeBytes = Int.MAX_VALUE.toLong()

        shouldNotThrowAny {
            validateNativeMemoryAllocatorInitialParameters(
                pageSizeBytes = pageSizeBytes,
                nativeMemorySizeBytes = nativeMemorySizeBytes,
            )
        }

        // negative nativeMemorySizeBytes
        pageSizeBytes = 1
        nativeMemorySizeBytes = -1L

        shouldThrow<IllegalArgumentException> {
            validateNativeMemoryAllocatorInitialParameters(
                pageSizeBytes = pageSizeBytes,
                nativeMemorySizeBytes = nativeMemorySizeBytes,
            )
        }

        // zero nativeMemorySizeBytes
        pageSizeBytes = 1
        nativeMemorySizeBytes = 0L

        shouldThrow<IllegalArgumentException> {
            validateNativeMemoryAllocatorInitialParameters(
                pageSizeBytes = pageSizeBytes,
                nativeMemorySizeBytes = nativeMemorySizeBytes,
            )
        }

        // positive nativeMemorySizeBytes
        pageSizeBytes = 1
        nativeMemorySizeBytes = 1L

        shouldNotThrowAny {
            validateNativeMemoryAllocatorInitialParameters(
                pageSizeBytes = pageSizeBytes,
                nativeMemorySizeBytes = nativeMemorySizeBytes,
            )
        }

        // positive nativeMemorySizeBytes
        pageSizeBytes = 4 * 1024
        nativeMemorySizeBytes = (1L * 1024L * 1024L * 1024L)

        shouldNotThrowAny {
            validateNativeMemoryAllocatorInitialParameters(
                pageSizeBytes = pageSizeBytes,
                nativeMemorySizeBytes = nativeMemorySizeBytes,
            )
        }

        // nativeMemorySizeBytes not evenly divisible by pageSizeBytes
        pageSizeBytes = 4 * 1024
        nativeMemorySizeBytes = (1L * 1024L * 1024L * 1024L) + 1L

        shouldThrow<IllegalArgumentException> {
            validateNativeMemoryAllocatorInitialParameters(
                pageSizeBytes = pageSizeBytes,
                nativeMemorySizeBytes = nativeMemorySizeBytes,
            )
        }

        // invalid totalNumPages (larger than Int.MAX_VALUE)
        pageSizeBytes = 1
        nativeMemorySizeBytes = (20L * 1024L * 1024L * 1024L)
        shouldThrow<IllegalArgumentException> {
            validateNativeMemoryAllocatorInitialParameters(
                pageSizeBytes = pageSizeBytes,
                nativeMemorySizeBytes = nativeMemorySizeBytes,
            )
        }
    }

    @Test
    fun `test initialization`() {
        val pageSizeBytes = 4_096 // 4kb
        val nativeMemorySizeBytes = 1L * 1024 * 1024 * 1024 // 1gb
        val expectedNumPages = (nativeMemorySizeBytes / pageSizeBytes).toInt()
        val mockNativeMemoryPointer = 0x80000000

        every {
            mockUnsafe.allocateMemory(nativeMemorySizeBytes)
        } returns mockNativeMemoryPointer

        val nativeMemoryAllocator = NativeMemoryAllocatorImpl(
            pageSizeBytes = pageSizeBytes,
            nativeMemorySizeBytes = nativeMemorySizeBytes,
            zeroNativeMemoryOnStartup = false,
        )

        nativeMemoryAllocator.baseNativeMemoryPointer() shouldBe mockNativeMemoryPointer
        nativeMemoryAllocator.numFreePages shouldBe expectedNumPages
        nativeMemoryAllocator.totalNumPages shouldBe expectedNumPages
        nativeMemoryAllocator.numUsedPages shouldBe 0
        nativeMemoryAllocator.numAllocationExceptions shouldBe 0
        nativeMemoryAllocator.numFreeExceptions shouldBe 0

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

        nativeMemoryAllocator.nativeMemoryAllocatorMetadata shouldBe expectedNativeMemoryAllocatorMetadata

        verify(exactly = 1) {
            mockUnsafe.allocateMemory(nativeMemorySizeBytes)
        }

        verify(exactly = 0) {
            mockUnsafe.setMemory(any(), any(), any())
        }
    }

    @Test
    fun `test initialization with zeroNativeMemoryOnStartup = true`() {
        val pageSizeBytes = 4_096 // 4kb
        val nativeMemorySizeBytes = 1L * 1024 * 1024 * 1024 // 1gb
        val expectedNumPages = (nativeMemorySizeBytes / pageSizeBytes).toInt()
        val mockNativeMemoryPointer = 0x80000000

        every {
            mockUnsafe.allocateMemory(nativeMemorySizeBytes)
        } returns mockNativeMemoryPointer

        every {
            mockUnsafe.setMemory(mockNativeMemoryPointer, nativeMemorySizeBytes, 0)
        } returns Unit

        val nativeMemoryAllocator = NativeMemoryAllocatorImpl(
            pageSizeBytes = pageSizeBytes,
            nativeMemorySizeBytes = nativeMemorySizeBytes,
            zeroNativeMemoryOnStartup = true,
        )

        nativeMemoryAllocator.baseNativeMemoryPointer() shouldBe mockNativeMemoryPointer
        nativeMemoryAllocator.numFreePages shouldBe expectedNumPages
        nativeMemoryAllocator.totalNumPages shouldBe expectedNumPages
        nativeMemoryAllocator.numUsedPages shouldBe 0
        nativeMemoryAllocator.numAllocationExceptions shouldBe 0
        nativeMemoryAllocator.numFreeExceptions shouldBe 0

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

        nativeMemoryAllocator.nativeMemoryAllocatorMetadata shouldBe expectedNativeMemoryAllocatorMetadata

        verify(exactly = 1) {
            mockUnsafe.allocateMemory(nativeMemorySizeBytes)
        }

        verify(exactly = 1) {
            mockUnsafe.setMemory(mockNativeMemoryPointer, nativeMemorySizeBytes, 0)
        }
    }

    @Test
    fun `test initialization invalid nativeMemorySizeBytes`() {
        val pageSizeBytes = 4_096 // 4kb
        val nativeMemorySizeBytes = (1L * 1024 * 1024 * 1024) + 1 // 1gb + 1 byte
        val mockNativeMemoryPointer = 0x80000000

        every {
            mockUnsafe.allocateMemory(any())
        } returns mockNativeMemoryPointer

        shouldThrow<IllegalArgumentException> {
            NativeMemoryAllocatorImpl(
                pageSizeBytes = pageSizeBytes,
                nativeMemorySizeBytes = nativeMemorySizeBytes,
                zeroNativeMemoryOnStartup = false,
            )
        }

        verify(exactly = 0) {
            mockUnsafe.allocateMemory(any())
        }
    }

    @Test
    fun `test allocation of 100 bytes, then free, then double free, then resize freed buffer`() {
        val pageSizeBytes = 4_096 // 4kb
        val nativeMemorySizeBytes = 1L * 1024 * 1024 * 1024 // 1gb
        val totalNumPages = (nativeMemorySizeBytes / pageSizeBytes).toInt()
        val mockNativeMemoryPointer = 0x80000000

        every {
            mockUnsafe.allocateMemory(nativeMemorySizeBytes)
        } returns mockNativeMemoryPointer

        val nativeMemoryAllocator = NativeMemoryAllocatorImpl(
            pageSizeBytes = pageSizeBytes,
            nativeMemorySizeBytes = nativeMemorySizeBytes,
            zeroNativeMemoryOnStartup = false,
        )

        val buffer = nativeMemoryAllocator.allocateNativeMemoryBuffer(
            capacityBytes = 100,
        )

        verify(exactly = 1) {
            mockUnsafe.allocateMemory(nativeMemorySizeBytes)
        }

        nativeMemoryAllocator.baseNativeMemoryPointer() shouldBe mockNativeMemoryPointer
        nativeMemoryAllocator.numFreePages shouldBe totalNumPages - 1
        nativeMemoryAllocator.totalNumPages shouldBe totalNumPages
        nativeMemoryAllocator.numUsedPages shouldBe 1

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

        nativeMemoryAllocator.nativeMemoryAllocatorMetadata shouldBe expectedNativeMemoryAllocatorMetadata

        buffer.pageSizeBytes shouldBe pageSizeBytes
        buffer.capacityBytes shouldBe 100
        buffer.freed shouldBe false
        buffer.numPages shouldBe 1
        nativeMemoryAllocator.numAllocationExceptions shouldBe 0
        nativeMemoryAllocator.numFreeExceptions shouldBe 0

        // Free the buffer
        shouldNotThrowAny {
            nativeMemoryAllocator.freeNativeMemoryBuffer(
                buffer = buffer,
            )
        }

        nativeMemoryAllocator.baseNativeMemoryPointer() shouldBe mockNativeMemoryPointer
        nativeMemoryAllocator.numFreePages shouldBe totalNumPages
        nativeMemoryAllocator.totalNumPages shouldBe totalNumPages
        nativeMemoryAllocator.numUsedPages shouldBe 0
        nativeMemoryAllocator.numAllocationExceptions shouldBe 0
        nativeMemoryAllocator.numFreeExceptions shouldBe 0

        buffer.pageSizeBytes shouldBe pageSizeBytes
        buffer.capacityBytes shouldBe 0
        buffer.freed shouldBe true
        buffer.numPages shouldBe 0

        // Free the buffer again
        shouldThrow<IllegalStateException> {
            nativeMemoryAllocator.freeNativeMemoryBuffer(
                buffer = buffer,
            )
        }

        nativeMemoryAllocator.baseNativeMemoryPointer() shouldBe mockNativeMemoryPointer
        nativeMemoryAllocator.numFreePages shouldBe totalNumPages
        nativeMemoryAllocator.totalNumPages shouldBe totalNumPages
        nativeMemoryAllocator.numUsedPages shouldBe 0
        nativeMemoryAllocator.numAllocationExceptions shouldBe 0
        nativeMemoryAllocator.numFreeExceptions shouldBe 0

        buffer.pageSizeBytes shouldBe pageSizeBytes
        buffer.capacityBytes shouldBe 0
        buffer.freed shouldBe true
        buffer.numPages shouldBe 0
    }

    @Test
    fun `test allocation of 10_000 bytes, then resize scenarios, then free`() {
        val pageSizeBytes = 4_096 // 4kb
        val nativeMemorySizeBytes = 1L * 1024 * 1024 * 1024 // 1gb
        val totalNumPages = (nativeMemorySizeBytes / pageSizeBytes).toInt()
        val mockNativeMemoryPointer = 0x80000000

        every {
            mockUnsafe.allocateMemory(nativeMemorySizeBytes)
        } returns mockNativeMemoryPointer

        val nativeMemoryAllocator = NativeMemoryAllocatorImpl(
            pageSizeBytes = pageSizeBytes,
            nativeMemorySizeBytes = nativeMemorySizeBytes,
            zeroNativeMemoryOnStartup = false,
        )

        val buffer = nativeMemoryAllocator.allocateNativeMemoryBuffer(
            capacityBytes = 10_000,
        )

        verify(exactly = 1) {
            mockUnsafe.allocateMemory(nativeMemorySizeBytes)
        }

        nativeMemoryAllocator.baseNativeMemoryPointer() shouldBe mockNativeMemoryPointer
        nativeMemoryAllocator.numFreePages shouldBe totalNumPages - 3
        nativeMemoryAllocator.totalNumPages shouldBe totalNumPages
        nativeMemoryAllocator.numUsedPages shouldBe 3
        nativeMemoryAllocator.numAllocationExceptions shouldBe 0
        nativeMemoryAllocator.numFreeExceptions shouldBe 0

        buffer.pageSizeBytes shouldBe pageSizeBytes
        buffer.capacityBytes shouldBe 10_000
        buffer.freed shouldBe false
        buffer.numPages shouldBe 3

        // resize to same capacity
        nativeMemoryAllocator.resizeNativeMemoryBuffer(
            buffer = buffer,
            newCapacityBytes = 10_000,
        )

        nativeMemoryAllocator.baseNativeMemoryPointer() shouldBe mockNativeMemoryPointer
        nativeMemoryAllocator.numFreePages shouldBe totalNumPages - 3
        nativeMemoryAllocator.totalNumPages shouldBe totalNumPages
        nativeMemoryAllocator.numUsedPages shouldBe 3
        nativeMemoryAllocator.numAllocationExceptions shouldBe 0
        nativeMemoryAllocator.numFreeExceptions shouldBe 0

        buffer.pageSizeBytes shouldBe pageSizeBytes
        buffer.capacityBytes shouldBe 10_000
        buffer.freed shouldBe false
        buffer.numPages shouldBe 3

        // resize to new capacity, same number of pages
        nativeMemoryAllocator.resizeNativeMemoryBuffer(
            buffer = buffer,
            newCapacityBytes = 10_500,
        )

        nativeMemoryAllocator.baseNativeMemoryPointer() shouldBe mockNativeMemoryPointer
        nativeMemoryAllocator.numFreePages shouldBe totalNumPages - 3
        nativeMemoryAllocator.totalNumPages shouldBe totalNumPages
        nativeMemoryAllocator.numUsedPages shouldBe 3
        nativeMemoryAllocator.numAllocationExceptions shouldBe 0
        nativeMemoryAllocator.numFreeExceptions shouldBe 0

        buffer.pageSizeBytes shouldBe pageSizeBytes
        buffer.capacityBytes shouldBe 10_500
        buffer.freed shouldBe false
        buffer.numPages shouldBe 3

        // resize to new capacity, more pages
        nativeMemoryAllocator.resizeNativeMemoryBuffer(
            buffer = buffer,
            newCapacityBytes = 17_000,
        )

        nativeMemoryAllocator.baseNativeMemoryPointer() shouldBe mockNativeMemoryPointer
        nativeMemoryAllocator.numFreePages shouldBe totalNumPages - 5
        nativeMemoryAllocator.totalNumPages shouldBe totalNumPages
        nativeMemoryAllocator.numUsedPages shouldBe 5
        nativeMemoryAllocator.numAllocationExceptions shouldBe 0
        nativeMemoryAllocator.numFreeExceptions shouldBe 0

        buffer.pageSizeBytes shouldBe pageSizeBytes
        buffer.capacityBytes shouldBe 17_000
        buffer.freed shouldBe false
        buffer.numPages shouldBe 5

        // resize to new capacity, fewer pages
        nativeMemoryAllocator.resizeNativeMemoryBuffer(
            buffer = buffer,
            newCapacityBytes = 4_097,
        )

        nativeMemoryAllocator.baseNativeMemoryPointer() shouldBe mockNativeMemoryPointer
        nativeMemoryAllocator.numFreePages shouldBe totalNumPages - 2
        nativeMemoryAllocator.totalNumPages shouldBe totalNumPages
        nativeMemoryAllocator.numUsedPages shouldBe 2
        nativeMemoryAllocator.numAllocationExceptions shouldBe 0
        nativeMemoryAllocator.numFreeExceptions shouldBe 0

        buffer.pageSizeBytes shouldBe pageSizeBytes
        buffer.capacityBytes shouldBe 4_097
        buffer.freed shouldBe false
        buffer.numPages shouldBe 2

        // free buffer
        nativeMemoryAllocator.freeNativeMemoryBuffer(buffer)

        nativeMemoryAllocator.baseNativeMemoryPointer() shouldBe mockNativeMemoryPointer
        nativeMemoryAllocator.numFreePages shouldBe totalNumPages
        nativeMemoryAllocator.totalNumPages shouldBe totalNumPages
        nativeMemoryAllocator.numUsedPages shouldBe 0
        nativeMemoryAllocator.numAllocationExceptions shouldBe 0
        nativeMemoryAllocator.numFreeExceptions shouldBe 0

        buffer.pageSizeBytes shouldBe pageSizeBytes
        buffer.capacityBytes shouldBe 0
        buffer.freed shouldBe true
        buffer.numPages shouldBe 0
    }

    @Test
    fun `test allocation of 5_000 bytes, then resize out of memory, then free`() {
        val pageSizeBytes = 4_096 // 4kb
        val nativeMemorySizeBytes = 1L * 1024 * 1024 * 1024 // 1gb
        val totalNumPages = (nativeMemorySizeBytes / pageSizeBytes).toInt()
        val mockNativeMemoryPointer = 0x80000000

        every {
            mockUnsafe.allocateMemory(nativeMemorySizeBytes)
        } returns mockNativeMemoryPointer

        val nativeMemoryAllocator = NativeMemoryAllocatorImpl(
            pageSizeBytes = pageSizeBytes,
            nativeMemorySizeBytes = nativeMemorySizeBytes,
            zeroNativeMemoryOnStartup = false,
        )

        val buffer = nativeMemoryAllocator.allocateNativeMemoryBuffer(
            capacityBytes = 5_000,
        )

        verify(exactly = 1) {
            mockUnsafe.allocateMemory(nativeMemorySizeBytes)
        }

        nativeMemoryAllocator.baseNativeMemoryPointer() shouldBe mockNativeMemoryPointer
        nativeMemoryAllocator.numFreePages shouldBe totalNumPages - 2
        nativeMemoryAllocator.totalNumPages shouldBe totalNumPages
        nativeMemoryAllocator.numUsedPages shouldBe 2
        nativeMemoryAllocator.numAllocationExceptions shouldBe 0
        nativeMemoryAllocator.numFreeExceptions shouldBe 0

        buffer.pageSizeBytes shouldBe pageSizeBytes
        buffer.capacityBytes shouldBe 5_000
        buffer.freed shouldBe false
        buffer.numPages shouldBe 2

        // resize buffer to max capacity
        nativeMemoryAllocator.resizeNativeMemoryBuffer(
            buffer = buffer,
            newCapacityBytes = nativeMemorySizeBytes.toInt(), //1gb
        )

        nativeMemoryAllocator.baseNativeMemoryPointer() shouldBe mockNativeMemoryPointer
        nativeMemoryAllocator.numFreePages shouldBe 0
        nativeMemoryAllocator.totalNumPages shouldBe totalNumPages
        nativeMemoryAllocator.numUsedPages shouldBe totalNumPages
        nativeMemoryAllocator.numAllocationExceptions shouldBe 0
        nativeMemoryAllocator.numFreeExceptions shouldBe 0

        buffer.pageSizeBytes shouldBe pageSizeBytes
        buffer.capacityBytes shouldBe nativeMemorySizeBytes.toInt()
        buffer.freed shouldBe false
        buffer.numPages shouldBe totalNumPages

        // resize buffer to 1 page
        nativeMemoryAllocator.resizeNativeMemoryBuffer(
            buffer = buffer,
            newCapacityBytes = 4_096,
        )

        nativeMemoryAllocator.baseNativeMemoryPointer() shouldBe mockNativeMemoryPointer
        nativeMemoryAllocator.numFreePages shouldBe totalNumPages - 1
        nativeMemoryAllocator.totalNumPages shouldBe totalNumPages
        nativeMemoryAllocator.numUsedPages shouldBe 1
        nativeMemoryAllocator.numAllocationExceptions shouldBe 0
        nativeMemoryAllocator.numFreeExceptions shouldBe 0

        buffer.pageSizeBytes shouldBe pageSizeBytes
        buffer.capacityBytes shouldBe 4_096
        buffer.freed shouldBe false
        buffer.numPages shouldBe 1

        // resize buffer 1 byte more than max capacity
        shouldThrow<IllegalStateException> {
            nativeMemoryAllocator.resizeNativeMemoryBuffer(
                buffer = buffer,
                newCapacityBytes = nativeMemorySizeBytes.toInt() + 1,
            )
        }

        nativeMemoryAllocator.baseNativeMemoryPointer() shouldBe mockNativeMemoryPointer
        nativeMemoryAllocator.numFreePages shouldBe totalNumPages - 1
        nativeMemoryAllocator.totalNumPages shouldBe totalNumPages
        nativeMemoryAllocator.numUsedPages shouldBe 1
        nativeMemoryAllocator.numAllocationExceptions shouldBe 1
        nativeMemoryAllocator.numFreeExceptions shouldBe 0

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

        nativeMemoryAllocator.nativeMemoryAllocatorMetadata shouldBe expectedNativeMemoryAllocatorMetadata

        buffer.pageSizeBytes shouldBe pageSizeBytes
        buffer.capacityBytes shouldBe 4_096
        buffer.freed shouldBe false
        buffer.numPages shouldBe 1

        // free buffer
        nativeMemoryAllocator.freeNativeMemoryBuffer(
            buffer = buffer,
        )

        nativeMemoryAllocator.baseNativeMemoryPointer() shouldBe mockNativeMemoryPointer
        nativeMemoryAllocator.numFreePages shouldBe totalNumPages
        nativeMemoryAllocator.totalNumPages shouldBe totalNumPages
        nativeMemoryAllocator.numUsedPages shouldBe 0
        nativeMemoryAllocator.numAllocationExceptions shouldBe 1
        nativeMemoryAllocator.numFreeExceptions shouldBe 0

        buffer.pageSizeBytes shouldBe pageSizeBytes
        buffer.capacityBytes shouldBe 0
        buffer.freed shouldBe true
        buffer.numPages shouldBe 0
    }

    @Test
    fun `test allocation of negative capacity`() {
        val pageSizeBytes = 4_096 // 4kb
        val nativeMemorySizeBytes = 1L * 1024 * 1024 * 1024 // 1gb
        val totalNumPages = (nativeMemorySizeBytes / pageSizeBytes).toInt()
        val mockNativeMemoryPointer = 0x80000000

        every {
            mockUnsafe.allocateMemory(nativeMemorySizeBytes)
        } returns mockNativeMemoryPointer

        val nativeMemoryAllocator = NativeMemoryAllocatorImpl(
            pageSizeBytes = pageSizeBytes,
            nativeMemorySizeBytes = nativeMemorySizeBytes,
            zeroNativeMemoryOnStartup = false,
        )

        shouldThrow<IllegalArgumentException> {
            nativeMemoryAllocator.allocateNativeMemoryBuffer(
                capacityBytes = -1,
            )
        }

        verify(exactly = 1) {
            mockUnsafe.allocateMemory(nativeMemorySizeBytes)
        }

        nativeMemoryAllocator.baseNativeMemoryPointer() shouldBe mockNativeMemoryPointer
        nativeMemoryAllocator.numFreePages shouldBe totalNumPages
        nativeMemoryAllocator.totalNumPages shouldBe totalNumPages
        nativeMemoryAllocator.numUsedPages shouldBe 0
        nativeMemoryAllocator.numAllocationExceptions shouldBe 0
        nativeMemoryAllocator.numFreeExceptions shouldBe 0
    }

}