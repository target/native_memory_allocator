package com.target.oss.nativememoryallocator.map.impl

import com.github.benmanes.caffeine.cache.*
import com.target.oss.nativememoryallocator.allocator.NativeMemoryAllocator
import com.target.oss.nativememoryallocator.buffer.NativeMemoryBuffer
import com.target.oss.nativememoryallocator.map.CaffeineConfigBuilder
import com.target.oss.nativememoryallocator.map.NativeMemoryMap
import com.target.oss.nativememoryallocator.map.NativeMemoryMapStats
import mu.KotlinLogging
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

/**
 * Implementation of [CaffeineConfigBuilder].
 */
class CaffeineConfigBuilderImpl(
    var caffeine: Caffeine<Any, Any>,
) : CaffeineConfigBuilder {

    override fun expireAfterAccess(duration: Long, timeUnit: TimeUnit): CaffeineConfigBuilder {
        caffeine = caffeine.expireAfterAccess(duration, timeUnit)
        return this
    }

    override fun maximumSize(maximumSize: Long): CaffeineConfigBuilder {
        caffeine = caffeine.maximumSize(maximumSize)
        return this
    }

    override fun recordStats(): CaffeineConfigBuilder {
        caffeine = caffeine.recordStats()
        return this
    }

    override fun ticker(ticker: Ticker): CaffeineConfigBuilder {
        caffeine = caffeine.ticker(ticker)
        return this
    }
}

/**
 * [RemovalListener] for caffeine eviction.
 *
 * Frees [NativeMemoryBuffer] when eviction occurs.
 *
 * @param nativeMemoryAllocator used to freeing value storage buffers.
 */
class CaffeineEvictionListener<KEY_TYPE>(
    private val nativeMemoryAllocator: NativeMemoryAllocator,
) : RemovalListener<KEY_TYPE, NativeMemoryBuffer> {

    /**
     * Callback from caffeine when eviction occurrs.
     *
     * @param key key object
     * @param value [NativeMemoryBuffer] object
     * @param cause [RemovalCause] object
     */
    override fun onRemoval(key: KEY_TYPE?, value: NativeMemoryBuffer?, cause: RemovalCause?) {
        try {
            if ((cause?.wasEvicted() == true) && (value?.freed == false)) {
                nativeMemoryAllocator.freeNativeMemoryBuffer(value)
            }
        } catch (e: Exception) {
            logger.warn(e) { "exception in caffeine eviction listener" }
        }
    }

}

/**
 * Build a caffeine [Cache] instance.
 *
 * @param nativeMemoryAllocator used to allocate and free value storage buffers.
 * @param caffeineConfigFunction caffeine configuration function used for builder pattern.
 * @return [Cache] caffeine cache
 */
fun <KEY_TYPE> buildCaffeineCache(
    nativeMemoryAllocator: NativeMemoryAllocator,
    caffeineConfigFunction: (CaffeineConfigBuilder) -> Unit,
): Cache<KEY_TYPE, NativeMemoryBuffer> {
    val configBuilder = CaffeineConfigBuilderImpl(
        caffeine = Caffeine.newBuilder(),
    )
    caffeineConfigFunction(configBuilder)

    return configBuilder
        .caffeine
        .evictionListener(
            CaffeineEvictionListener<KEY_TYPE>(
                nativeMemoryAllocator = nativeMemoryAllocator,
            )
        )
        .scheduler(Scheduler.systemScheduler())
        .build()
}

/**
 * This class is part of the implementation of NativeMemoryMap and should not be used directly.
 *
 * Caffeine-backed implementation of [NativeMemoryMap].
 *
 * @param caffeineCache caffeine cache instance
 * @param nativeMemoryMap [NativeMemoryMap] instance for delegation
 */
class CaffeineNativeMemoryMapImpl<KEY_TYPE, VALUE_TYPE>(
    private val caffeineCache: Cache<KEY_TYPE, NativeMemoryBuffer>,
    nativeMemoryMap: NativeMemoryMap<KEY_TYPE, VALUE_TYPE>,
) : NativeMemoryMap<KEY_TYPE, VALUE_TYPE> by nativeMemoryMap {

    /**
     * Override stats to include caffeine statistics.
     */
    override val stats: NativeMemoryMapStats
        get() = NativeMemoryMapStats(
            caffeineStats = caffeineCache.stats(),
        )

}