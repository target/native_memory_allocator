package com.target.nativememoryallocator.map.impl

import com.target.nativememoryallocator.map.NativeMemoryMap
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import java.util.concurrent.atomic.AtomicLong

class OperationCountedNativeMemoryMapImplSpec : Spek({
    class TestValueObject

    Feature("NativeMemoryMapWithOperationCountersImpl") {
        Scenario("test OperationCountersImpl.counterValuesEqual") {
            var retVal: Boolean = false

            Given("reset retVal") {
                retVal = false
            }
            When("compare all zero OperationCountersImpl instances") {
                val operationCountersImpl1 = OperationCountersImpl()

                val operationCountersImpl2 = OperationCountersImpl()

                retVal = operationCountersImpl1.counterValuesEqual(operationCountersImpl2)
            }
            Then("retVal is true") {
                assertTrue(retVal)
            }
            Given("reset retVal") {
                retVal = false
            }
            When("compare non equal values") {
                val operationCountersImpl1 = OperationCountersImpl()
                operationCountersImpl1.numPutsNoChange.set(1)

                val operationCountersImpl2 = OperationCountersImpl()

                retVal = operationCountersImpl1.counterValuesEqual(operationCountersImpl2)
            }
            Then("retVal is false") {
                assertFalse(retVal)
            }
            Given("reset retVal") {
                retVal = false
            }
            When("compare non equal values") {
                val operationCountersImpl1 = OperationCountersImpl()

                val operationCountersImpl2 = OperationCountersImpl()
                operationCountersImpl2.numGetsNonNullValue.set(1)

                retVal = operationCountersImpl1.counterValuesEqual(operationCountersImpl2)
            }
            Then("retVal is false") {
                assertFalse(retVal)
            }
        }
        Scenario("test put no change") {
            lateinit var nativeMemoryMap: NativeMemoryMap<Int, TestValueObject>
            lateinit var operationCountedNativeMemoryMapImpl: OperationCountedNativeMemoryMapImpl<Int, TestValueObject>
            lateinit var putResult: NativeMemoryMap.PutResult

            Given("setup variables") {
                nativeMemoryMap = mockk()

                operationCountedNativeMemoryMapImpl = OperationCountedNativeMemoryMapImpl(
                    nativeMemoryMap = nativeMemoryMap,
                )
            }
            When("put operation returns NO_CHANGE") {
                every {
                    nativeMemoryMap.put(key = 1, value = null)
                } returns NativeMemoryMap.PutResult.NO_CHANGE

                putResult = operationCountedNativeMemoryMapImpl.put(key = 1, value = null)
            }
            Then("operationCounters state is correct") {
                assertEquals(NativeMemoryMap.PutResult.NO_CHANGE, putResult)

                assertTrue(
                    operationCountedNativeMemoryMapImpl.operationCounters.counterValuesEqual(
                        OperationCountersImpl(
                            numPutsNoChange = AtomicLong(1),
                        )
                    )
                )

                verify(exactly = 1) { nativeMemoryMap.put(key = 1, value = null) }
            }
            clearAllMocks()
        }
        Scenario("test put freed buffer") {
            lateinit var nativeMemoryMap: NativeMemoryMap<Int, TestValueObject>
            lateinit var operationCountedNativeMemoryMapImpl: OperationCountedNativeMemoryMapImpl<Int, TestValueObject>
            lateinit var putResult: NativeMemoryMap.PutResult

            Given("setup variables") {
                nativeMemoryMap = mockk()

                operationCountedNativeMemoryMapImpl = OperationCountedNativeMemoryMapImpl(
                    nativeMemoryMap = nativeMemoryMap,
                )
            }
            When("put operation returns freed buffer") {
                every {
                    nativeMemoryMap.put(key = 1, value = null)
                } returns NativeMemoryMap.PutResult.FREED_CURRENT_BUFFER

                putResult = operationCountedNativeMemoryMapImpl.put(key = 1, value = null)
            }
            Then("operationCounters state is correct") {
                assertEquals(NativeMemoryMap.PutResult.FREED_CURRENT_BUFFER, putResult)

                assertTrue(
                    operationCountedNativeMemoryMapImpl.operationCounters.counterValuesEqual(
                        OperationCountersImpl(
                            numPutsFreedBuffer = AtomicLong(1),
                        )
                    )
                )

                verify(exactly = 1) { nativeMemoryMap.put(key = 1, value = null) }
            }
            clearAllMocks()
        }
        Scenario("test put allocated new buffer") {
            lateinit var nativeMemoryMap: NativeMemoryMap<Int, TestValueObject>
            lateinit var operationCountedNativeMemoryMapImpl: OperationCountedNativeMemoryMapImpl<Int, TestValueObject>
            lateinit var putResult: NativeMemoryMap.PutResult
            lateinit var testValueObject: TestValueObject

            Given("setup variables") {
                nativeMemoryMap = mockk()

                operationCountedNativeMemoryMapImpl = OperationCountedNativeMemoryMapImpl(
                    nativeMemoryMap = nativeMemoryMap,
                )

                testValueObject = mockk()
            }
            When("put operation returns allocated new buffer") {
                every {
                    nativeMemoryMap.put(key = 1, value = testValueObject)
                } returns NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER

                putResult = operationCountedNativeMemoryMapImpl.put(key = 1, value = testValueObject)
            }
            Then("operationCounters state is correct") {
                assertEquals(NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER, putResult)

                assertTrue(
                    operationCountedNativeMemoryMapImpl.operationCounters.counterValuesEqual(
                        OperationCountersImpl(
                            numPutsNewBuffer = AtomicLong(1),
                        )
                    )
                )

                verify(exactly = 1) { nativeMemoryMap.put(key = 1, value = testValueObject) }
            }
            clearAllMocks()
        }
        Scenario("test put reused buffer") {
            lateinit var nativeMemoryMap: NativeMemoryMap<Int, TestValueObject>
            lateinit var operationCountedNativeMemoryMapImpl: OperationCountedNativeMemoryMapImpl<Int, TestValueObject>
            lateinit var putResult: NativeMemoryMap.PutResult
            lateinit var testValueObject: TestValueObject

            Given("setup variables") {
                nativeMemoryMap = mockk()

                operationCountedNativeMemoryMapImpl = OperationCountedNativeMemoryMapImpl(
                    nativeMemoryMap = nativeMemoryMap,
                )

                testValueObject = mockk()
            }
            When("put operation returns reused buffer") {
                every {
                    nativeMemoryMap.put(key = 1, value = testValueObject)
                } returns NativeMemoryMap.PutResult.REUSED_EXISTING_BUFFER

                putResult = operationCountedNativeMemoryMapImpl.put(key = 1, value = testValueObject)
            }
            Then("operationCounters state is correct") {
                assertEquals(NativeMemoryMap.PutResult.REUSED_EXISTING_BUFFER, putResult)

                assertTrue(
                    operationCountedNativeMemoryMapImpl.operationCounters.counterValuesEqual(
                        OperationCountersImpl(
                            numPutsReusedBuffer = AtomicLong(1),
                        )
                    )
                )

                verify(exactly = 1) { nativeMemoryMap.put(key = 1, value = testValueObject) }
            }
            clearAllMocks()
        }
        Scenario("test get returning null") {
            lateinit var nativeMemoryMap: NativeMemoryMap<Int, TestValueObject>
            lateinit var operationCountedNativeMemoryMapImpl: OperationCountedNativeMemoryMapImpl<Int, TestValueObject>
            var getResult: TestValueObject? = null

            Given("setup variables") {
                nativeMemoryMap = mockk()

                operationCountedNativeMemoryMapImpl = OperationCountedNativeMemoryMapImpl(
                    nativeMemoryMap = nativeMemoryMap,
                )
            }
            When("get operation returns null") {
                every {
                    nativeMemoryMap.get(key = 1)
                } returns null

                getResult = operationCountedNativeMemoryMapImpl.get(key = 1)
            }
            Then("operationCounters state is correct") {
                assertNull(getResult)

                assertTrue(
                    operationCountedNativeMemoryMapImpl.operationCounters.counterValuesEqual(
                        OperationCountersImpl(
                            numGetsNullValue = AtomicLong(1),
                        )
                    )
                )

                verify(exactly = 1) { nativeMemoryMap.get(key = 1) }
            }
            clearAllMocks()
        }
        Scenario("test get returning non-null") {
            lateinit var nativeMemoryMap: NativeMemoryMap<Int, TestValueObject>
            lateinit var operationCountedNativeMemoryMapImpl: OperationCountedNativeMemoryMapImpl<Int, TestValueObject>
            lateinit var mockResult: TestValueObject
            var getResult: TestValueObject? = null

            Given("setup variables") {
                nativeMemoryMap = mockk()
                mockResult = mockk()

                operationCountedNativeMemoryMapImpl = OperationCountedNativeMemoryMapImpl(
                    nativeMemoryMap = nativeMemoryMap,
                )
            }
            When("get operation returns null") {
                every {
                    nativeMemoryMap.get(key = 1)
                } returns mockResult

                getResult = operationCountedNativeMemoryMapImpl.get(key = 1)
            }
            Then("operationCounters state is correct") {
                assertEquals(mockResult, getResult)

                assertTrue(
                    operationCountedNativeMemoryMapImpl.operationCounters.counterValuesEqual(
                        OperationCountersImpl(
                            numGetsNonNullValue = AtomicLong(1),
                        )
                    )
                )

                verify(exactly = 1) { nativeMemoryMap.get(key = 1) }
            }
            clearAllMocks()
        }
        Scenario("test delete freed buffer") {
            lateinit var nativeMemoryMap: NativeMemoryMap<Int, TestValueObject>
            lateinit var operationCountedNativeMemoryMapImpl: OperationCountedNativeMemoryMapImpl<Int, TestValueObject>
            var deleteResult: Boolean? = null

            Given("setup variables") {
                nativeMemoryMap = mockk()

                operationCountedNativeMemoryMapImpl = OperationCountedNativeMemoryMapImpl(
                    nativeMemoryMap = nativeMemoryMap,
                )
            }
            When("delete operation returns true") {
                every {
                    nativeMemoryMap.delete(key = 1)
                } returns true

                deleteResult = operationCountedNativeMemoryMapImpl.delete(key = 1)
            }
            Then("operationCounters state is correct") {
                assertEquals(true, deleteResult)

                assertTrue(
                    operationCountedNativeMemoryMapImpl.operationCounters.counterValuesEqual(
                        OperationCountersImpl(
                            numDeletesFreedBuffer = AtomicLong(1),
                        )
                    )
                )

                verify(exactly = 1) { nativeMemoryMap.delete(key = 1) }
            }
            clearAllMocks()
        }
        Scenario("test delete no change") {
            lateinit var nativeMemoryMap: NativeMemoryMap<Int, TestValueObject>
            lateinit var operationCountedNativeMemoryMapImpl: OperationCountedNativeMemoryMapImpl<Int, TestValueObject>
            var deleteResult: Boolean? = null

            Given("setup variables") {
                nativeMemoryMap = mockk()

                operationCountedNativeMemoryMapImpl = OperationCountedNativeMemoryMapImpl(
                    nativeMemoryMap = nativeMemoryMap,
                )
            }
            When("delete operation returns true") {
                every {
                    nativeMemoryMap.delete(key = 1)
                } returns false

                deleteResult = operationCountedNativeMemoryMapImpl.delete(key = 1)
            }
            Then("operationCounters state is correct") {
                assertEquals(false, deleteResult)

                assertTrue(
                    operationCountedNativeMemoryMapImpl.operationCounters.counterValuesEqual(
                        OperationCountersImpl(
                            numDeletesNoChange = AtomicLong(1),
                        )
                    )
                )

                verify(exactly = 1) { nativeMemoryMap.delete(key = 1) }
            }
            clearAllMocks()
        }
    }
})