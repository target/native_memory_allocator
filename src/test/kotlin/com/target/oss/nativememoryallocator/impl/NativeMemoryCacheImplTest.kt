package com.target.oss.nativememoryallocator.impl

import com.target.oss.nativememoryallocator.*
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import java.util.*
import java.util.concurrent.ThreadLocalRandom

private class TestValueObject

class NativeMemoryCacheImplTest : Spek({
    Feature("NativeMemoryCacheImpl") {
        Scenario("test initialization") {
            lateinit var testValueObjectNativeMemoryCacheSerializer: NativeMemoryCacheSerializer<TestValueObject>
            lateinit var nativeMemoryAllocator: NativeMemoryAllocator
            lateinit var nativeMemoryCache: NativeMemoryCacheImpl<Int, TestValueObject>

            When("construct NativeMemoryCacheImpl") {
                testValueObjectNativeMemoryCacheSerializer = mockk()
                nativeMemoryAllocator = mockk()
                nativeMemoryCache = NativeMemoryCacheImpl(
                    valueSerializer = testValueObjectNativeMemoryCacheSerializer,
                    nativeMemoryAllocator = nativeMemoryAllocator,
                    useThreadLocalOnHeapReadBuffer = true,
                    threadLocalOnHeapReadBufferInitialCapacityBytes = (256 * 1024),
                )
            }
            Then("initial state is correct") {
                assertTrue(nativeMemoryCache.entries.isEmpty())
                assertEquals(0, nativeMemoryCache.size)
            }
            clearAllMocks()
        }
        Scenario("test put") {
            lateinit var testValueObjectNativeMemoryCacheSerializer: NativeMemoryCacheSerializer<TestValueObject>
            lateinit var nativeMemoryAllocator: NativeMemoryAllocator
            lateinit var putValue: TestValueObject
            val serializedValue = ByteArray(10)
            ThreadLocalRandom.current().nextBytes(serializedValue)
            lateinit var nativeMemoryBuffer: NativeMemoryBuffer
            lateinit var nativeMemoryCache: NativeMemoryCacheImpl<Int, TestValueObject>
            lateinit var putResult: NativeMemoryCache.PutResult

            When("test single put") {
                testValueObjectNativeMemoryCacheSerializer = mockk()
                nativeMemoryAllocator = mockk()
                putValue = mockk()
                nativeMemoryBuffer = mockk()

                nativeMemoryCache = NativeMemoryCacheImpl(
                    valueSerializer = testValueObjectNativeMemoryCacheSerializer,
                    nativeMemoryAllocator = nativeMemoryAllocator,
                    useThreadLocalOnHeapReadBuffer = true,
                    threadLocalOnHeapReadBufferInitialCapacityBytes = (256 * 1024),
                )

                every {
                    testValueObjectNativeMemoryCacheSerializer.serializeToByteArray(value = putValue)
                } returns serializedValue

                every {
                    nativeMemoryAllocator.allocateNativeMemoryBuffer(capacityBytes = 10)
                } returns nativeMemoryBuffer

                every {
                    nativeMemoryBuffer.copyFromArray(byteArray = serializedValue)
                } returns Unit

                putResult = nativeMemoryCache.put(key = 1, value = putValue)
            }
            Then("state is correct") {
                assertEquals(NativeMemoryCache.PutResult.ALLOCATED_NEW_BUFFER, putResult)
                assertEquals(
                    setOf(
                        AbstractMap.SimpleEntry(
                            1,
                            nativeMemoryBuffer
                        )
                    ),
                    nativeMemoryCache.entries
                )
                assertEquals(1, nativeMemoryCache.size)

                verify(exactly = 1) {
                    testValueObjectNativeMemoryCacheSerializer.serializeToByteArray(value = putValue)
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
            lateinit var testValueObjectNativeMemoryCacheSerializer: NativeMemoryCacheSerializer<TestValueObject>
            lateinit var nativeMemoryAllocator: NativeMemoryAllocator
            lateinit var putValue: TestValueObject
            val serializedValue = ByteArray(10)
            ThreadLocalRandom.current().nextBytes(serializedValue)
            lateinit var nativeMemoryBuffer: NativeMemoryBuffer
            lateinit var threadLocalReadBuffer: OnHeapMemoryBuffer
            lateinit var getDeserializedValue: TestValueObject
            lateinit var nativeMemoryCache: NativeMemoryCacheImpl<Int, TestValueObject>
            lateinit var putResult: NativeMemoryCache.PutResult
            var getResult: TestValueObject? = null

            When("test single put then get") {
                testValueObjectNativeMemoryCacheSerializer = mockk()
                nativeMemoryAllocator = mockk()
                putValue = mockk()
                nativeMemoryBuffer = mockk()
                threadLocalReadBuffer = mockk()
                getDeserializedValue = mockk()

                nativeMemoryCache = NativeMemoryCacheImpl(
                    valueSerializer = testValueObjectNativeMemoryCacheSerializer,
                    nativeMemoryAllocator = nativeMemoryAllocator,
                    useThreadLocalOnHeapReadBuffer = true,
                    threadLocalOnHeapReadBufferInitialCapacityBytes = (256 * 1024),
                )

                every {
                    testValueObjectNativeMemoryCacheSerializer.serializeToByteArray(value = putValue)
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
                    testValueObjectNativeMemoryCacheSerializer.deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer = threadLocalReadBuffer)
                } returns getDeserializedValue

                putResult = nativeMemoryCache.put(key = 1, value = putValue)

                nativeMemoryCache.threadLocalHeapReadBuffer!!.set(threadLocalReadBuffer)

                getResult = nativeMemoryCache.get(key = 1)
            }
            Then("state is correct") {
                assertEquals(NativeMemoryCache.PutResult.ALLOCATED_NEW_BUFFER, putResult)
                assertEquals(
                    setOf(
                        AbstractMap.SimpleEntry(
                            1,
                            nativeMemoryBuffer
                        )
                    ),
                    nativeMemoryCache.entries
                )
                assertEquals(1, nativeMemoryCache.size)
                assertEquals(getDeserializedValue, getResult)

                verify(exactly = 1) {
                    testValueObjectNativeMemoryCacheSerializer.serializeToByteArray(value = putValue)
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
                    testValueObjectNativeMemoryCacheSerializer.deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer = threadLocalReadBuffer)
                }
            }
            clearAllMocks()
        }
        Scenario("test put then get useThreadLocalOnHeapReadBuffer = false") {
            lateinit var testValueObjectNativeMemoryCacheSerializer: NativeMemoryCacheSerializer<TestValueObject>
            lateinit var nativeMemoryAllocator: NativeMemoryAllocator
            lateinit var putValue: TestValueObject
            val serializedValue = ByteArray(10)
            ThreadLocalRandom.current().nextBytes(serializedValue)
            lateinit var nativeMemoryBuffer: NativeMemoryBuffer
            lateinit var getDeserializedValue: TestValueObject
            lateinit var onHeapMemoryBuffer: OnHeapMemoryBuffer
            lateinit var nativeMemoryCache: NativeMemoryCacheImpl<Int, TestValueObject>
            lateinit var putResult: NativeMemoryCache.PutResult
            var getResult: TestValueObject? = null

            When("test single put then get") {
                testValueObjectNativeMemoryCacheSerializer = mockk()
                nativeMemoryAllocator = mockk()
                putValue = mockk()
                nativeMemoryBuffer = mockk()
                getDeserializedValue = mockk()
                onHeapMemoryBuffer = mockk()

                mockkObject(OnHeapMemoryBufferFactory)

                nativeMemoryCache = NativeMemoryCacheImpl(
                    valueSerializer = testValueObjectNativeMemoryCacheSerializer,
                    nativeMemoryAllocator = nativeMemoryAllocator,
                    useThreadLocalOnHeapReadBuffer = false,
                )

                every {
                    testValueObjectNativeMemoryCacheSerializer.serializeToByteArray(value = putValue)
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
                    testValueObjectNativeMemoryCacheSerializer.deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer = onHeapMemoryBuffer)
                } returns getDeserializedValue

                putResult = nativeMemoryCache.put(key = 1, value = putValue)

                getResult = nativeMemoryCache.get(key = 1)
            }
            Then("state is correct") {
                assertEquals(NativeMemoryCache.PutResult.ALLOCATED_NEW_BUFFER, putResult)
                assertEquals(
                    setOf(
                        AbstractMap.SimpleEntry(
                            1,
                            nativeMemoryBuffer
                        )
                    ),
                    nativeMemoryCache.entries
                )
                assertEquals(1, nativeMemoryCache.size)
                assertEquals(getDeserializedValue, getResult)

                verify(exactly = 1) {
                    testValueObjectNativeMemoryCacheSerializer.serializeToByteArray(value = putValue)
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
                    testValueObjectNativeMemoryCacheSerializer.deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer = onHeapMemoryBuffer)
                }
            }
            clearAllMocks()
        }
        Scenario("test put reuse buffer") {
            lateinit var testValueObjectNativeMemoryCacheSerializer: NativeMemoryCacheSerializer<TestValueObject>
            lateinit var nativeMemoryAllocator: NativeMemoryAllocator
            lateinit var putValue1: TestValueObject
            lateinit var putValue2: TestValueObject
            val serializedValue1 = ByteArray(10)
            ThreadLocalRandom.current().nextBytes(serializedValue1)
            val serializedValue2 = ByteArray(20)
            ThreadLocalRandom.current().nextBytes(serializedValue2)
            lateinit var nativeMemoryBuffer: NativeMemoryBuffer
            lateinit var getDeserializedValue: TestValueObject
            lateinit var nativeMemoryCache: NativeMemoryCacheImpl<Int, TestValueObject>
            lateinit var putResult1: NativeMemoryCache.PutResult
            lateinit var putResult2: NativeMemoryCache.PutResult
            lateinit var threadLocalReadBuffer: OnHeapMemoryBuffer
            var getResult: TestValueObject? = null

            When("test 2 puts for same key") {
                testValueObjectNativeMemoryCacheSerializer = mockk()
                nativeMemoryAllocator = mockk()
                putValue1 = mockk()
                nativeMemoryBuffer = mockk()
                getDeserializedValue = mockk()
                putValue2 = mockk()
                threadLocalReadBuffer = mockk()

                mockkObject(OnHeapMemoryBufferFactory)

                nativeMemoryCache = NativeMemoryCacheImpl(
                    valueSerializer = testValueObjectNativeMemoryCacheSerializer,
                    nativeMemoryAllocator = nativeMemoryAllocator,
                    useThreadLocalOnHeapReadBuffer = true,
                    threadLocalOnHeapReadBufferInitialCapacityBytes = (256 * 1024),
                )

                every {
                    testValueObjectNativeMemoryCacheSerializer.serializeToByteArray(value = putValue1)
                } returns serializedValue1

                every {
                    testValueObjectNativeMemoryCacheSerializer.serializeToByteArray(value = putValue2)
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

                nativeMemoryCache.threadLocalHeapReadBuffer!!.set(threadLocalReadBuffer)

                every {
                    nativeMemoryBuffer.copyToOnHeapMemoryBuffer(threadLocalReadBuffer)
                } returns Unit

                every {
                    testValueObjectNativeMemoryCacheSerializer.deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer = threadLocalReadBuffer)
                } returns getDeserializedValue

                putResult1 = nativeMemoryCache.put(key = 1, value = putValue1)

                putResult2 = nativeMemoryCache.put(key = 1, value = putValue2)

                getResult = nativeMemoryCache.get(key = 1)
            }
            Then("state is correct") {
                assertEquals(NativeMemoryCache.PutResult.ALLOCATED_NEW_BUFFER, putResult1)
                assertEquals(NativeMemoryCache.PutResult.REUSED_EXISTING_BUFFER, putResult2)
                assertEquals(
                    setOf(
                        AbstractMap.SimpleEntry(
                            1,
                            nativeMemoryBuffer
                        )
                    ),
                    nativeMemoryCache.entries
                )
                assertEquals(1, nativeMemoryCache.size)
                assertEquals(getDeserializedValue, getResult)

                verify(exactly = 1) {
                    testValueObjectNativeMemoryCacheSerializer.serializeToByteArray(value = putValue1)
                }
                verify(exactly = 1) {
                    testValueObjectNativeMemoryCacheSerializer.serializeToByteArray(value = putValue2)
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
                    testValueObjectNativeMemoryCacheSerializer.deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer = threadLocalReadBuffer)
                }
            }
            clearAllMocks()
        }
        Scenario("test put then delete") {
            lateinit var testValueObjectNativeMemoryCacheSerializer: NativeMemoryCacheSerializer<TestValueObject>
            lateinit var nativeMemoryAllocator: NativeMemoryAllocator
            lateinit var putValue: TestValueObject
            val serializedValue = ByteArray(10)
            ThreadLocalRandom.current().nextBytes(serializedValue)
            lateinit var nativeMemoryBuffer: NativeMemoryBuffer
            lateinit var nativeMemoryCache: NativeMemoryCacheImpl<Int, TestValueObject>
            lateinit var putResult1: NativeMemoryCache.PutResult
            lateinit var putResult2: NativeMemoryCache.PutResult

            When("test put value, then put null") {
                testValueObjectNativeMemoryCacheSerializer = mockk()
                nativeMemoryAllocator = mockk()
                putValue = mockk()
                nativeMemoryBuffer = mockk()

                nativeMemoryCache = NativeMemoryCacheImpl(
                    valueSerializer = testValueObjectNativeMemoryCacheSerializer,
                    nativeMemoryAllocator = nativeMemoryAllocator,
                    useThreadLocalOnHeapReadBuffer = true,
                    threadLocalOnHeapReadBufferInitialCapacityBytes = (256 * 1024),
                )

                every {
                    testValueObjectNativeMemoryCacheSerializer.serializeToByteArray(value = putValue)
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

                putResult1 = nativeMemoryCache.put(key = 1, value = putValue)

                putResult2 = nativeMemoryCache.put(key = 1, value = null)
            }
            Then("state is correct") {
                assertEquals(NativeMemoryCache.PutResult.ALLOCATED_NEW_BUFFER, putResult1)
                assertEquals(NativeMemoryCache.PutResult.FREED_CURRENT_BUFFER, putResult2)
                assertTrue(nativeMemoryCache.entries.isEmpty())
                assertEquals(0, nativeMemoryCache.size)

                verify(exactly = 1) {
                    testValueObjectNativeMemoryCacheSerializer.serializeToByteArray(value = putValue)
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