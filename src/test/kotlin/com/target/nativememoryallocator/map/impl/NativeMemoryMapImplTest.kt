package com.target.nativememoryallocator.map.impl

import com.target.nativememoryallocator.allocator.NativeMemoryAllocator
import com.target.nativememoryallocator.buffer.NativeMemoryBuffer
import com.target.nativememoryallocator.map.NativeMemoryMap
import com.target.nativememoryallocator.map.NativeMemoryMapSerializer
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.util.AbstractMap
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class NativeMemoryMapImplTest {

    private class TestValueObject

    private val mockTestValueObjectNativeMemoryMapSerializer = mockk<NativeMemoryMapSerializer<TestValueObject>>()

    private val mockNativeMemoryAllocator = mockk<NativeMemoryAllocator>()

    private val mockNativeMemoryBuffer = mockk<NativeMemoryBuffer>()

    @AfterEach
    fun afterEach() {
        clearAllMocks()
    }

    @Test
    fun `construct NativeMemoryMapImpl`() {
        val nativeMemoryMap = NativeMemoryMapImpl(
            valueSerializer = mockTestValueObjectNativeMemoryMapSerializer,
            nativeMemoryAllocator = mockNativeMemoryAllocator,
            useThreadLocalOnHeapReadBuffer = true,
            threadLocalOnHeapReadBufferInitialCapacityBytes = (256 * 1024),
            cacheMap = ConcurrentHashMap(),
        )

        nativeMemoryMap.entries.isEmpty() shouldBe true
        nativeMemoryMap.keys.isEmpty() shouldBe true
        nativeMemoryMap.size shouldBe 0
    }

    @Test
    fun `test put of null value`() {
        val nativeMemoryMap = NativeMemoryMapImpl(
            valueSerializer = mockTestValueObjectNativeMemoryMapSerializer,
            nativeMemoryAllocator = mockNativeMemoryAllocator,
            useThreadLocalOnHeapReadBuffer = true,
            threadLocalOnHeapReadBufferInitialCapacityBytes = (256 * 1024),
            cacheMap = ConcurrentHashMap(),
        )

        val putResult = nativeMemoryMap.put(key = 1, value = null)

        putResult shouldBe NativeMemoryMap.PutResult.NO_CHANGE
        nativeMemoryMap.entries.isEmpty() shouldBe true
        nativeMemoryMap.keys.isEmpty() shouldBe true
        nativeMemoryMap.size shouldBe 0
    }

    @Test
    fun `test put`() {
        val serializedValue = ByteArray(10)
        Random.nextBytes(serializedValue)

        val putValue = mockk<TestValueObject>()

        val nativeMemoryMap = NativeMemoryMapImpl(
            valueSerializer = mockTestValueObjectNativeMemoryMapSerializer,
            nativeMemoryAllocator = mockNativeMemoryAllocator,
            useThreadLocalOnHeapReadBuffer = true,
            threadLocalOnHeapReadBufferInitialCapacityBytes = (256 * 1024),
            cacheMap = ConcurrentHashMap(),
        )

        every {
            mockTestValueObjectNativeMemoryMapSerializer.serializeToByteArray(value = putValue)
        } returns serializedValue

        every {
            mockNativeMemoryAllocator.allocateNativeMemoryBuffer(capacityBytes = 10)
        } returns mockNativeMemoryBuffer

        every {
            mockNativeMemoryBuffer.copyFromArray(byteArray = serializedValue)
        } returns Unit

        val putResult = nativeMemoryMap.put(key = 1, value = putValue)

        putResult shouldBe NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER
        nativeMemoryMap.entries shouldBe setOf(
            AbstractMap.SimpleEntry(
                1,
                mockNativeMemoryBuffer,
            ),
        )
        nativeMemoryMap.keys shouldBe setOf(1)
        nativeMemoryMap.size shouldBe 1

        verify(exactly = 1) {
            mockTestValueObjectNativeMemoryMapSerializer.serializeToByteArray(value = putValue)
        }
        verify(exactly = 1) {
            mockNativeMemoryAllocator.allocateNativeMemoryBuffer(capacityBytes = 10)
        }
        verify(exactly = 1) {
            mockNativeMemoryBuffer.copyFromArray(byteArray = serializedValue)
        }
    }
}