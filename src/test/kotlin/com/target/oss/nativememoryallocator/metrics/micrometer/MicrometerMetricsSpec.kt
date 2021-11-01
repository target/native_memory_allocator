package com.target.oss.nativememoryallocator.metrics.micrometer

import com.github.benmanes.caffeine.cache.stats.CacheStats
import com.target.oss.nativememoryallocator.allocator.NativeMemoryAllocator
import com.target.oss.nativememoryallocator.map.BaseNativeMemoryMap
import com.target.oss.nativememoryallocator.map.NativeMemoryMapStats
import com.target.oss.nativememoryallocator.map.impl.NativeMemoryMapOperationCountersImpl
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Tags
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature

private val logger = KotlinLogging.logger {}

class MicrometerMetricsSpec : Spek({
    Feature("MicrometerMetrics") {
        Scenario("test MicrometerNativeMemoryAllocatorMetrics") {
            val meterRegistry = SimpleMeterRegistry()
            lateinit var nativeMemoryAllocator: NativeMemoryAllocator
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

            Given("setup variables") {
                nativeMemoryAllocator = mockk()
            }
            When("construct MicrometerNativeMemoryAllocatorMetrics") {
                every {
                    nativeMemoryAllocator.numFreePages
                } returns numFreePagesValue
                every {
                    nativeMemoryAllocator.numUsedPages
                } returns numUsedPagesValue
                every {
                    nativeMemoryAllocator.totalNumPages
                } returns totalNumPagesValue
                every {
                    nativeMemoryAllocator.numAllocationExceptions
                } returns numAllocationExceptionsValue
                every {
                    nativeMemoryAllocator.numFreeExceptions
                } returns numFreeExceptionsValue

                MicrometerNativeMemoryAllocatorMetrics(
                    nativeMemoryAllocator = nativeMemoryAllocator,
                    meterRegistry = meterRegistry,
                    tags = Tags.of(tags),
                )
            }
            Then("MicrometerNativeMemoryAllocatorMetrics registers metrics with MeterRegistry") {
                assertEquals(5, meterRegistry.meters.size)

                logger.info { "meterRegistry.meters = ${meterRegistry.meters}" }
                val idToMeterMap = meterRegistry.meters.filterNotNull().associateBy { it.id }

                assertEquals(5, idToMeterMap.size)
                val idAndMeterList = idToMeterMap.toList()

                meterNameToValue.forEach { (meterName, expectedValue) ->
                    val meterObject =
                        idAndMeterList.find { (it.first.name == meterName) && (it.first.tags == tags) }?.second
                    assertNotNull(meterObject)

                    val measurement = meterObject?.measure()
                    assertNotNull(measurement)

                    assertEquals(expectedValue.toDouble(), measurement?.take(1)?.get(0)?.value)
                }
            }
            clearAllMocks()
        }
        Scenario("test MicrometerNativeMemoryMapMetrics no caffiene, no operation counters") {
            val meterRegistry = SimpleMeterRegistry()
            lateinit var nativeMemoryMap: BaseNativeMemoryMap
            val tags = listOf(Tag.of("key1", "value1"))
            val mapSizeValue = 42

            val meterNameToValue = mapOf(
                "nativeMemoryMap.size" to mapSizeValue,
            )

            Given("setup variables") {
                nativeMemoryMap = mockk()
            }
            When("construct MicrometerNativeMemoryMapMetrics") {
                every {
                    nativeMemoryMap.stats
                } returns NativeMemoryMapStats(
                    caffeineStats = null,
                )

                every {
                    nativeMemoryMap.operationCounters
                } returns null

                every {
                    nativeMemoryMap.size
                } returns mapSizeValue

                MicrometerNativeMemoryMapMetrics(
                    nativeMemoryMap = nativeMemoryMap,
                    meterRegistry = meterRegistry,
                    tags = Tags.of(tags),
                )
            }
            Then("MicrometerNativeMemoryAllocatorMetrics registers metrics with MeterRegistry") {
                assertEquals(1, meterRegistry.meters.size)

                logger.info { "meterRegistry.meters = ${meterRegistry.meters}" }
                val idToMeterMap = meterRegistry.meters.filterNotNull().associateBy { it.id }

                assertEquals(1, idToMeterMap.size)
                val idAndMeterList = idToMeterMap.toList()

                meterNameToValue.forEach { (meterName, expectedValue) ->
                    val meterObject =
                        idAndMeterList.find { (it.first.name == meterName) && (it.first.tags == tags) }?.second
                    assertNotNull(meterObject)

                    val measurement = meterObject?.measure()
                    assertNotNull(measurement)

                    assertEquals(expectedValue.toDouble(), measurement?.take(1)?.get(0)?.value)
                }
            }
            clearAllMocks()
        }
        Scenario("test MicrometerNativeMemoryMapMetrics with caffiene, with operation counters") {
            val meterRegistry = SimpleMeterRegistry()
            lateinit var nativeMemoryMap: BaseNativeMemoryMap
            lateinit var caffeineStats: CacheStats
            val tags = listOf(Tag.of("key1", "value1"))
            val mapSizeValue = 42
            val caffeineEvictionCountValue = 43

            val operationCounters = NativeMemoryMapOperationCountersImpl()
            operationCounters.numPutsNoChanges.set(44)
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
                "nativeMemoryMap.numPutsNoChange" to operationCounters.numPutsNoChanges.get(),
                "nativeMemoryMap.numPutsFreedBuffer" to operationCounters.numPutsFreedBuffer.get(),
                "nativeMemoryMap.numPutsReusedBuffer" to operationCounters.numPutsReusedBuffer.get(),
                "nativeMemoryMap.numPutsNewBuffer" to operationCounters.numPutsNewBuffer.get(),
                "nativeMemoryMap.numDeletesFreedBuffer" to operationCounters.numDeletesFreedBuffer.get(),
                "nativeMemoryMap.numDeletesNoChange" to operationCounters.numDeletesNoChange.get(),
                "nativeMemoryMap.numGetsNullValue" to operationCounters.numGetsNullValue.get(),
                "nativeMemoryMap.numGetsNonNullValue" to operationCounters.numGetsNonNullValue.get(),
            )

            Given("setup variables") {
                nativeMemoryMap = mockk()
                caffeineStats = mockk()
            }
            When("construct MicrometerNativeMemoryMapMetrics") {
                every {
                    caffeineStats.evictionCount()
                } returns caffeineEvictionCountValue.toLong()

                every {
                    nativeMemoryMap.stats
                } returns NativeMemoryMapStats(
                    caffeineStats = caffeineStats,
                )

                every {
                    nativeMemoryMap.operationCounters
                } returns operationCounters

                every {
                    nativeMemoryMap.size
                } returns mapSizeValue

                MicrometerNativeMemoryMapMetrics(
                    nativeMemoryMap = nativeMemoryMap,
                    meterRegistry = meterRegistry,
                    tags = Tags.of(tags),
                )
            }
            Then("MicrometerNativeMemoryAllocatorMetrics registers metrics with MeterRegistry") {
                assertEquals(10, meterRegistry.meters.size)

                logger.info { "meterRegistry.meters = ${meterRegistry.meters}" }
                val idToMeterMap = meterRegistry.meters.filterNotNull().associateBy { it.id }

                assertEquals(10, idToMeterMap.size)
                val idAndMeterList = idToMeterMap.toList()

                meterNameToValue.forEach { (meterName, expectedValue) ->
                    val meterObject =
                        idAndMeterList.find { (it.first.name == meterName) && (it.first.tags == tags) }?.second
                    assertNotNull(meterObject)

                    val measurement = meterObject?.measure()
                    assertNotNull(measurement)

                    assertEquals(expectedValue.toDouble(), measurement?.take(1)?.get(0)?.value)
                }
            }
            clearAllMocks()
        }
    }
})
