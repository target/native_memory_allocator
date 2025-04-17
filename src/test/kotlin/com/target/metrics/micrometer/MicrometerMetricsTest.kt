package com.target.metrics.micrometer

import com.target.nativememoryallocator.allocator.NativeMemoryAllocator
import com.target.nativememoryallocator.metrics.micrometer.MicrometerNativeMemoryAllocatorMetrics
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Tags
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

private val logger = KotlinLogging.logger {}

class MicrometerMetricsTest {

    private val mockNativeMemoryAllocator = mockk<NativeMemoryAllocator>()

    @AfterEach
    fun afterEach() {
        clearAllMocks()
    }

    @Test
    fun `test MicrometerNativeMemoryAllocatorMetrics`() {
        val meterRegistry = SimpleMeterRegistry()

        val tags = listOf(Tag.of("key1", "value1"))

        val numFreePagesValue = 1
        val numUsedPagesValue = 2
        val totalNumPagesValue = 3
        val numAllocationExceptionsValue = 4
        val numFreeExceptionsValue = 5

        val meterNameToValue = mapOf(
            "nativeMemoryAllocator.numFreePages" to numFreePagesValue,
            "nativeMemoryAllocator.numUsedPages" to numUsedPagesValue,
            "nativeMemoryAllocator.totalNumPages" to totalNumPagesValue,
            "nativeMemoryAllocator.numAllocationExceptions" to numAllocationExceptionsValue,
            "nativeMemoryAllocator.numFreeExceptions" to numFreeExceptionsValue,
        )

        every {
            mockNativeMemoryAllocator.numFreePages
        } returns numFreePagesValue
        every {
            mockNativeMemoryAllocator.numUsedPages
        } returns numUsedPagesValue
        every {
            mockNativeMemoryAllocator.totalNumPages
        } returns totalNumPagesValue
        every {
            mockNativeMemoryAllocator.numAllocationExceptions
        } returns numAllocationExceptionsValue
        every {
            mockNativeMemoryAllocator.numFreeExceptions
        } returns numFreeExceptionsValue

        MicrometerNativeMemoryAllocatorMetrics(
            nativeMemoryAllocator = mockNativeMemoryAllocator,
            meterRegistry = meterRegistry,
            tags = Tags.of(tags),
        )

        meterRegistry.meters.size shouldBe 5
        logger.info { "meterRegistry.meters = ${meterRegistry.meters}" }

        val idToMeterMap = meterRegistry.meters.filterNotNull().associateBy { it.id }
        idToMeterMap.size shouldBe 5

        val idAndMeterList = idToMeterMap.toList()

        meterNameToValue.forEach { (meterName, expectedValue) ->
            val meterObject =
                idAndMeterList.find { (it.first.name == meterName) && (it.first.tags == tags) }?.second
            meterObject shouldNotBe null

            val measurement = meterObject?.measure()
            measurement shouldNotBe null

            measurement?.take(1)?.get(0)?.value shouldBe expectedValue.toDouble()
        }

    }
}