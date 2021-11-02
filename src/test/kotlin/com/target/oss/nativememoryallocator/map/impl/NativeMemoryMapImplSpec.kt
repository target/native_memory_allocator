package com.target.oss.nativememoryallocator.map.impl

import com.target.oss.nativememoryallocator.allocator.NativeMemoryAllocator
import com.target.oss.nativememoryallocator.buffer.NativeMemoryBuffer
import com.target.oss.nativememoryallocator.buffer.OnHeapMemoryBuffer
import com.target.oss.nativememoryallocator.buffer.OnHeapMemoryBufferFactory
import com.target.oss.nativememoryallocator.map.NativeMemoryMap
import com.target.oss.nativememoryallocator.map.NativeMemoryMapSerializer
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom

class NativeMemoryMapImplSpec : Spek({
    class TestValueObject

    Feature("NativeMemoryMapImpl") {
        Scenario("test initialization") {
            lateinit var testValueObjectNativeMemoryMapSerializer: NativeMemoryMapSerializer<TestValueObject>
            lateinit var nativeMemoryAllocator: NativeMemoryAllocator
            lateinit var nativeMemoryMap: NativeMemoryMapImpl<Int, TestValueObject>

            When("construct NativeMemoryMapImpl") {
                testValueObjectNativeMemoryMapSerializer = mockk()
                nativeMemoryAllocator = mockk()
                nativeMemoryMap = NativeMemoryMapImpl(
                    valueSerializer = testValueObjectNativeMemoryMapSerializer,
                    nativeMemoryAllocator = nativeMemoryAllocator,
                    useThreadLocalOnHeapReadBuffer = true,
                    threadLocalOnHeapReadBufferInitialCapacityBytes = (256 * 1024),
                    cacheMap = ConcurrentHashMap(),
                )
            }
            Then("initial state is correct") {
                assertTrue(nativeMemoryMap.entries.isEmpty())
                assertEquals(0, nativeMemoryMap.size)
            }
            clearAllMocks()
        }
        Scenario("test put of null value") {
            lateinit var testValueObjectNativeMemoryMapSerializer: NativeMemoryMapSerializer<TestValueObject>
            lateinit var nativeMemoryAllocator: NativeMemoryAllocator
            lateinit var nativeMemoryMap: NativeMemoryMapImpl<Int, TestValueObject>
            lateinit var putResult: NativeMemoryMap.PutResult

            When("test single put") {
                testValueObjectNativeMemoryMapSerializer = mockk()
                nativeMemoryAllocator = mockk()

                nativeMemoryMap = NativeMemoryMapImpl(
                    valueSerializer = testValueObjectNativeMemoryMapSerializer,
                    nativeMemoryAllocator = nativeMemoryAllocator,
                    useThreadLocalOnHeapReadBuffer = true,
                    threadLocalOnHeapReadBufferInitialCapacityBytes = (256 * 1024),
                    cacheMap = ConcurrentHashMap(),
                )

                putResult = nativeMemoryMap.put(key = 1, value = null)
            }
            Then("state is correct") {
                assertEquals(NativeMemoryMap.PutResult.NO_CHANGE, putResult)
                assertTrue(nativeMemoryMap.entries.isEmpty())
                assertEquals(0, nativeMemoryMap.size)
            }
            clearAllMocks()
        }
        Scenario("test put") {
            lateinit var testValueObjectNativeMemoryMapSerializer: NativeMemoryMapSerializer<TestValueObject>
            lateinit var nativeMemoryAllocator: NativeMemoryAllocator
            lateinit var putValue: TestValueObject
            val serializedValue = ByteArray(10)
            ThreadLocalRandom.current().nextBytes(serializedValue)
            lateinit var nativeMemoryBuffer: NativeMemoryBuffer
            lateinit var nativeMemoryMap: NativeMemoryMapImpl<Int, TestValueObject>
            lateinit var putResult: NativeMemoryMap.PutResult

            When("test single put") {
                testValueObjectNativeMemoryMapSerializer = mockk()
                nativeMemoryAllocator = mockk()
                putValue = mockk()
                nativeMemoryBuffer = mockk()

                nativeMemoryMap = NativeMemoryMapImpl(
                    valueSerializer = testValueObjectNativeMemoryMapSerializer,
                    nativeMemoryAllocator = nativeMemoryAllocator,
                    useThreadLocalOnHeapReadBuffer = true,
                    threadLocalOnHeapReadBufferInitialCapacityBytes = (256 * 1024),
                    cacheMap = ConcurrentHashMap(),
                )

                every {
                    testValueObjectNativeMemoryMapSerializer.serializeToByteArray(value = putValue)
                } returns serializedValue

                every {
                    nativeMemoryAllocator.allocateNativeMemoryBuffer(capacityBytes = 10)
                } returns nativeMemoryBuffer

                every {
                    nativeMemoryBuffer.copyFromArray(byteArray = serializedValue)
                } returns Unit

                putResult = nativeMemoryMap.put(key = 1, value = putValue)
            }
            Then("state is correct") {
                assertEquals(NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER, putResult)
                assertEquals(
                    setOf(
                        AbstractMap.SimpleEntry(
                            1,
                            nativeMemoryBuffer
                        )
                    ),
                    nativeMemoryMap.entries
                )
                assertEquals(1, nativeMemoryMap.size)

                verify(exactly = 1) {
                    testValueObjectNativeMemoryMapSerializer.serializeToByteArray(value = putValue)
                }
                verify(exactly = 1) {
                    nativeMemoryAllocator.allocateNativeMemoryBuffer(capacityBytes = 10)
                }
                verify(exactly = 1) {
                    nativeMemoryBuffer.copyFromArray(byteArray = serializedValue)
                }
            }
            clearAllMocks()
        }
        Scenario("test put then get useThreadLocalOnHeapReadBuffer = true") {
            lateinit var testValueObjectNativeMemoryMapSerializer: NativeMemoryMapSerializer<TestValueObject>
            lateinit var nativeMemoryAllocator: NativeMemoryAllocator
            lateinit var putValue: TestValueObject
            val serializedValue = ByteArray(10)
            ThreadLocalRandom.current().nextBytes(serializedValue)
            lateinit var nativeMemoryBuffer: NativeMemoryBuffer
            lateinit var threadLocalReadBuffer: OnHeapMemoryBuffer
            lateinit var getDeserializedValue: TestValueObject
            lateinit var nativeMemoryMap: NativeMemoryMapImpl<Int, TestValueObject>
            lateinit var putResult: NativeMemoryMap.PutResult
            var getResult: TestValueObject? = null

            When("test single put then get") {
                testValueObjectNativeMemoryMapSerializer = mockk()
                nativeMemoryAllocator = mockk()
                putValue = mockk()
                nativeMemoryBuffer = mockk()
                threadLocalReadBuffer = mockk()
                getDeserializedValue = mockk()

                nativeMemoryMap = NativeMemoryMapImpl(
                    valueSerializer = testValueObjectNativeMemoryMapSerializer,
                    nativeMemoryAllocator = nativeMemoryAllocator,
                    useThreadLocalOnHeapReadBuffer = true,
                    threadLocalOnHeapReadBufferInitialCapacityBytes = (256 * 1024),
                    cacheMap = ConcurrentHashMap(),
                )

                every {
                    testValueObjectNativeMemoryMapSerializer.serializeToByteArray(value = putValue)
                } returns serializedValue

                every {
                    nativeMemoryAllocator.allocateNativeMemoryBuffer(capacityBytes = 10)
                } returns nativeMemoryBuffer

                every {
                    nativeMemoryBuffer.copyFromArray(byteArray = serializedValue)
                } returns Unit

                every {
                    nativeMemoryBuffer.copyToOnHeapMemoryBuffer(threadLocalReadBuffer)
                } returns Unit

                every {
                    testValueObjectNativeMemoryMapSerializer.deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer = threadLocalReadBuffer)
                } returns getDeserializedValue

                putResult = nativeMemoryMap.put(key = 1, value = putValue)

                nativeMemoryMap.threadLocalHeapReadBuffer!!.set(threadLocalReadBuffer)

                getResult = nativeMemoryMap.get(key = 1)
            }
            Then("state is correct") {
                assertEquals(NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER, putResult)
                assertEquals(
                    setOf(
                        AbstractMap.SimpleEntry(
                            1,
                            nativeMemoryBuffer
                        )
                    ),
                    nativeMemoryMap.entries
                )
                assertEquals(1, nativeMemoryMap.size)
                assertEquals(getDeserializedValue, getResult)

                verify(exactly = 1) {
                    testValueObjectNativeMemoryMapSerializer.serializeToByteArray(value = putValue)
                }
                verify(exactly = 1) {
                    nativeMemoryAllocator.allocateNativeMemoryBuffer(capacityBytes = 10)
                }
                verify(exactly = 1) {
                    nativeMemoryBuffer.copyFromArray(byteArray = serializedValue)
                }
                verify(exactly = 1) {
                    nativeMemoryBuffer.copyToOnHeapMemoryBuffer(threadLocalReadBuffer)
                }
                verify(exactly = 1) {
                    testValueObjectNativeMemoryMapSerializer.deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer = threadLocalReadBuffer)
                }
            }
            clearAllMocks()
        }
        Scenario("test put then get useThreadLocalOnHeapReadBuffer = false") {
            lateinit var testValueObjectNativeMemoryMapSerializer: NativeMemoryMapSerializer<TestValueObject>
            lateinit var nativeMemoryAllocator: NativeMemoryAllocator
            lateinit var putValue: TestValueObject
            val serializedValue = ByteArray(10)
            ThreadLocalRandom.current().nextBytes(serializedValue)
            lateinit var nativeMemoryBuffer: NativeMemoryBuffer
            lateinit var getDeserializedValue: TestValueObject
            lateinit var onHeapMemoryBuffer: OnHeapMemoryBuffer
            lateinit var nativeMemoryMap: NativeMemoryMapImpl<Int, TestValueObject>
            lateinit var putResult: NativeMemoryMap.PutResult
            var getResult: TestValueObject? = null

            When("test single put then get") {
                testValueObjectNativeMemoryMapSerializer = mockk()
                nativeMemoryAllocator = mockk()
                putValue = mockk()
                nativeMemoryBuffer = mockk()
                getDeserializedValue = mockk()
                onHeapMemoryBuffer = mockk()

                mockkObject(OnHeapMemoryBufferFactory)

                nativeMemoryMap = NativeMemoryMapImpl(
                    valueSerializer = testValueObjectNativeMemoryMapSerializer,
                    nativeMemoryAllocator = nativeMemoryAllocator,
                    useThreadLocalOnHeapReadBuffer = false,
                    threadLocalOnHeapReadBufferInitialCapacityBytes = 0,
                    cacheMap = ConcurrentHashMap(),
                )

                every {
                    testValueObjectNativeMemoryMapSerializer.serializeToByteArray(value = putValue)
                } returns serializedValue

                every {
                    nativeMemoryAllocator.allocateNativeMemoryBuffer(capacityBytes = 10)
                } returns nativeMemoryBuffer

                every {
                    nativeMemoryBuffer.capacityBytes
                } returns 10

                every {
                    nativeMemoryBuffer.copyFromArray(byteArray = serializedValue)
                } returns Unit

                every {
                    OnHeapMemoryBufferFactory.newOnHeapMemoryBuffer(initialCapacityBytes = 10)
                } returns onHeapMemoryBuffer

                every {
                    nativeMemoryBuffer.copyToOnHeapMemoryBuffer(onHeapMemoryBuffer)
                } returns Unit

                every {
                    testValueObjectNativeMemoryMapSerializer.deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer = onHeapMemoryBuffer)
                } returns getDeserializedValue

                putResult = nativeMemoryMap.put(key = 1, value = putValue)

                getResult = nativeMemoryMap.get(key = 1)
            }
            Then("state is correct") {
                assertEquals(NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER, putResult)
                assertEquals(
                    setOf(
                        AbstractMap.SimpleEntry(
                            1,
                            nativeMemoryBuffer
                        )
                    ),
                    nativeMemoryMap.entries
                )
                assertEquals(1, nativeMemoryMap.size)
                assertEquals(getDeserializedValue, getResult)

                verify(exactly = 1) {
                    testValueObjectNativeMemoryMapSerializer.serializeToByteArray(value = putValue)
                }
                verify(exactly = 1) {
                    nativeMemoryAllocator.allocateNativeMemoryBuffer(capacityBytes = 10)
                }
                verify(exactly = 1) {
                    nativeMemoryBuffer.capacityBytes
                }
                verify(exactly = 1) {
                    nativeMemoryBuffer.copyFromArray(byteArray = serializedValue)
                }
                verify(exactly = 1) {
                    OnHeapMemoryBufferFactory.newOnHeapMemoryBuffer(initialCapacityBytes = 10)
                }
                verify(exactly = 1) {
                    nativeMemoryBuffer.copyToOnHeapMemoryBuffer(onHeapMemoryBuffer)
                }
                verify(exactly = 1) {
                    testValueObjectNativeMemoryMapSerializer.deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer = onHeapMemoryBuffer)
                }
            }
            clearAllMocks()
        }
        Scenario("test put reuse buffer") {
            lateinit var testValueObjectNativeMemoryMapSerializer: NativeMemoryMapSerializer<TestValueObject>
            lateinit var nativeMemoryAllocator: NativeMemoryAllocator
            lateinit var putValue1: TestValueObject
            lateinit var putValue2: TestValueObject
            val serializedValue1 = ByteArray(10)
            ThreadLocalRandom.current().nextBytes(serializedValue1)
            val serializedValue2 = ByteArray(20)
            ThreadLocalRandom.current().nextBytes(serializedValue2)
            lateinit var nativeMemoryBuffer: NativeMemoryBuffer
            lateinit var getDeserializedValue: TestValueObject
            lateinit var nativeMemoryMap: NativeMemoryMapImpl<Int, TestValueObject>
            lateinit var putResult1: NativeMemoryMap.PutResult
            lateinit var putResult2: NativeMemoryMap.PutResult
            lateinit var threadLocalReadBuffer: OnHeapMemoryBuffer
            var getResult: TestValueObject? = null

            When("test 2 puts for same key") {
                testValueObjectNativeMemoryMapSerializer = mockk()
                nativeMemoryAllocator = mockk()
                putValue1 = mockk()
                nativeMemoryBuffer = mockk()
                getDeserializedValue = mockk()
                putValue2 = mockk()
                threadLocalReadBuffer = mockk()

                mockkObject(OnHeapMemoryBufferFactory)

                nativeMemoryMap = NativeMemoryMapImpl(
                    valueSerializer = testValueObjectNativeMemoryMapSerializer,
                    nativeMemoryAllocator = nativeMemoryAllocator,
                    useThreadLocalOnHeapReadBuffer = true,
                    threadLocalOnHeapReadBufferInitialCapacityBytes = (256 * 1024),
                    cacheMap = ConcurrentHashMap(),
                )

                every {
                    testValueObjectNativeMemoryMapSerializer.serializeToByteArray(value = putValue1)
                } returns serializedValue1

                every {
                    testValueObjectNativeMemoryMapSerializer.serializeToByteArray(value = putValue2)
                } returns serializedValue2

                every {
                    nativeMemoryAllocator.allocateNativeMemoryBuffer(capacityBytes = 10)
                } returns nativeMemoryBuffer

                every {
                    nativeMemoryAllocator.resizeNativeMemoryBuffer(
                        buffer = nativeMemoryBuffer,
                        newCapacityBytes = 20
                    )
                } returns Unit

                every {
                    nativeMemoryBuffer.copyFromArray(byteArray = serializedValue1)
                } returns Unit

                every {
                    nativeMemoryBuffer.copyFromArray(byteArray = serializedValue2)
                } returns Unit

                nativeMemoryMap.threadLocalHeapReadBuffer!!.set(threadLocalReadBuffer)

                every {
                    nativeMemoryBuffer.copyToOnHeapMemoryBuffer(threadLocalReadBuffer)
                } returns Unit

                every {
                    testValueObjectNativeMemoryMapSerializer.deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer = threadLocalReadBuffer)
                } returns getDeserializedValue

                putResult1 = nativeMemoryMap.put(key = 1, value = putValue1)

                putResult2 = nativeMemoryMap.put(key = 1, value = putValue2)

                getResult = nativeMemoryMap.get(key = 1)
            }
            Then("state is correct") {
                assertEquals(NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER, putResult1)
                assertEquals(NativeMemoryMap.PutResult.REUSED_EXISTING_BUFFER, putResult2)
                assertEquals(
                    setOf(
                        AbstractMap.SimpleEntry(
                            1,
                            nativeMemoryBuffer
                        )
                    ),
                    nativeMemoryMap.entries
                )
                assertEquals(1, nativeMemoryMap.size)
                assertEquals(getDeserializedValue, getResult)

                verify(exactly = 1) {
                    testValueObjectNativeMemoryMapSerializer.serializeToByteArray(value = putValue1)
                }
                verify(exactly = 1) {
                    testValueObjectNativeMemoryMapSerializer.serializeToByteArray(value = putValue2)
                }
                verify(exactly = 1) {
                    nativeMemoryAllocator.allocateNativeMemoryBuffer(capacityBytes = 10)
                }
                verify(exactly = 1) {
                    nativeMemoryAllocator.resizeNativeMemoryBuffer(
                        buffer = nativeMemoryBuffer,
                        newCapacityBytes = 20
                    )
                }
                verify(exactly = 1) {
                    nativeMemoryBuffer.copyFromArray(byteArray = serializedValue1)
                }
                verify(exactly = 1) {
                    nativeMemoryBuffer.copyFromArray(byteArray = serializedValue2)
                }
                verify(exactly = 1) {
                    nativeMemoryBuffer.copyToOnHeapMemoryBuffer(threadLocalReadBuffer)
                }
                verify(exactly = 1) {
                    testValueObjectNativeMemoryMapSerializer.deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer = threadLocalReadBuffer)
                }
            }
            clearAllMocks()
        }
        Scenario("test put then delete") {
            lateinit var testValueObjectNativeMemoryMapSerializer: NativeMemoryMapSerializer<TestValueObject>
            lateinit var nativeMemoryAllocator: NativeMemoryAllocator
            lateinit var putValue: TestValueObject
            val serializedValue = ByteArray(10)
            ThreadLocalRandom.current().nextBytes(serializedValue)
            lateinit var nativeMemoryBuffer: NativeMemoryBuffer
            lateinit var nativeMemoryMap: NativeMemoryMapImpl<Int, TestValueObject>
            lateinit var putResult1: NativeMemoryMap.PutResult
            lateinit var putResult2: NativeMemoryMap.PutResult

            When("test put value, then put null") {
                testValueObjectNativeMemoryMapSerializer = mockk()
                nativeMemoryAllocator = mockk()
                putValue = mockk()
                nativeMemoryBuffer = mockk()

                nativeMemoryMap = NativeMemoryMapImpl(
                    valueSerializer = testValueObjectNativeMemoryMapSerializer,
                    nativeMemoryAllocator = nativeMemoryAllocator,
                    useThreadLocalOnHeapReadBuffer = true,
                    threadLocalOnHeapReadBufferInitialCapacityBytes = (256 * 1024),
                    cacheMap = ConcurrentHashMap(),
                )

                every {
                    testValueObjectNativeMemoryMapSerializer.serializeToByteArray(value = putValue)
                } returns serializedValue

                every {
                    nativeMemoryAllocator.allocateNativeMemoryBuffer(capacityBytes = 10)
                } returns nativeMemoryBuffer

                every {
                    nativeMemoryBuffer.copyFromArray(byteArray = serializedValue)
                } returns Unit

                every {
                    nativeMemoryAllocator.freeNativeMemoryBuffer(buffer = nativeMemoryBuffer)
                } returns Unit

                putResult1 = nativeMemoryMap.put(key = 1, value = putValue)

                putResult2 = nativeMemoryMap.put(key = 1, value = null)
            }
            Then("state is correct") {
                assertEquals(NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER, putResult1)
                assertEquals(NativeMemoryMap.PutResult.FREED_CURRENT_BUFFER, putResult2)
                assertTrue(nativeMemoryMap.entries.isEmpty())
                assertEquals(0, nativeMemoryMap.size)

                verify(exactly = 1) {
                    testValueObjectNativeMemoryMapSerializer.serializeToByteArray(value = putValue)
                }
                verify(exactly = 1) {
                    nativeMemoryAllocator.allocateNativeMemoryBuffer(capacityBytes = 10)
                }
                verify(exactly = 1) {
                    nativeMemoryBuffer.copyFromArray(byteArray = serializedValue)
                }
                verify(exactly = 1) {
                    nativeMemoryAllocator.freeNativeMemoryBuffer(buffer = nativeMemoryBuffer)
                }
            }
            clearAllMocks()
        }
    }
})