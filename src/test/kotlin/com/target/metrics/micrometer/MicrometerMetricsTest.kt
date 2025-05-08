package com.target.metrics.micrometer

import com.github.benmanes.caffeine.cache.stats.CacheStats
import com.target.nativememoryallocator.allocator.NativeMemoryAllocator
import com.target.nativememoryallocator.map.BaseNativeMemoryMap
import com.target.nativememoryallocator.map.NativeMemoryMapStats
import com.target.nativememoryallocator.map.impl.OperationCountersImpl
import com.target.nativememoryallocator.metrics.micrometer.MicrometerNativeMemoryAllocatorMetrics
import com.target.nativememoryallocator.metrics.micrometer.MicrometerNativeMemoryMapMetrics
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

    private val mockNativeMemoryMap = mockk<BaseNativeMemoryMap>()

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

    @Test
    fun `test MicrometerNativeMemoryMapMetrics no caffeine, no operation counters`() {
        val meterRegistry = SimpleMeterRegistry()
        val tags = listOf(Tag.of("key1", "value1"))
        val mapSizeValue = 42

        val meterNameToValue = mapOf(
            "nativeMemoryMap.size" to mapSizeValue,
        )

        every {
            mockNativeMemoryMap.stats
        } returns NativeMemoryMapStats(
            caffeineStats = null,
        )

        every {
            mockNativeMemoryMap.operationCounters
        } returns null

        every {
            mockNativeMemoryMap.size
        } returns mapSizeValue

        MicrometerNativeMemoryMapMetrics(
            nativeMemoryMap = mockNativeMemoryMap,
            meterRegistry = meterRegistry,
            tags = Tags.of(tags),
        )

        meterRegistry.meters.size shouldBe 1

        logger.info { "meterRegistry.meters = ${meterRegistry.meters}" }
        val idToMeterMap = meterRegistry.meters.filterNotNull().associateBy { it.id }

        idToMeterMap.size shouldBe 1
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

    @Test
    fun `test MicrometerNativeMemoryMapMetrics with caffeine, with operation counters`() {
        val meterRegistry = SimpleMeterRegistry()
        val tags = listOf(Tag.of("key1", "value1"))
        val caffeineStats = mockk<CacheStats>()

        val mapSizeValue = 42
        val caffeineEvictionCountValue = 43

        val operationCounters = OperationCountersImpl()
        operationCounters.numPutsNoChange.set(44)
        operationCounters.numPutsFreedBuffer.set(45)
        operationCounters.numPutsReusedBuffer.set(46)
        operationCounters.numPutsNewBuffer.set(47)
        operationCounters.numDeletesFreedBuffer.set(48)
        operationCounters.numDeletesNoChange.set(49)
        operationCounters.numGetsNullValue.set(50)
        operationCounters.numGetsNonNullValue.set(51)

        val meterNameToValue = mapOf(
            "nativeMemoryMap.size" to mapSizeValue,
            "nativeMemoryMap.caffeineEvictionCount" to caffeineEvictionCountValue,
            "nativeMemoryMap.numPutsNoChange" to operationCounters.numPutsNoChange.get(),
            "nativeMemoryMap.numPutsFreedBuffer" to operationCounters.numPutsFreedBuffer.get(),
            "nativeMemoryMap.numPutsReusedBuffer" to operationCounters.numPutsReusedBuffer.get(),
            "nativeMemoryMap.numPutsNewBuffer" to operationCounters.numPutsNewBuffer.get(),
            "nativeMemoryMap.numDeletesFreedBuffer" to operationCounters.numDeletesFreedBuffer.get(),
            "nativeMemoryMap.numDeletesNoChange" to operationCounters.numDeletesNoChange.get(),
            "nativeMemoryMap.numGetsNullValue" to operationCounters.numGetsNullValue.get(),
            "nativeMemoryMap.numGetsNonNullValue" to operationCounters.numGetsNonNullValue.get(),
        )

        every {
            caffeineStats.evictionCount()
        } returns caffeineEvictionCountValue.toLong()

        every {
            mockNativeMemoryMap.stats
        } returns NativeMemoryMapStats(
            caffeineStats = caffeineStats,
        )

        every {
            mockNativeMemoryMap.operationCounters
        } returns operationCounters

        every {
            mockNativeMemoryMap.size
        } returns mapSizeValue

        MicrometerNativeMemoryMapMetrics(
            nativeMemoryMap = mockNativeMemoryMap,
            meterRegistry = meterRegistry,
            tags = Tags.of(tags),
        )

        meterRegistry.meters.size shouldBe 10

        logger.info { "meterRegistry.meters = ${meterRegistry.meters}" }
        val idToMeterMap = meterRegistry.meters.filterNotNull().associateBy { it.id }
        idToMeterMap.size shouldBe 10

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