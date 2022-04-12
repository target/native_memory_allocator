package com.target.nativememoryallocator.map.impl

import com.github.benmanes.caffeine.cache.RemovalCause
import com.target.nativememoryallocator.allocator.NativeMemoryAllocator
import com.target.nativememoryallocator.buffer.NativeMemoryBuffer
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature

class CaffeineNativeMemoryMapImplSpec : Spek({
    Feature("CaffeineNativeMemoryMapImpl") {
        Scenario("test CaffeineEvictionListener with RemovalCause.EXPLICIT") {
            lateinit var nativeMemoryAllocator: NativeMemoryAllocator
            lateinit var key: String
            lateinit var nativeMemoryBuffer: NativeMemoryBuffer
            lateinit var caffeineEvictionListener: CaffeineEvictionListener<String>

            Given("setup test variables") {
                key = "test"
                nativeMemoryAllocator = mockk()
                nativeMemoryBuffer = mockk()

                caffeineEvictionListener = CaffeineEvictionListener(
                    nativeMemoryAllocator = nativeMemoryAllocator,
                )
            }
            When("call CaffeineEvictionListener.onRemoval with RemovalCause.EXPLICIT") {
                caffeineEvictionListener.onRemoval(
                    key = key,
                    value = nativeMemoryBuffer,
                    cause = RemovalCause.EXPLICIT,
                )
            }
            Then("calls are correct") {
                verify(exactly = 0) {
                    nativeMemoryBuffer.freed
                }
                verify(exactly = 0) {
                    nativeMemoryAllocator.freeNativeMemoryBuffer(any())
                }
            }
            clearAllMocks()
        }
        Scenario("test CaffeineEvictionListener with RemovalCause.EXPIRED") {
            lateinit var nativeMemoryAllocator: NativeMemoryAllocator
            lateinit var key: String
            lateinit var nativeMemoryBuffer: NativeMemoryBuffer
            lateinit var caffeineEvictionListener: CaffeineEvictionListener<String>

            Given("setup test variables") {
                key = "test"
                nativeMemoryAllocator = mockk()
                nativeMemoryBuffer = mockk()

                caffeineEvictionListener = CaffeineEvictionListener(
                    nativeMemoryAllocator = nativeMemoryAllocator,
                )

                every {
                    nativeMemoryBuffer.freed
                } returns false
            }
            When("call CaffeineEvictionListener.onRemoval with RemovalCause.EXPIRED") {
                caffeineEvictionListener.onRemoval(
                    key = key,
                    value = nativeMemoryBuffer,
                    cause = RemovalCause.EXPIRED,
                )
            }
            Then("calls are correct") {
                verify(exactly = 1) {
                    nativeMemoryBuffer.freed
                }
                verify(exactly = 1) {
                    nativeMemoryAllocator.freeNativeMemoryBuffer(
                        buffer = nativeMemoryBuffer,
                    )
                }
            }
            clearAllMocks()
        }
        Scenario("test CaffeineEvictionListener with RemovalCause.EXPIRED, buffer is already freed") {
            lateinit var nativeMemoryAllocator: NativeMemoryAllocator
            lateinit var key: String
            lateinit var nativeMemoryBuffer: NativeMemoryBuffer
            lateinit var caffeineEvictionListener: CaffeineEvictionListener<String>

            Given("setup test variables") {
                key = "test"
                nativeMemoryAllocator = mockk()
                nativeMemoryBuffer = mockk()

                caffeineEvictionListener = CaffeineEvictionListener(
                    nativeMemoryAllocator = nativeMemoryAllocator,
                )

                every {
                    nativeMemoryBuffer.freed
                } returns true
            }
            When("call CaffeineEvictionListener.onRemoval with RemovalCause.EXPIRED") {
                caffeineEvictionListener.onRemoval(
                    key = key,
                    value = nativeMemoryBuffer,
                    cause = RemovalCause.EXPIRED,
                )
            }
            Then("calls are correct") {
                verify(exactly = 1) {
                    nativeMemoryBuffer.freed
                }
                verify(exactly = 0) {
                    nativeMemoryAllocator.freeNativeMemoryBuffer(
                        buffer = any(),
                    )
                }
            }
            clearAllMocks()
        }
        Scenario("test CaffeineEvictionListener with null parameters") {
            lateinit var nativeMemoryAllocator: NativeMemoryAllocator
            lateinit var caffeineEvictionListener: CaffeineEvictionListener<String>

            Given("setup test variables") {
                nativeMemoryAllocator = mockk()

                caffeineEvictionListener = CaffeineEvictionListener(
                    nativeMemoryAllocator = nativeMemoryAllocator,
                )
            }
            When("call CaffeineEvictionListener.onRemoval with null parameters") {
                caffeineEvictionListener.onRemoval(
                    key = null,
                    value = null,
                    cause = null,
                )
            }
            Then("calls are correct") {
                verify(exactly = 0) {
                    nativeMemoryAllocator.freeNativeMemoryBuffer(
                        buffer = any(),
                    )
                }
            }
            clearAllMocks()
        }
    }
})