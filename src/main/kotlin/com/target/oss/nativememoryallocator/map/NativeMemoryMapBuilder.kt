package com.target.oss.nativememoryallocator.map

import com.target.oss.nativememoryallocator.allocator.NativeMemoryAllocator
import com.target.oss.nativememoryallocator.map.impl.CaffeineNativeMemoryMapImpl
import com.target.oss.nativememoryallocator.map.impl.NativeMemoryMapImpl
import com.target.oss.nativememoryallocator.map.impl.buildCaffeineCache
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

enum class NativeMemoryMapBackend {
    CONCURRENT_HASH_MAP,
    CAFFEINE,
}

interface CaffeineConfigBuilder {
    fun expireAfterAccess(duration: Long, timeUnit: TimeUnit): CaffeineConfigBuilder
    fun expireAfterWrite(duration: Long, timeUnit: TimeUnit): CaffeineConfigBuilder
    fun maximumSize(maximumSize: Long): CaffeineConfigBuilder
    fun recordStats(): CaffeineConfigBuilder
}

data class NativeMemoryMapBuilder<KEY_TYPE, VALUE_TYPE>(
    val valueSerializer: NativeMemoryMapSerializer<VALUE_TYPE>,
    val nativeMemoryAllocator: NativeMemoryAllocator,
    val useThreadLocalOnHeapReadBuffer: Boolean = true,
    val threadLocalOnHeapReadBufferInitialCapacityBytes: Int = (256 * 1024),
    val backend: NativeMemoryMapBackend = NativeMemoryMapBackend.CONCURRENT_HASH_MAP,
    val caffeineConfigFunction: (CaffeineConfigBuilder) -> Unit = {},
) {

    fun build(): NativeMemoryMap<KEY_TYPE, VALUE_TYPE> =
        when (backend) {
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

                val nativeMemoryMapImpl = NativeMemoryMapImpl(
                    valueSerializer = valueSerializer,
                    nativeMemoryAllocator = nativeMemoryAllocator,
                    useThreadLocalOnHeapReadBuffer = useThreadLocalOnHeapReadBuffer,
                    threadLocalOnHeapReadBufferInitialCapacityBytes = threadLocalOnHeapReadBufferInitialCapacityBytes,
                    cacheMap = caffeineCache.asMap(),
                )

                CaffeineNativeMemoryMapImpl(
                    caffeineCache = caffeineCache,
                    nativeMemoryMapImpl = nativeMemoryMapImpl,
                )
            }
        }
}
