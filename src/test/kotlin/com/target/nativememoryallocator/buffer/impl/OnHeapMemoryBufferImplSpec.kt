package com.target.nativememoryallocator.buffer.impl

import io.mockk.clearAllMocks
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import java.util.concurrent.ThreadLocalRandom

class OnHeapMemoryBufferImplSpec : Spek({
    Feature("OnHeapMemoryBufferImpl") {
        Scenario("test initial capacity set to 2 in constructor") {
            lateinit var onHeapMemoryBufferImpl: OnHeapMemoryBufferImpl

            When("OnHeapMemoryBufferImpl constructor with  initialCapacityBytes = 0") {
                onHeapMemoryBufferImpl = OnHeapMemoryBufferImpl(
                    initialCapacityBytes = 0,
                )
            }
            Then("array size is as expected") {
                assertEquals(2, onHeapMemoryBufferImpl.array.size)
                assertEquals(0, onHeapMemoryBufferImpl.getReadableBytes())
            }
            clearAllMocks()
        }
        Scenario("test setReadableBytes") {
            lateinit var onHeapMemoryBufferImpl: OnHeapMemoryBufferImpl

            Given("") {
                onHeapMemoryBufferImpl = OnHeapMemoryBufferImpl(
                    initialCapacityBytes = 0,
                )
            }
            When("setReadableBytes") {
                onHeapMemoryBufferImpl.setReadableBytes(1023 * 1024)
            }
            Then("value is as expected") {
                assertEquals(1024 * 1024, onHeapMemoryBufferImpl.array.size)
                assertEquals(1023 * 1024, onHeapMemoryBufferImpl.getReadableBytes())
            }
            clearAllMocks()
        }
        Scenario("test asByteBuffer") {
            lateinit var onHeapMemoryBufferImpl: OnHeapMemoryBufferImpl

            Given("") {
                onHeapMemoryBufferImpl = OnHeapMemoryBufferImpl(
                    initialCapacityBytes = 0,
                )
            }
            When("setReadableBytes") {
                onHeapMemoryBufferImpl.setReadableBytes((1024 * 1024) - 1)

                ThreadLocalRandom.current().nextBytes(onHeapMemoryBufferImpl.array)
            }
            Then("value is as expected") {
                assertEquals(1024 * 1024, onHeapMemoryBufferImpl.array.size)
                assertEquals((1024 * 1024) - 1, onHeapMemoryBufferImpl.getReadableBytes())

                val byteBuffer = onHeapMemoryBufferImpl.asByteBuffer()

                assertEquals(1024 * 1024, byteBuffer.capacity())
                assertEquals((1024 * 1024) - 1, byteBuffer.limit())
                assertEquals(0, byteBuffer.arrayOffset())
                assertTrue(onHeapMemoryBufferImpl.array.sliceArray(0 until ((1024 * 1024) - 1))
                    .contentEquals(onHeapMemoryBufferImpl.asByteBuffer().array()
                        .sliceArray(0 until byteBuffer.limit())))
            }
            clearAllMocks()
        }
    }
})