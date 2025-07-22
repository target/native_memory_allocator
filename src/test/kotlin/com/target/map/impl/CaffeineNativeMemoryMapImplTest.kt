package com.target.map.impl

import com.github.benmanes.caffeine.cache.RemovalCause
import com.target.nativememoryallocator.allocator.NativeMemoryAllocator
import com.target.nativememoryallocator.buffer.NativeMemoryBuffer
import com.target.nativememoryallocator.map.impl.CaffeineEvictionListener
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class CaffeineNativeMemoryMapImplTest {

    private val mockNativeMemoryAllocator = mockk<NativeMemoryAllocator>()
    private val mockNativeMemoryBuffer = mockk<NativeMemoryBuffer>()

    @AfterEach
    fun afterEach() {
        clearAllMocks()
    }

    @Test
    fun `test CaffeineEvictionListener with RemovalCause EXPLICIT`() {
        val key = "test"

        val caffeineEvictionListener = CaffeineEvictionListener<String>(
            nativeMemoryAllocator = mockNativeMemoryAllocator,
        )

        caffeineEvictionListener.onRemoval(
            key = key,
            value = mockNativeMemoryBuffer,
            cause = RemovalCause.EXPLICIT,
        )

        verify(exactly = 0) {
            mockNativeMemoryBuffer.freed
        }
        verify(exactly = 0) {
            mockNativeMemoryAllocator.freeNativeMemoryBuffer(any())
        }
    }

    @Test
    fun `test CaffeineEvictionListener with RemovalCause EXPIRED`() {
        val key = "test"

        val caffeineEvictionListener = CaffeineEvictionListener<String>(
            nativeMemoryAllocator = mockNativeMemoryAllocator,
        )

        every {
            mockNativeMemoryBuffer.freed
        } returns false

        caffeineEvictionListener.onRemoval(
            key = key,
            value = mockNativeMemoryBuffer,
            cause = RemovalCause.EXPIRED,
        )

        verify(exactly = 1) {
            mockNativeMemoryBuffer.freed
        }
        verify(exactly = 1) {
            mockNativeMemoryAllocator.freeNativeMemoryBuffer(
                buffer = mockNativeMemoryBuffer,
            )
        }
    }

    @Test
    fun `test CaffeineEvictionListener with RemovalCause EXPIRED, buffer is already freed`() {
        val key = "test"

        val caffeineEvictionListener = CaffeineEvictionListener<String>(
            nativeMemoryAllocator = mockNativeMemoryAllocator,
        )

        every {
            mockNativeMemoryBuffer.freed
        } returns true

        caffeineEvictionListener.onRemoval(
            key = key,
            value = mockNativeMemoryBuffer,
            cause = RemovalCause.EXPIRED,
        )

        verify(exactly = 1) {
            mockNativeMemoryBuffer.freed
        }
        verify(exactly = 0) {
            mockNativeMemoryAllocator.freeNativeMemoryBuffer(
                buffer = any(),
            )
        }
    }

    @Test
    fun `test CaffeineEvictionListener with null parameters`() {
        val caffeineEvictionListener = CaffeineEvictionListener<String>(
            nativeMemoryAllocator = mockNativeMemoryAllocator,
        )

        caffeineEvictionListener.onRemoval(
            key = null,
            value = null,
            cause = RemovalCause.EXPLICIT,
        )

        verify(exactly = 0) {
            mockNativeMemoryAllocator.freeNativeMemoryBuffer(
                buffer = any(),
            )
        }
    }
}