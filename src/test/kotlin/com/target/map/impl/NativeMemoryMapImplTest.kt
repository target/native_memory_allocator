package com.target.map.impl

import com.target.nativememoryallocator.allocator.NativeMemoryAllocator
import com.target.nativememoryallocator.buffer.NativeMemoryBuffer
import com.target.nativememoryallocator.buffer.OnHeapMemoryBuffer
import com.target.nativememoryallocator.buffer.OnHeapMemoryBufferFactory
import com.target.nativememoryallocator.map.NativeMemoryMap
import com.target.nativememoryallocator.map.NativeMemoryMapSerializer
import com.target.nativememoryallocator.map.impl.NativeMemoryMapImpl
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.AbstractMap
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class NativeMemoryMapImplTest {

    private class TestValueObject

    private val mockTestValueObjectNativeMemoryMapSerializer = mockk<NativeMemoryMapSerializer<TestValueObject>>()

    private val mockNativeMemoryAllocator = mockk<NativeMemoryAllocator>()

    private val mockNativeMemoryBuffer = mockk<NativeMemoryBuffer>()

    @BeforeEach
    fun beforeEach() {
        mockkObject(OnHeapMemoryBufferFactory)
    }

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

    @Test
    fun `test put then get useThreadLocalOnHeapReadBuffer = true`() {
        val serializedValue = ByteArray(10)
        Random.nextBytes(serializedValue)

        val threadLocalReadBuffer = mockk<OnHeapMemoryBuffer>()

        val putValue = mockk<TestValueObject>()

        val getDeserializedValue = mockk<TestValueObject>()

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

        every {
            mockNativeMemoryBuffer.copyToOnHeapMemoryBuffer(threadLocalReadBuffer)
        } returns Unit

        every {
            mockTestValueObjectNativeMemoryMapSerializer.deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer = threadLocalReadBuffer)
        } returns getDeserializedValue

        val putResult = nativeMemoryMap.put(key = 1, value = putValue)
        putResult shouldBe NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER

        nativeMemoryMap.threadLocalOnHeapReadBuffer!!.set(threadLocalReadBuffer)

        val getResult = nativeMemoryMap.get(key = 1)
        getResult shouldBe getDeserializedValue

        nativeMemoryMap.entries shouldBe setOf(
            AbstractMap.SimpleEntry(
                1,
                mockNativeMemoryBuffer,
            )
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
        verify(exactly = 1) {
            mockNativeMemoryBuffer.copyToOnHeapMemoryBuffer(threadLocalReadBuffer)
        }
        verify(exactly = 1) {
            mockTestValueObjectNativeMemoryMapSerializer.deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer = threadLocalReadBuffer)
        }
    }


    @Test
    fun `test put then get useThreadLocalOnHeapReadBuffer = false`() {
        val serializedValue = ByteArray(10)
        Random.nextBytes(serializedValue)

        val putValue = mockk<TestValueObject>()

        val getDeserializedValue = mockk<TestValueObject>()

        val onHeapMemoryBuffer = mockk<OnHeapMemoryBuffer>()

        val nativeMemoryMap = NativeMemoryMapImpl(
            valueSerializer = mockTestValueObjectNativeMemoryMapSerializer,
            nativeMemoryAllocator = mockNativeMemoryAllocator,
            useThreadLocalOnHeapReadBuffer = false,
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

        every {
            OnHeapMemoryBufferFactory.newOnHeapMemoryBuffer(initialCapacityBytes = 10)
        } returns onHeapMemoryBuffer

        every {
            mockTestValueObjectNativeMemoryMapSerializer.deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer = onHeapMemoryBuffer)
        } returns getDeserializedValue

        val putResult = nativeMemoryMap.put(key = 1, value = putValue)
        putResult shouldBe NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER

        every {
            mockNativeMemoryBuffer.capacityBytes
        } returns 10

        every {
            mockNativeMemoryBuffer.copyToOnHeapMemoryBuffer(onHeapMemoryBuffer)
        } returns Unit

        val getResult = nativeMemoryMap.get(key = 1)
        getResult shouldBe getDeserializedValue

        nativeMemoryMap.entries shouldBe setOf(
            AbstractMap.SimpleEntry(
                1,
                mockNativeMemoryBuffer,
            )
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
        verify(exactly = 1) {
            OnHeapMemoryBufferFactory.newOnHeapMemoryBuffer(initialCapacityBytes = 10)
        }
        verify(exactly = 1) {
            mockNativeMemoryBuffer.copyToOnHeapMemoryBuffer(onHeapMemoryBuffer)
        }
        verify(exactly = 1) {
            mockTestValueObjectNativeMemoryMapSerializer.deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer = onHeapMemoryBuffer)
        }
    }

    @Test
    fun `test put reuse buffer`() {
        val serializedValue1 = ByteArray(10)
        Random.nextBytes(serializedValue1)

        val serializedValue2 = ByteArray(20)
        Random.nextBytes(serializedValue2)

        val putValue1 = mockk<TestValueObject>()

        val putValue2 = mockk<TestValueObject>()

        val threadLocalReadBuffer = mockk<OnHeapMemoryBuffer>()

        val getDeserializedValue = mockk<TestValueObject>()

        val nativeMemoryMap = NativeMemoryMapImpl(
            valueSerializer = mockTestValueObjectNativeMemoryMapSerializer,
            nativeMemoryAllocator = mockNativeMemoryAllocator,
            useThreadLocalOnHeapReadBuffer = true,
            threadLocalOnHeapReadBufferInitialCapacityBytes = (256 * 1024),
            cacheMap = ConcurrentHashMap(),
        )

        every {
            mockTestValueObjectNativeMemoryMapSerializer.serializeToByteArray(value = putValue1)
        } returns serializedValue1

        every {
            mockTestValueObjectNativeMemoryMapSerializer.serializeToByteArray(value = putValue2)
        } returns serializedValue2

        every {
            mockNativeMemoryAllocator.allocateNativeMemoryBuffer(capacityBytes = 10)
        } returns mockNativeMemoryBuffer

        every {
            mockNativeMemoryAllocator.resizeNativeMemoryBuffer(
                buffer = mockNativeMemoryBuffer,
                newCapacityBytes = 20
            )
        } returns Unit

        every {
            mockNativeMemoryBuffer.copyFromArray(byteArray = serializedValue1)
        } returns Unit

        every {
            mockNativeMemoryBuffer.copyFromArray(byteArray = serializedValue2)
        } returns Unit

        nativeMemoryMap.threadLocalOnHeapReadBuffer!!.set(threadLocalReadBuffer)

        every {
            mockNativeMemoryBuffer.copyToOnHeapMemoryBuffer(threadLocalReadBuffer)
        } returns Unit

        every {
            mockTestValueObjectNativeMemoryMapSerializer.deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer = threadLocalReadBuffer)
        } returns getDeserializedValue

        val putResult1 = nativeMemoryMap.put(key = 1, value = putValue1)
        putResult1 shouldBe NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER

        val putResult2 = nativeMemoryMap.put(key = 1, value = putValue2)
        putResult2 shouldBe NativeMemoryMap.PutResult.REUSED_EXISTING_BUFFER

        val getResult = nativeMemoryMap.get(key = 1)
        getResult shouldBe getDeserializedValue

        nativeMemoryMap.entries shouldBe setOf(
            AbstractMap.SimpleEntry(
                1,
                mockNativeMemoryBuffer,
            ),
        )
        nativeMemoryMap.keys shouldBe setOf(1)
        nativeMemoryMap.size shouldBe 1

        verify(exactly = 1) {
            mockTestValueObjectNativeMemoryMapSerializer.serializeToByteArray(value = putValue1)
        }
        verify(exactly = 1) {
            mockTestValueObjectNativeMemoryMapSerializer.serializeToByteArray(value = putValue2)
        }
        verify(exactly = 1) {
            mockNativeMemoryAllocator.allocateNativeMemoryBuffer(capacityBytes = 10)
        }
        verify(exactly = 1) {
            mockNativeMemoryAllocator.resizeNativeMemoryBuffer(
                buffer = mockNativeMemoryBuffer,
                newCapacityBytes = 20
            )
        }
        verify(exactly = 1) {
            mockNativeMemoryBuffer.copyFromArray(byteArray = serializedValue1)
        }
        verify(exactly = 1) {
            mockNativeMemoryBuffer.copyFromArray(byteArray = serializedValue2)
        }
        verify(exactly = 1) {
            mockNativeMemoryBuffer.copyToOnHeapMemoryBuffer(threadLocalReadBuffer)
        }
        verify(exactly = 1) {
            mockTestValueObjectNativeMemoryMapSerializer.deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer = threadLocalReadBuffer)
        }
    }

    @Test
    fun `test put then delete`() {
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

        every {
            mockNativeMemoryAllocator.freeNativeMemoryBuffer(buffer = mockNativeMemoryBuffer)
        } returns Unit

        val putResult1 = nativeMemoryMap.put(key = 1, value = putValue)
        putResult1 shouldBe NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER

        val putResult2 = nativeMemoryMap.put(key = 1, value = null)
        putResult2 shouldBe NativeMemoryMap.PutResult.FREED_CURRENT_BUFFER

        nativeMemoryMap.entries.isEmpty() shouldBe true
        nativeMemoryMap.keys.isEmpty() shouldBe true
        nativeMemoryMap.size shouldBe 0

        verify(exactly = 1) {
            mockTestValueObjectNativeMemoryMapSerializer.serializeToByteArray(value = putValue)
        }
        verify(exactly = 1) {
            mockNativeMemoryAllocator.allocateNativeMemoryBuffer(capacityBytes = 10)
        }
        verify(exactly = 1) {
            mockNativeMemoryBuffer.copyFromArray(byteArray = serializedValue)
        }
        verify(exactly = 1) {
            mockNativeMemoryAllocator.freeNativeMemoryBuffer(buffer = mockNativeMemoryBuffer)
        }
    }
}