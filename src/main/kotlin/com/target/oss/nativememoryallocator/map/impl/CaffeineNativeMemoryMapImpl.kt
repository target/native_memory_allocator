package com.target.oss.nativememoryallocator.map.impl

import com.github.benmanes.caffeine.cache.*
import com.target.oss.nativememoryallocator.allocator.NativeMemoryAllocator
import com.target.oss.nativememoryallocator.buffer.NativeMemoryBuffer
import com.target.oss.nativememoryallocator.map.CaffeineConfigBuilder
import com.target.oss.nativememoryallocator.map.NativeMemoryMap
import com.target.oss.nativememoryallocator.map.NativeMemoryMapStats
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class EvictionListener<KEY_TYPE>(
    private val nativeMemoryAllocator: NativeMemoryAllocator,
) : RemovalListener<KEY_TYPE, NativeMemoryBuffer> {

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
            EvictionListener<KEY_TYPE>(
                nativeMemoryAllocator = nativeMemoryAllocator,
            )
        )
        .scheduler(Scheduler.systemScheduler())
        .build()
}

class CaffeineNativeMemoryMapImpl<KEY_TYPE, VALUE_TYPE>(
    private val caffeineCache: Cache<KEY_TYPE, NativeMemoryBuffer>,
    nativeMemoryMapImpl: NativeMemoryMapImpl<KEY_TYPE, VALUE_TYPE>,
) : NativeMemoryMap<KEY_TYPE, VALUE_TYPE> by nativeMemoryMapImpl {

    override val stats: NativeMemoryMapStats
        get() = NativeMemoryMapStats(
            caffeineStats = caffeineCache.stats(),
        )

}