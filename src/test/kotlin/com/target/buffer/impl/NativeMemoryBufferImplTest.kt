package com.target.buffer.impl

import com.target.nativememoryallocator.buffer.impl.NativeMemoryBufferImpl
import com.target.nativememoryallocator.unsafe.NativeMemoryPage
import com.target.nativememoryallocator.unsafe.UnsafeContainer
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
}