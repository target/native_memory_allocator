package com.target.oss.nativememoryallocator.map

import com.github.benmanes.caffeine.cache.Ticker
import com.target.oss.nativememoryallocator.allocator.NativeMemoryAllocator
import com.target.oss.nativememoryallocator.map.impl.CaffeineNativeMemoryMapImpl
import com.target.oss.nativememoryallocator.map.impl.NativeMemoryMapImpl
import com.target.oss.nativememoryallocator.map.impl.OperationCountedNativeMemoryMapImpl
import com.target.oss.nativememoryallocator.map.impl.buildCaffeineCache
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Backend to use for [NativeMemoryMap].
 */
enum class NativeMemoryMapBackend {
    /**
     * [ConcurrentHashMap] backend.
     */
    CONCURRENT_HASH_MAP,

    /**
     * Caffeine backend.
     */
    CAFFEINE,
}

/**
 * Caffeine backend configuration builder.
 */
interface CaffeineConfigBuilder {

    /**
     * Set a TTL so that entries are evicted after the duration elapses after read or write operations for an entry.
     *
     * See [caffeine time-based eviction](https://github.com/ben-manes/caffeine/wiki/Eviction#time-based)
     */
    fun expireAfterAccess(duration: Long, timeUnit: TimeUnit): CaffeineConfigBuilder

    /**
     * Set a maximum size after which least-recently-used entries are evicted.
     *
     * See [caffeine size-based eviction]https://github.com/ben-manes/caffeine/wiki/Eviction#size-based
     */
    fun maximumSize(maximumSize: Long): CaffeineConfigBuilder

    /**
     * Enable statistics recording.
     *
     * See [caffeine statistics](https://github.com/ben-manes/caffeine/wiki/Statistics)
     */
    fun recordStats(): CaffeineConfigBuilder

    /**
     * Override the standard [Ticker].
     *
     * For unit testing only.
     *
     * See [caffeine testing](https://github.com/ben-manes/caffeine/wiki/Testing)
     */
    fun ticker(ticker: Ticker): CaffeineConfigBuilder

}

/**
 * Builder for [NativeMemoryMap].
 *
 * @property valueSerializer [NativeMemoryMapSerializer] for serializing values.
 * @property nativeMemoryAllocator [NativeMemoryAllocator] instance used to allocate and free value storage buffers.
 * @property enableOperationCounters if true enable operation counters.
 * @property useThreadLocalOnHeapReadBuffer if true enable [ThreadLocal] storage of on-heap read buffers.
 * @property threadLocalOnHeapReadBufferInitialCapacityBytes initial capacity in bytes for [ThreadLocal] on-heap read buffers.
 * @property caffeineConfigFunction caffeine config function
 */
data class NativeMemoryMapBuilder<KEY_TYPE, VALUE_TYPE>(
    val valueSerializer: NativeMemoryMapSerializer<VALUE_TYPE>,
    val nativeMemoryAllocator: NativeMemoryAllocator,
    val enableOperationCounters: Boolean = false,
    val useThreadLocalOnHeapReadBuffer: Boolean = true,
    val threadLocalOnHeapReadBufferInitialCapacityBytes: Int = (256 * 1024),
    val backend: NativeMemoryMapBackend = NativeMemoryMapBackend.CONCURRENT_HASH_MAP,
    val caffeineConfigFunction: (CaffeineConfigBuilder) -> Unit = {},
) {

    /**
     * Build a [NativeMemoryMap] with the specified properties.
     * @return [NativeMemoryMap] instance.
     */
    fun build(): NativeMemoryMap<KEY_TYPE, VALUE_TYPE> {

        val nativeMemoryMap = when (backend) {
            NativeMemoryMapBackend.CONCURRENT_HASH_MAP -> {
                NativeMemoryMapImpl(
                    valueSerializer = valueSerializer,
                    nativeMemoryAllocator = nativeMemoryAllocator,
                    useThreadLocalOnHeapReadBuffer = useThreadLocalOnHeapReadBuffer,
                    threadLocalOnHeapReadBufferInitialCapacityBytes = threadLocalOnHeapReadBufferInitialCapacityBytes,
                    cacheMap = ConcurrentHashMap(),
                )
            }
            NativeMemoryMapBackend.CAFFEINE -> {
                val caffeineCache = buildCaffeineCache<KEY_TYPE>(
                    nativeMemoryAllocator = nativeMemoryAllocator,
                    caffeineConfigFunction = caffeineConfigFunction,
                )

                val nativeMemoryMap = NativeMemoryMapImpl(
                    valueSerializer = valueSerializer,
                    nativeMemoryAllocator = nativeMemoryAllocator,
                    useThreadLocalOnHeapReadBuffer = useThreadLocalOnHeapReadBuffer,
                    threadLocalOnHeapReadBufferInitialCapacityBytes = threadLocalOnHeapReadBufferInitialCapacityBytes,
                    cacheMap = caffeineCache.asMap(),
                )

                CaffeineNativeMemoryMapImpl(
                    caffeineCache = caffeineCache,
                    nativeMemoryMap = nativeMemoryMap,
                )
            }
        }

        return if (enableOperationCounters) {
            OperationCountedNativeMemoryMapImpl(
                nativeMemoryMap = nativeMemoryMap,
            )
        } else {
            nativeMemoryMap
        }
    }
}
