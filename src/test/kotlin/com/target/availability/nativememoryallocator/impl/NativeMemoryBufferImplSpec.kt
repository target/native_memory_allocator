package com.target.availability.nativememoryallocator.impl

import com.target.availability.nativememoryallocator.OnHeapMemoryBuffer
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import sun.misc.Unsafe

class NativeMemoryBufferImplSpec : Spek({
    Feature("NativeMemoryBufferImpl") {
        Scenario("readByte(0) reads the correct data at the correct offset") {
            lateinit var mockUnsafe: Unsafe
            lateinit var nativeMemoryBufferImpl: NativeMemoryBufferImpl
            lateinit var pages: ArrayList<NativeMemoryPage>
            var readRetVal: Byte = 0

            Given("setup nativeMemoryBufferImpl") {
                mockUnsafe = mockk()
                UnsafeContainer.unsafe = mockUnsafe

                pages = ArrayList()
                pages.add(NativeMemoryPage(2L * 4_096))
                pages.add(NativeMemoryPage(1L * 4_096))
                pages.add(NativeMemoryPage(0L * 4_096))

                nativeMemoryBufferImpl = NativeMemoryBufferImpl(
                    pageSizeBytes = 4_096,
                    capacityBytes = 3 * (4_096),
                    freed = false,
                    pages = pages,
                )

                every {
                    mockUnsafe.getByte(2L * 4_096)
                } returns 123
            }
            When("readByte(0)") {
                readRetVal = nativeMemoryBufferImpl.readByte(offset = 0)
            }
            Then("value is as expected") {
                assertEquals(123.toByte(), readRetVal)
                verify(exactly = 1) { mockUnsafe.getByte(2L * 4_096) }
            }
            clearAllMocks()
            UnsafeContainer.restoreUnsafe()
        }
        Scenario("readByte(4095) reads the correct data at the correct offset") {
            lateinit var mockUnsafe: Unsafe
            lateinit var nativeMemoryBufferImpl: NativeMemoryBufferImpl
            lateinit var pages: ArrayList<NativeMemoryPage>
            var readRetVal: Byte = 0

            Given("setup nativeMemoryBufferImpl") {
                mockUnsafe = mockk()
                UnsafeContainer.unsafe = mockUnsafe

                pages = ArrayList()
                pages.add(NativeMemoryPage(2L * 4_096))
                pages.add(NativeMemoryPage(1L * 4_096))
                pages.add(NativeMemoryPage(0L * 4_096))

                nativeMemoryBufferImpl = NativeMemoryBufferImpl(
                    pageSizeBytes = 4_096,
                    capacityBytes = 3 * (4_096),
                    freed = false,
                    pages = pages,
                )

                every {
                    mockUnsafe.getByte((2L * 4_096) + 4_095)
                } returns 42
            }
            When("readByte(4095)") {
                readRetVal = nativeMemoryBufferImpl.readByte(offset = 4_095)
            }
            Then("value is as expected") {
                assertEquals(42.toByte(), readRetVal)
                verify(exactly = 1) { mockUnsafe.getByte((2L * 4_096) + 4_095) }
            }
            clearAllMocks()
            UnsafeContainer.restoreUnsafe()
        }
        Scenario("readByte(8500) reads the correct data at the correct offset") {
            lateinit var mockUnsafe: Unsafe
            lateinit var nativeMemoryBufferImpl: NativeMemoryBufferImpl
            lateinit var pages: ArrayList<NativeMemoryPage>
            var readRetVal: Byte = 0

            Given("setup nativeMemoryBufferImpl") {
                mockUnsafe = mockk()
                UnsafeContainer.unsafe = mockUnsafe

                pages = ArrayList()
                pages.add(NativeMemoryPage(2L * 4_096))
                pages.add(NativeMemoryPage(1L * 4_096))
                pages.add(NativeMemoryPage(0L * 4_096))

                nativeMemoryBufferImpl = NativeMemoryBufferImpl(
                    pageSizeBytes = 4_096,
                    capacityBytes = 3 * (4_096),
                    freed = false,
                    pages = pages,
                )

                every {
                    mockUnsafe.getByte((0 * 4_096L) + 308)
                } returns 55
            }
            When("readByte(8500)") {
                readRetVal = nativeMemoryBufferImpl.readByte(offset = 8500)
            }
            Then("value is as expected") {
                assertEquals(55.toByte(), readRetVal)
                verify(exactly = 1) { mockUnsafe.getByte((0L * 4_096) + 308) }
            }
            clearAllMocks()
            UnsafeContainer.restoreUnsafe()
        }
        Scenario("readByte(12287) reads the correct data at the correct offset") {
            lateinit var mockUnsafe: Unsafe
            lateinit var nativeMemoryBufferImpl: NativeMemoryBufferImpl
            lateinit var pages: ArrayList<NativeMemoryPage>
            var readRetVal: Byte = 0

            Given("setup nativeMemoryBufferImpl") {
                mockUnsafe = mockk()
                UnsafeContainer.unsafe = mockUnsafe

                pages = ArrayList()
                pages.add(NativeMemoryPage(2L * 4_096))
                pages.add(NativeMemoryPage(1L * 4_096))
                pages.add(NativeMemoryPage(0L * 4_096))

                nativeMemoryBufferImpl = NativeMemoryBufferImpl(
                    pageSizeBytes = 4_096,
                    capacityBytes = 3 * (4_096),
                    freed = false,
                    pages = pages,
                )

                every {
                    mockUnsafe.getByte((0L * 4_096) + 4_095)
                } returns 22
            }
            When("readByte(12287)") {
                readRetVal = nativeMemoryBufferImpl.readByte(offset = 12287)
            }
            Then("value is as expected") {
                assertEquals(22.toByte(), readRetVal)
                verify(exactly = 1) { mockUnsafe.getByte((0L * 4_096) + 4_095) }
            }
            clearAllMocks()
            UnsafeContainer.restoreUnsafe()
        }
        Scenario("writeByte(0) writes the correct data at the correct offset") {
            lateinit var mockUnsafe: Unsafe
            lateinit var nativeMemoryBufferImpl: NativeMemoryBufferImpl
            lateinit var pages: ArrayList<NativeMemoryPage>

            Given("setup nativeMemoryBufferImpl") {
                mockUnsafe = mockk()
                UnsafeContainer.unsafe = mockUnsafe

                pages = ArrayList()
                pages.add(NativeMemoryPage(2L * 4_096))
                pages.add(NativeMemoryPage(1L * 4_096))
                pages.add(NativeMemoryPage(0L * 4_096))

                nativeMemoryBufferImpl = NativeMemoryBufferImpl(
                    pageSizeBytes = 4_096,
                    capacityBytes = 3 * (4_096),
                    freed = false,
                    pages = pages,
                )

                every {
                    mockUnsafe.putByte(2L * 4_096, 42)
                } returns Unit
            }
            When("writeByte(0)") {
                nativeMemoryBufferImpl.writeByte(offset = 0, byte = 42)
            }
            Then("value is as expected") {
                verify(exactly = 1) { mockUnsafe.putByte(2L * 4_096, 42) }
            }
            clearAllMocks()
            UnsafeContainer.restoreUnsafe()
        }
        Scenario("writeByte(12287) writes the correct data at the correct offset") {
            lateinit var mockUnsafe: Unsafe
            lateinit var nativeMemoryBufferImpl: NativeMemoryBufferImpl
            lateinit var pages: ArrayList<NativeMemoryPage>

            Given("setup nativeMemoryBufferImpl") {
                mockUnsafe = mockk()
                UnsafeContainer.unsafe = mockUnsafe

                pages = ArrayList()
                pages.add(NativeMemoryPage(2L * 4_096))
                pages.add(NativeMemoryPage(1L * 4_096))
                pages.add(NativeMemoryPage(0L * 4_096))

                nativeMemoryBufferImpl = NativeMemoryBufferImpl(
                    pageSizeBytes = 4_096,
                    capacityBytes = 3 * (4_096),
                    freed = false,
                    pages = pages,
                )

                every {
                    mockUnsafe.putByte((0L * 4_096) + 4095, 33)
                } returns Unit
            }
            When("writeByte(12287)") {
                nativeMemoryBufferImpl.writeByte(offset = 12287, byte = 33)
            }
            Then("value is as expected") {
                verify(exactly = 1) { mockUnsafe.putByte((0L * 4_096) + 4095, 33) }
            }
            clearAllMocks()
            UnsafeContainer.restoreUnsafe()
        }
        Scenario("test readAllToByteArray") {
            lateinit var mockUnsafe: Unsafe
            lateinit var nativeMemoryBufferImpl: NativeMemoryBufferImpl
            lateinit var pages: ArrayList<NativeMemoryPage>

            Given("setup nativeMemoryBufferImpl") {
                mockUnsafe = mockk()
                UnsafeContainer.unsafe = mockUnsafe

                pages = ArrayList()
                pages.add(NativeMemoryPage(2L * 4_096))
                pages.add(NativeMemoryPage(1L * 4_096))
                pages.add(NativeMemoryPage(0L * 4_096))

                nativeMemoryBufferImpl = NativeMemoryBufferImpl(
                    pageSizeBytes = 4_096,
                    capacityBytes = 3 * (4_096),
                    freed = false,
                    pages = pages,
                )

                every {
                    mockUnsafe.copyMemory(
                        null, pages[0].startAddress,
                        any(), UnsafeContainer.BYTE_ARRAY_BASE_OFFSET + 0,
                        4_096.toLong()
                    )
                } returns Unit

                every {
                    mockUnsafe.copyMemory(
                        null, pages[1].startAddress,
                        any(), UnsafeContainer.BYTE_ARRAY_BASE_OFFSET + 4_096,
                        4_096.toLong()
                    )
                } returns Unit

                every {
                    mockUnsafe.copyMemory(
                        null, pages[2].startAddress,
                        any(), UnsafeContainer.BYTE_ARRAY_BASE_OFFSET + 8_192,
                        4_096.toLong()
                    )
                } returns Unit
            }
            When("readAllToByteArray") {
                nativeMemoryBufferImpl.readAllToByteArray()
            }
            Then("calls are as expected") {
                verify(exactly = 1) {
                    mockUnsafe.copyMemory(
                        null, pages[0].startAddress,
                        any(), UnsafeContainer.BYTE_ARRAY_BASE_OFFSET + 0,
                        4_096.toLong()
                    )
                }
                verify(exactly = 1) {
                    mockUnsafe.copyMemory(
                        null, pages[1].startAddress,
                        any(), UnsafeContainer.BYTE_ARRAY_BASE_OFFSET + 4_096,
                        4_096.toLong()
                    )
                }
                verify(exactly = 1) {
                    mockUnsafe.copyMemory(
                        null, pages[2].startAddress,
                        any(), UnsafeContainer.BYTE_ARRAY_BASE_OFFSET + 8_192,
                        4_096.toLong()
                    )
                }
            }
            clearAllMocks()
            UnsafeContainer.restoreUnsafe()
        }
        Scenario("test copyToOnHeapMemoryBuffer") {
            lateinit var mockUnsafe: Unsafe
            lateinit var nativeMemoryBufferImpl: NativeMemoryBufferImpl
            lateinit var pages: ArrayList<NativeMemoryPage>
            lateinit var onHeapMemoryBuffer: OnHeapMemoryBuffer

            Given("setup nativeMemoryBufferImpl") {
                mockUnsafe = mockk()
                UnsafeContainer.unsafe = mockUnsafe

                pages = ArrayList()
                pages.add(NativeMemoryPage(2L * 4_096))
                pages.add(NativeMemoryPage(1L * 4_096))
                pages.add(NativeMemoryPage(0L * 4_096))

                nativeMemoryBufferImpl = NativeMemoryBufferImpl(
                    pageSizeBytes = 4_096,
                    capacityBytes = 10_000,
                    freed = false,
                    pages = pages,
                )

                val array = ByteArray(10_000)

                onHeapMemoryBuffer = mockk()
                every {
                    onHeapMemoryBuffer.array
                } returns array
                every {
                    onHeapMemoryBuffer.setReadableBytes(10_000)
                } returns Unit

                every {
                    mockUnsafe.copyMemory(
                        null, pages[0].startAddress,
                        any(), UnsafeContainer.BYTE_ARRAY_BASE_OFFSET + 0,
                        4_096.toLong()
                    )
                } returns Unit

                every {
                    mockUnsafe.copyMemory(
                        null, pages[1].startAddress,
                        any(), UnsafeContainer.BYTE_ARRAY_BASE_OFFSET + 4_096,
                        4_096.toLong()
                    )
                } returns Unit

                every {
                    mockUnsafe.copyMemory(
                        null, pages[2].startAddress,
                        any(), UnsafeContainer.BYTE_ARRAY_BASE_OFFSET + 8_192,
                        1_808.toLong()
                    )
                } returns Unit
            }
            When("copyToOnHeapMemoryBuffer") {
                nativeMemoryBufferImpl.copyToOnHeapMemoryBuffer(onHeapMemoryBuffer = onHeapMemoryBuffer)
            }
            Then("calls are as expected") {
                verify(exactly = 1) {
                    mockUnsafe.copyMemory(
                        null, pages[0].startAddress,
                        any(), UnsafeContainer.BYTE_ARRAY_BASE_OFFSET + 0,
                        4_096.toLong()
                    )
                }
                verify(exactly = 1) {
                    mockUnsafe.copyMemory(
                        null, pages[1].startAddress,
                        any(), UnsafeContainer.BYTE_ARRAY_BASE_OFFSET + 4_096,
                        4_096.toLong()
                    )
                }
                verify(exactly = 1) {
                    mockUnsafe.copyMemory(
                        null, pages[2].startAddress,
                        any(), UnsafeContainer.BYTE_ARRAY_BASE_OFFSET + 8_192,
                        1_808.toLong()
                    )
                }
                verify(exactly = 1) {
                    onHeapMemoryBuffer.setReadableBytes(10_000)
                }
            }
            clearAllMocks()
            UnsafeContainer.restoreUnsafe()
        }
        Scenario("test copyFromArray") {
            lateinit var mockUnsafe: Unsafe
            lateinit var nativeMemoryBufferImpl: NativeMemoryBufferImpl
            lateinit var pages: ArrayList<NativeMemoryPage>
            lateinit var array: ByteArray

            Given("setup nativeMemoryBufferImpl") {
                mockUnsafe = mockk()
                UnsafeContainer.unsafe = mockUnsafe

                pages = ArrayList()
                pages.add(NativeMemoryPage(2L * 4_096))
                pages.add(NativeMemoryPage(1L * 4_096))
                pages.add(NativeMemoryPage(0L * 4_096))

                nativeMemoryBufferImpl = NativeMemoryBufferImpl(
                    pageSizeBytes = 4_096,
                    capacityBytes = 8_193,
                    freed = false,
                    pages = pages,
                )

                array = ByteArray(8_193)

                every {
                    mockUnsafe.copyMemory(
                        array, UnsafeContainer.BYTE_ARRAY_BASE_OFFSET + 0,
                        null, pages[0].startAddress,
                        4_096.toLong()
                    )
                } returns Unit

                every {
                    mockUnsafe.copyMemory(
                        array, UnsafeContainer.BYTE_ARRAY_BASE_OFFSET + 4_096,
                        null, pages[1].startAddress,
                        4_096.toLong()
                    )
                } returns Unit

                every {
                    mockUnsafe.copyMemory(
                        array, UnsafeContainer.BYTE_ARRAY_BASE_OFFSET + 8_192,
                        null, pages[2].startAddress,
                        1.toLong()
                    )
                } returns Unit
            }
            When("copyFromArray") {
                nativeMemoryBufferImpl.copyFromArray(array)
            }
            Then("calls are as expected") {
                verify(exactly = 1) {
                    mockUnsafe.copyMemory(
                        array, UnsafeContainer.BYTE_ARRAY_BASE_OFFSET + 0,
                        null, pages[0].startAddress,
                        4_096.toLong()
                    )
                }
                verify(exactly = 1) {
                    mockUnsafe.copyMemory(
                        array, UnsafeContainer.BYTE_ARRAY_BASE_OFFSET + 4_096,
                        null, pages[1].startAddress,
                        4_096.toLong()
                    )
                }
                verify(exactly = 1) {
                    mockUnsafe.copyMemory(
                        array, UnsafeContainer.BYTE_ARRAY_BASE_OFFSET + 8_192,
                        null, pages[2].startAddress,
                        1.toLong()
                    )
                }
            }
            clearAllMocks()
            UnsafeContainer.restoreUnsafe()
        }
        Scenario("test copyFromArray array too large") {
            lateinit var mockUnsafe: Unsafe
            lateinit var nativeMemoryBufferImpl: NativeMemoryBufferImpl
            lateinit var pages: ArrayList<NativeMemoryPage>
            lateinit var array: ByteArray
            var exceptionsThrown = 0

            Given("setup nativeMemoryBufferImpl") {
                mockUnsafe = mockk()
                UnsafeContainer.unsafe = mockUnsafe

                pages = ArrayList()
                pages.add(NativeMemoryPage(2L * 4_096))
                pages.add(NativeMemoryPage(1L * 4_096))
                pages.add(NativeMemoryPage(0L * 4_096))

                nativeMemoryBufferImpl = NativeMemoryBufferImpl(
                    pageSizeBytes = 4_096,
                    capacityBytes = 8_193,
                    freed = false,
                    pages = pages,
                )

                array = ByteArray(8_194)
            }
            When("copyFromArray") {
                try {
                    nativeMemoryBufferImpl.copyFromArray(array)
                } catch (e: IllegalStateException) {
                    exceptionsThrown += 1
                }
            }
            Then("calls are as expected") {
                assertEquals(1, exceptionsThrown)

                verify(exactly = 0) {
                    mockUnsafe.copyMemory(
                        any(), any(),
                        any(), any(),
                        any(),
                    )
                }
            }
            clearAllMocks()
            UnsafeContainer.restoreUnsafe()
        }
    }
})