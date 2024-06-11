package com.target.nativememoryallocator.metrics.micrometer

import com.target.nativememoryallocator.allocator.NativeMemoryAllocator
import com.target.nativememoryallocator.map.BaseNativeMemoryMap
import com.target.nativememoryallocator.map.ReferenceCountMap
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import java.util.function.ToDoubleFunction

private fun <T> MeterRegistry.registerGauge(
    name: String,
    gaugeObject: T,
    tags: Tags,
    toDoubleFunction: ToDoubleFunction<T>,
) {
    Gauge.builder(
        name,
        gaugeObject,
        toDoubleFunction,
    ).tags(tags).register(this)
}

/**
 * Register Micrometer metrics for the specified [NativeMemoryAllocator].
 *
 * Micrometer [Gauge] instances will be created with weak references
 * to the specified [NativeMemoryAllocator].
 *
 * @param nativeMemoryAllocator [NativeMemoryAllocator] instance.
 * @param meterRegistry [MeterRegistry] instance for metrics registration.
 * @param tags set of custom tags.
 */
class MicrometerNativeMemoryAllocatorMetrics(
    nativeMemoryAllocator: NativeMemoryAllocator,
    meterRegistry: MeterRegistry,
    tags: Tags = Tags.empty(),
) {
    init {
        meterRegistry.registerGauge(
            name = "nativeMemoryAllocator.numFreePages",
            gaugeObject = nativeMemoryAllocator,
            tags = tags,
        ) { nativeMemoryAllocator.numFreePages.toDouble() }

        meterRegistry.registerGauge(
            name = "nativeMemoryAllocator.numUsedPages",
            gaugeObject = nativeMemoryAllocator,
            tags = tags,
        ) { nativeMemoryAllocator.numUsedPages.toDouble() }

        meterRegistry.registerGauge(
            name = "nativeMemoryAllocator.totalNumPages",
            gaugeObject = nativeMemoryAllocator,
            tags = tags,
        ) { nativeMemoryAllocator.totalNumPages.toDouble() }

        meterRegistry.registerGauge(
            name = "nativeMemoryAllocator.numAllocationExceptions",
            gaugeObject = nativeMemoryAllocator,
            tags = tags,
        ) { nativeMemoryAllocator.numAllocationExceptions.toDouble() }

        meterRegistry.registerGauge(
            name = "nativeMemoryAllocator.numFreeExceptions",
            gaugeObject = nativeMemoryAllocator,
            tags = tags,
        ) { nativeMemoryAllocator.numFreeExceptions.toDouble() }
    }
}

/**
 * Register Micrometer metrics for the specified NativeMemoryMap.
 *
 * Micrometer [Gauge] instances will be created with weak references
 * to the specified [BaseNativeMemoryMap].
 *
 * @param nativeMemoryMap [BaseNativeMemoryMap] instance
 * @param meterRegistry [MeterRegistry] instance for metrics registration.
 * @param tags set of custom tags.
 */
class MicrometerNativeMemoryMapMetrics(
    nativeMemoryMap: BaseNativeMemoryMap,
    meterRegistry: MeterRegistry,
    tags: Tags = Tags.empty(),
) {
    init {
        meterRegistry.registerGauge(
            name = "nativeMemoryMap.size",
            gaugeObject = nativeMemoryMap,
            tags = tags,
        ) { nativeMemoryMap.size.toDouble() }

        if (nativeMemoryMap.stats.caffeineStats != null) {
            meterRegistry.registerGauge(
                name = "nativeMemoryMap.caffeineEvictionCount",
                gaugeObject = nativeMemoryMap,
                tags = tags,
            ) { nativeMemoryMap.stats.caffeineStats?.evictionCount()?.toDouble() ?: 0.0 }
        }

        val operationCounters = nativeMemoryMap.operationCounters

        operationCounters?.apply {

            meterRegistry.registerGauge(
                name = "nativeMemoryMap.numPutsNoChange",
                gaugeObject = this,
                tags = tags,
            ) { operationCounters.numPutsNoChange.toDouble() }

            meterRegistry.registerGauge(
                name = "nativeMemoryMap.numPutsFreedBuffer",
                gaugeObject = this,
                tags = tags,
            ) { operationCounters.numPutsFreedBuffer.toDouble() }

            meterRegistry.registerGauge(
                name = "nativeMemoryMap.numPutsReusedBuffer",
                gaugeObject = this,
                tags = tags,
            ) { operationCounters.numPutsReusedBuffer.toDouble() }

            meterRegistry.registerGauge(
                name = "nativeMemoryMap.numPutsNewBuffer",
                gaugeObject = this,
                tags = tags,
            ) { operationCounters.numPutsNewBuffer.toDouble() }

            meterRegistry.registerGauge(
                name = "nativeMemoryMap.numDeletesFreedBuffer",
                gaugeObject = this,
                tags = tags,
            ) { operationCounters.numDeletesFreedBuffer.toDouble() }

            meterRegistry.registerGauge(
                name = "nativeMemoryMap.numDeletesNoChange",
                gaugeObject = this,
                tags = tags,
            ) { operationCounters.numDeletesNoChange.toDouble() }

            meterRegistry.registerGauge(
                name = "nativeMemoryMap.numGetsNullValue",
                gaugeObject = this,
                tags = tags,
            ) { operationCounters.numGetsNullValue.toDouble() }

            meterRegistry.registerGauge(
                name = "nativeMemoryMap.numGetsNonNullValue",
                gaugeObject = this,
                tags = tags,
            ) { operationCounters.numGetsNonNullValue.toDouble() }
        }
    }
}

class MicrometerReferenceCountMapMetrics(
    referenceCountMap: ReferenceCountMap<*, *>,
    meterRegistry: MeterRegistry,
    tags: Tags = Tags.empty(),
) {
    init {
        meterRegistry.registerGauge(
            name = "nativeMemoryMap.size",
            gaugeObject = referenceCountMap,
            tags = tags,
        ) { referenceCountMap.size.toDouble() }
    }
}