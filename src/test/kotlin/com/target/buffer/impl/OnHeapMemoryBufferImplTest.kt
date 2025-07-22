package com.target.buffer.impl

import com.target.nativememoryallocator.buffer.impl.OnHeapMemoryBufferImpl
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.random.Random

class OnHeapMemoryBufferImplTest {

    @Test
    fun `test initial capacity set to 2 in constructor`() {
        val onHeapMemoryBufferImpl = OnHeapMemoryBufferImpl(
            initialCapacityBytes = 0,
        )

        onHeapMemoryBufferImpl.array.size shouldBe 2
        onHeapMemoryBufferImpl.getReadableBytes() shouldBe 0
    }

    @Test
    fun `test setReadableBytes`() {
        val onHeapMemoryBufferImpl = OnHeapMemoryBufferImpl(
            initialCapacityBytes = 0,
        )

        onHeapMemoryBufferImpl.setReadableBytes(1_023 * 1_024)

        onHeapMemoryBufferImpl.array.size shouldBe 1_024 * 1_024
        onHeapMemoryBufferImpl.getReadableBytes() shouldBe 1_023 * 1_024
    }

    @Test
    fun `test asByteBuffer`() {
        val onHeapMemoryBufferImpl = OnHeapMemoryBufferImpl(
            initialCapacityBytes = 0,
        )

        onHeapMemoryBufferImpl.setReadableBytes((1_024 * 1_024) - 1)

        Random.nextBytes(onHeapMemoryBufferImpl.array)

        onHeapMemoryBufferImpl.array.size shouldBe 1_024 * 1_024
        onHeapMemoryBufferImpl.getReadableBytes() shouldBe (1_024 * 1_024) - 1

        val byteBuffer = onHeapMemoryBufferImpl.asByteBuffer()

        byteBuffer.capacity() shouldBe 1_024 * 1_024
        byteBuffer.limit() shouldBe (1_024 * 1_024) - 1
        byteBuffer.arrayOffset() shouldBe 0
        onHeapMemoryBufferImpl.array.sliceArray(0 until ((1024 * 1024) - 1)) shouldBe
                onHeapMemoryBufferImpl.asByteBuffer().array().sliceArray(0 until byteBuffer.limit())
    }
}