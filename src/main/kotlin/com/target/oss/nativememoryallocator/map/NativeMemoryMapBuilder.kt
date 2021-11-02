package com.target.oss.nativememoryallocator.map

import com.github.benmanes.caffeine.cache.Ticker
import com.target.oss.nativememoryallocator.allocator.NativeMemoryAllocator
import com.target.oss.nativememoryallocator.map.impl.CaffeineNativeMemoryMapImpl
import com.target.oss.nativememoryallocator.map.impl.NativeMemoryMapImpl
import com.target.oss.nativememoryallocator.map.impl.OperationCountedNativeMemoryMapImpl
import com.target.oss.nativememoryallocator.map.impl.buildCaffeineCache
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

enum class NativeMemoryMapBackend {
    CONCURRENT_HASH_MAP,
    CAFFEINE,
}

interface CaffeineConfigBuilder {

    // Set a TTL so that entries are evicted after the duration elapses after read or write operations
    // for an entry.
    // See https://github.com/ben-manes/caffeine/wiki/Eviction#time-based
    fun expireAfterAccess(duration: Long, timeUnit: TimeUnit): CaffeineConfigBuilder

    // Set a maximum size after which least-recently-used entries are evicted.
    // See https://github.com/ben-manes/caffeine/wiki/Eviction#size-based
    fun maximumSize(maximumSize: Long): CaffeineConfigBuilder

    // Enable statistics recording.
    // See https://github.com/ben-manes/caffeine/wiki/Statistics
    fun recordStats(): CaffeineConfigBuilder

    // Override the standard Ticker, for unit testing only.
    // See https://github.com/ben-manes/caffeine/wiki/Testing
    fun ticker(ticker: Ticker): CaffeineConfigBuilder

}

data class NativeMemoryMapBuilder<KEY_TYPE, VALUE_TYPE>(
    val valueSerializer: NativeMemoryMapSerializer<VALUE_TYPE>,
    val nativeMemoryAllocator: NativeMemoryAllocator,
    val enableOperationCounters: Boolean = false,
    val useThreadLocalOnHeapReadBuffer: Boolean = true,
    val threadLocalOnHeapReadBufferInitialCapacityBytes: Int = (256 * 1024),
    val backend: NativeMemoryMapBackend = NativeMemoryMapBackend.CONCURRENT_HASH_MAP,
    val caffeineConfigFunction: (CaffeineConfigBuilder) -> Unit = {},
) {

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
