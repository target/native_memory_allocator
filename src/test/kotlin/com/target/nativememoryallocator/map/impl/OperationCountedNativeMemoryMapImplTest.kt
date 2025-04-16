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

}