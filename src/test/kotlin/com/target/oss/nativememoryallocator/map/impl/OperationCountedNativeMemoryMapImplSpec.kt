package com.target.oss.nativememoryallocator.map.impl

import com.target.oss.nativememoryallocator.map.NativeMemoryMap
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

    // Needed because AtomicLong does not implement equals()
    // See https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/atomic/package-summary.html
    fun OperationCountersImpl.counterValuesEqual(that: OperationCountersImpl): Boolean =
        ((this.numPutsNoChange.toLong() == that.numPutsNoChange.toLong()) &&
                (this.numPutsFreedBuffer.toLong() == that.numPutsFreedBuffer.toLong()) &&
                (this.numPutsReusedBuffer.toLong() == that.numPutsReusedBuffer.toLong()) &&
                (this.numPutsNewBuffer.toLong() == that.numPutsNewBuffer.toLong()) &&
                (this.numDeletesFreedBuffer.toLong() == that.numDeletesFreedBuffer.toLong()) &&
                (this.numDeletesNoChange.toLong() == that.numDeletesNoChange.toLong()) &&
                (this.numGetsNullValue.toLong() == that.numGetsNullValue.toLong()) &&
                (this.numGetsNonNullValue.toLong() == that.numGetsNonNullValue.toLong()))

    Feature("NativeMemoryMapWithOperationCountersImpl") {
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