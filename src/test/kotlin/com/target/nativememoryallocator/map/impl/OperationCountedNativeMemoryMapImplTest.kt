package com.target.nativememoryallocator.map.impl

import com.target.nativememoryallocator.map.NativeMemoryMap
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicLong

class OperationCountedNativeMemoryMapImplTest {

    private class TestValueObject

    private val mockNativeMemoryMap = mockk<NativeMemoryMap<Int, TestValueObject>>()

    @AfterEach
    fun afterEach() {
        clearAllMocks()
    }

    @Test
    fun `test OperationCountersImpl counterValuesEqual`() {
        var operationCountersImpl1 = OperationCountersImpl()

        var operationCountersImpl2 = OperationCountersImpl()

        operationCountersImpl1.counterValuesEqual(operationCountersImpl2) shouldBe true

        operationCountersImpl1 = OperationCountersImpl()
        operationCountersImpl1.numPutsNoChange.set(1)

        operationCountersImpl2 = OperationCountersImpl()

        operationCountersImpl1.counterValuesEqual(operationCountersImpl2) shouldBe false

        operationCountersImpl1 = OperationCountersImpl()

        operationCountersImpl2 = OperationCountersImpl()
        operationCountersImpl2.numGetsNonNullValue.set(1)

        operationCountersImpl1.counterValuesEqual(operationCountersImpl2) shouldBe false

    }

    @Test
    fun `test put no change`() {
        val operationCountedNativeMemoryMapImpl = OperationCountedNativeMemoryMapImpl(
            nativeMemoryMap = mockNativeMemoryMap,
        )

        every {
            mockNativeMemoryMap.put(key = 1, value = null)
        } returns NativeMemoryMap.PutResult.NO_CHANGE

        val putResult = operationCountedNativeMemoryMapImpl.put(key = 1, value = null)

        putResult shouldBe NativeMemoryMap.PutResult.NO_CHANGE

        operationCountedNativeMemoryMapImpl.operationCounters.counterValuesEqual(
            OperationCountersImpl(
                numPutsNoChange = AtomicLong(1),
            )
        ) shouldBe true

        verify(exactly = 1) { mockNativeMemoryMap.put(key = 1, value = null) }
    }

    @Test
    fun `test test put freed buffer`() {
        val operationCountedNativeMemoryMapImpl = OperationCountedNativeMemoryMapImpl(
            nativeMemoryMap = mockNativeMemoryMap,
        )

        every {
            mockNativeMemoryMap.put(key = 1, value = null)
        } returns NativeMemoryMap.PutResult.FREED_CURRENT_BUFFER

        val putResult = operationCountedNativeMemoryMapImpl.put(key = 1, value = null)

        putResult shouldBe NativeMemoryMap.PutResult.FREED_CURRENT_BUFFER
        operationCountedNativeMemoryMapImpl.operationCounters.counterValuesEqual(
            OperationCountersImpl(
                numPutsFreedBuffer = AtomicLong(1),
            )
        ) shouldBe true

        verify(exactly = 1) { mockNativeMemoryMap.put(key = 1, value = null) }
    }

    @Test
    fun `test put allocated new buffer`() {
        val operationCountedNativeMemoryMapImpl = OperationCountedNativeMemoryMapImpl(
            nativeMemoryMap = mockNativeMemoryMap,
        )

        val testValueObject = mockk<TestValueObject>()

        every {
            mockNativeMemoryMap.put(key = 1, value = testValueObject)
        } returns NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER

        val putResult = operationCountedNativeMemoryMapImpl.put(key = 1, value = testValueObject)
        putResult shouldBe NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER

        operationCountedNativeMemoryMapImpl.operationCounters.counterValuesEqual(
            OperationCountersImpl(
                numPutsNewBuffer = AtomicLong(1),
            )
        ) shouldBe true

        verify(exactly = 1) { mockNativeMemoryMap.put(key = 1, value = testValueObject) }
    }

    @Test
    fun `test put reused buffer`() {
        val operationCountedNativeMemoryMapImpl = OperationCountedNativeMemoryMapImpl(
            nativeMemoryMap = mockNativeMemoryMap,
        )

        val testValueObject = mockk<TestValueObject>()

        every {
            mockNativeMemoryMap.put(key = 1, value = testValueObject)
        } returns NativeMemoryMap.PutResult.REUSED_EXISTING_BUFFER

        val putResult = operationCountedNativeMemoryMapImpl.put(key = 1, value = testValueObject)

        putResult shouldBe NativeMemoryMap.PutResult.REUSED_EXISTING_BUFFER

        operationCountedNativeMemoryMapImpl.operationCounters.counterValuesEqual(
            OperationCountersImpl(
                numPutsReusedBuffer = AtomicLong(1),
            )
        ) shouldBe true

        verify(exactly = 1) { mockNativeMemoryMap.put(key = 1, value = testValueObject) }
    }

    @Test
    fun `test get returning null`() {
        val operationCountedNativeMemoryMapImpl = OperationCountedNativeMemoryMapImpl(
            nativeMemoryMap = mockNativeMemoryMap,
        )

        every {
            mockNativeMemoryMap.get(key = 1)
        } returns null

        val getResult = operationCountedNativeMemoryMapImpl.get(key = 1)

        getResult shouldBe null

        operationCountedNativeMemoryMapImpl.operationCounters.counterValuesEqual(
            OperationCountersImpl(
                numGetsNullValue = AtomicLong(1),
            )
        ) shouldBe true

        verify(exactly = 1) { mockNativeMemoryMap.get(key = 1) }
    }

    @Test
    fun `test get returning non-null`() {
        val mockResult = mockk<TestValueObject>()

        val operationCountedNativeMemoryMapImpl = OperationCountedNativeMemoryMapImpl(
            nativeMemoryMap = mockNativeMemoryMap,
        )

        every {
            mockNativeMemoryMap.get(key = 1)
        } returns mockResult

        val getResult = operationCountedNativeMemoryMapImpl.get(key = 1)

        getResult shouldBe mockResult

        operationCountedNativeMemoryMapImpl.operationCounters.counterValuesEqual(
            OperationCountersImpl(
                numGetsNonNullValue = AtomicLong(1),
            )
        ) shouldBe true

        verify(exactly = 1) { mockNativeMemoryMap.get(key = 1) }
    }

}