package com.target.buffer.impl

import com.target.nativememoryallocator.buffer.OnHeapMemoryBuffer
import com.target.nativememoryallocator.buffer.impl.NativeMemoryBufferImpl
import com.target.nativememoryallocator.unsafe.NativeMemoryPage
import com.target.nativememoryallocator.unsafe.UnsafeContainer
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

class NativeMemoryBufferImplTest {

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
    fun `readByte(0) reads the correct data at the correct offset`() {
        val pages = ArrayList<NativeMemoryPage>()
        pages.add(NativeMemoryPage(2L * 4_096))
        pages.add(NativeMemoryPage(1L * 4_096))
        pages.add(NativeMemoryPage(0L * 4_096))

        val nativeMemoryBufferImpl = NativeMemoryBufferImpl(
            pageSizeBytes = 4_096,
            capacityBytes = 3 * (4_096),
            freed = false,
            pages = pages,
        )

        every {
            mockUnsafe.getByte(2L * 4_096)
        } returns 123

        val readRetVal = nativeMemoryBufferImpl.readByte(offset = 0)

        readRetVal shouldBe 123.toByte()
        verify(exactly = 1) { mockUnsafe.getByte(2L * 4_096) }
    }

    @Test
    fun `readByte(4095) reads the correct data at the correct offset`() {
        val pages = ArrayList<NativeMemoryPage>()
        pages.add(NativeMemoryPage(2L * 4_096))
        pages.add(NativeMemoryPage(1L * 4_096))
        pages.add(NativeMemoryPage(0L * 4_096))

        val nativeMemoryBufferImpl = NativeMemoryBufferImpl(
            pageSizeBytes = 4_096,
            capacityBytes = 3 * (4_096),
            freed = false,
            pages = pages,
        )

        every {
            mockUnsafe.getByte((2L * 4_096) + 4_095)
        } returns 42

        val readRetVal = nativeMemoryBufferImpl.readByte(offset = 4_095)

        readRetVal shouldBe 42.toByte()
        verify(exactly = 1) { mockUnsafe.getByte((2L * 4_096) + 4_095) }
    }

    @Test
    fun `readByte(8500) reads the correct data at the correct offset`() {
        val pages = ArrayList<NativeMemoryPage>()
        pages.add(NativeMemoryPage(2L * 4_096))
        pages.add(NativeMemoryPage(1L * 4_096))
        pages.add(NativeMemoryPage(0L * 4_096))

        val nativeMemoryBufferImpl = NativeMemoryBufferImpl(
            pageSizeBytes = 4_096,
            capacityBytes = 3 * (4_096),
            freed = false,
            pages = pages,
        )

        every {
            mockUnsafe.getByte((0 * 4_096L) + 308)
        } returns 55

        val readRetVal = nativeMemoryBufferImpl.readByte(offset = 8500)

        readRetVal shouldBe 55.toByte()
        verify(exactly = 1) { mockUnsafe.getByte((0L * 4_096) + 308) }
    }

    @Test
    fun `readByte(12287) reads the correct data at the correct offset`() {
        val pages = ArrayList<NativeMemoryPage>()
        pages.add(NativeMemoryPage(2L * 4_096))
        pages.add(NativeMemoryPage(1L * 4_096))
        pages.add(NativeMemoryPage(0L * 4_096))

        val nativeMemoryBufferImpl = NativeMemoryBufferImpl(
            pageSizeBytes = 4_096,
            capacityBytes = 3 * (4_096),
            freed = false,
            pages = pages,
        )

        every {
            mockUnsafe.getByte((0 * 4_096L) + 4_095)
        } returns 22

        val readRetVal = nativeMemoryBufferImpl.readByte(offset = 12_287)

        readRetVal shouldBe 22.toByte()
        verify(exactly = 1) { mockUnsafe.getByte((0L * 4_096) + 4_095) }
    }

    @Test
    fun `writeByte(0) writes the correct data at the correct offset`() {
        val pages = ArrayList<NativeMemoryPage>()
        pages.add(NativeMemoryPage(2L * 4_096))
        pages.add(NativeMemoryPage(1L * 4_096))
        pages.add(NativeMemoryPage(0L * 4_096))

        val nativeMemoryBufferImpl = NativeMemoryBufferImpl(
            pageSizeBytes = 4_096,
            capacityBytes = 3 * (4_096),
            freed = false,
            pages = pages,
        )

        every {
            mockUnsafe.putByte(2L * 4_096, 42)
        } returns Unit

        nativeMemoryBufferImpl.writeByte(offset = 0, byte = 42)

        verify(exactly = 1) { mockUnsafe.putByte(2L * 4_096, 42) }
    }

    @Test
    fun `writeByte(12287) writes the correct data at the correct offset`() {
        val pages = ArrayList<NativeMemoryPage>()
        pages.add(NativeMemoryPage(2L * 4_096))
        pages.add(NativeMemoryPage(1L * 4_096))
        pages.add(NativeMemoryPage(0L * 4_096))

        val nativeMemoryBufferImpl = NativeMemoryBufferImpl(
            pageSizeBytes = 4_096,
            capacityBytes = 3 * (4_096),
            freed = false,
            pages = pages,
        )

        every {
            mockUnsafe.putByte((0L * 4_096) + 4095, 33)
        } returns Unit

        nativeMemoryBufferImpl.writeByte(offset = 12287, byte = 33)

        verify(exactly = 1) { mockUnsafe.putByte((0L * 4_096) + 4095, 33) }
    }

    @Test
    fun `test readAllToByteArray`() {
        val byteArrayBaseOffset = 16L
        every {
            UnsafeContainer.BYTE_ARRAY_BASE_OFFSET
        } returns byteArrayBaseOffset

        val pages = ArrayList<NativeMemoryPage>()
        pages.add(NativeMemoryPage(2L * 4_096))
        pages.add(NativeMemoryPage(1L * 4_096))
        pages.add(NativeMemoryPage(0L * 4_096))

        val nativeMemoryBufferImpl = NativeMemoryBufferImpl(
            pageSizeBytes = 4_096,
            capacityBytes = 3 * (4_096),
            freed = false,
            pages = pages,
        )

        every {
            mockUnsafe.copyMemory(
                null, pages[0].startAddress,
                any(), byteArrayBaseOffset + 0,
                4_096.toLong()
            )
        } returns Unit

        every {
            mockUnsafe.copyMemory(
                null, pages[1].startAddress,
                any(), byteArrayBaseOffset + 4_096,
                4_096.toLong()
            )
        } returns Unit

        every {
            mockUnsafe.copyMemory(
                null, pages[2].startAddress,
                any(), byteArrayBaseOffset + 8_192,
                4_096.toLong()
            )
        } returns Unit

        nativeMemoryBufferImpl.readAllToByteArray()

        verify(exactly = 1) {
            mockUnsafe.copyMemory(
                null, pages[0].startAddress,
                any(), byteArrayBaseOffset + 0,
                4_096.toLong()
            )
        }
        verify(exactly = 1) {
            mockUnsafe.copyMemory(
                null, pages[1].startAddress,
                any(), byteArrayBaseOffset + 4_096,
                4_096.toLong()
            )
        }
        verify(exactly = 1) {
            mockUnsafe.copyMemory(
                null, pages[2].startAddress,
                any(), byteArrayBaseOffset + 8_192,
                4_096.toLong()
            )
        }
    }

    @Test
    fun `test copyToOnHeapMemoryBuffer`() {
        val byteArrayBaseOffset = 16L
        every {
            UnsafeContainer.BYTE_ARRAY_BASE_OFFSET
        } returns byteArrayBaseOffset

        val pages = ArrayList<NativeMemoryPage>()
        pages.add(NativeMemoryPage(2L * 4_096))
        pages.add(NativeMemoryPage(1L * 4_096))
        pages.add(NativeMemoryPage(0L * 4_096))

        val nativeMemoryBufferImpl = NativeMemoryBufferImpl(
            pageSizeBytes = 4_096,
            capacityBytes = 10_000,
            freed = false,
            pages = pages,
        )

        val array = ByteArray(10_000)

        val onHeapMemoryBuffer = mockk<OnHeapMemoryBuffer>()
        every {
            onHeapMemoryBuffer.array
        } returns array
        every {
            onHeapMemoryBuffer.setReadableBytes(10_000)
        } returns Unit

        every {
            mockUnsafe.copyMemory(
                null, pages[0].startAddress,
                any(), byteArrayBaseOffset + 0,
                4_096.toLong()
            )
        } returns Unit

        every {
            mockUnsafe.copyMemory(
                null, pages[1].startAddress,
                any(), byteArrayBaseOffset + 4_096,
                4_096.toLong()
            )
        } returns Unit

        every {
            mockUnsafe.copyMemory(
                null, pages[2].startAddress,
                any(), byteArrayBaseOffset + 8_192,
                1_808.toLong()
            )
        } returns Unit

        nativeMemoryBufferImpl.copyToOnHeapMemoryBuffer(onHeapMemoryBuffer = onHeapMemoryBuffer)

        verify(exactly = 1) {
            mockUnsafe.copyMemory(
                null, pages[0].startAddress,
                any(), byteArrayBaseOffset + 0,
                4_096.toLong()
            )
        }
        verify(exactly = 1) {
            mockUnsafe.copyMemory(
                null, pages[1].startAddress,
                any(), byteArrayBaseOffset + 4_096,
                4_096.toLong()
            )
        }
        verify(exactly = 1) {
            mockUnsafe.copyMemory(
                null, pages[2].startAddress,
                any(), byteArrayBaseOffset + 8_192,
                1_808.toLong()
            )
        }
        verify(exactly = 1) {
            onHeapMemoryBuffer.setReadableBytes(10_000)
        }
    }

    @Test
    fun `test copyFromArray`() {
        val byteArrayBaseOffset = 16L
        every {
            UnsafeContainer.BYTE_ARRAY_BASE_OFFSET
        } returns byteArrayBaseOffset

        val pages = ArrayList<NativeMemoryPage>()
        pages.add(NativeMemoryPage(2L * 4_096))
        pages.add(NativeMemoryPage(1L * 4_096))
        pages.add(NativeMemoryPage(0L * 4_096))

        val nativeMemoryBufferImpl = NativeMemoryBufferImpl(
            pageSizeBytes = 4_096,
            capacityBytes = 8_193,
            freed = false,
            pages = pages,
        )

        val array = ByteArray(8_193)

        every {
            mockUnsafe.copyMemory(
                array, byteArrayBaseOffset + 0,
                null, pages[0].startAddress,
                4_096.toLong()
            )
        } returns Unit

        every {
            mockUnsafe.copyMemory(
                array, byteArrayBaseOffset + 4_096,
                null, pages[1].startAddress,
                4_096.toLong()
            )
        } returns Unit

        every {
            mockUnsafe.copyMemory(
                array, byteArrayBaseOffset + 8_192,
                null, pages[2].startAddress,
                1.toLong()
            )
        } returns Unit

        nativeMemoryBufferImpl.copyFromArray(array)

        verify(exactly = 1) {
            mockUnsafe.copyMemory(
                array, byteArrayBaseOffset + 0,
                null, pages[0].startAddress,
                4_096.toLong()
            )
        }
        verify(exactly = 1) {
            mockUnsafe.copyMemory(
                array, byteArrayBaseOffset + 4_096,
                null, pages[1].startAddress,
                4_096.toLong()
            )
        }
        verify(exactly = 1) {
            mockUnsafe.copyMemory(
                array, byteArrayBaseOffset + 8_192,
                null, pages[2].startAddress,
                1.toLong()
            )
        }
    }

    @Test
    fun `test copyFromArray array too large`() {
        val byteArrayBaseOffset = 16L
        every {
            UnsafeContainer.BYTE_ARRAY_BASE_OFFSET
        } returns byteArrayBaseOffset

        val pages = ArrayList<NativeMemoryPage>()
        pages.add(NativeMemoryPage(2L * 4_096))
        pages.add(NativeMemoryPage(1L * 4_096))
        pages.add(NativeMemoryPage(0L * 4_096))

        val nativeMemoryBufferImpl = NativeMemoryBufferImpl(
            pageSizeBytes = 4_096,
            capacityBytes = 8_193,
            freed = false,
            pages = pages,
        )

        val array = ByteArray(8_194)

        shouldThrow<IllegalStateException> {
            nativeMemoryBufferImpl.copyFromArray(array)
        }

        verify(exactly = 0) {
            mockUnsafe.copyMemory(
                any(), any(),
                any(), any(),
                any(),
            )
        }

    }
}