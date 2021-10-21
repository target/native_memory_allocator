package com.target.oss.nativememoryallocator.impl

import com.target.oss.nativememoryallocator.*
import java.util.concurrent.ConcurrentHashMap

// All non-private methods in this class are safe for use by multiple threads.
// put() and get() manage synchronization using ConcurrentHashMap.compute() to
// ensure serialized access to a particular key/value mapping.
class NativeMemoryCacheImpl<KEY_TYPE, VALUE_TYPE>(
    private val valueSerializer: NativeMemoryCacheSerializer<VALUE_TYPE>,
    private val nativeMemoryAllocator: NativeMemoryAllocator,
    private val useThreadLocalOnHeapReadBuffer: Boolean = true,
    private val threadLocalOnHeapReadBufferInitialCapacityBytes: Int = (256 * 1024),
) : NativeMemoryCache<KEY_TYPE, VALUE_TYPE> {

    private val cacheMap = ConcurrentHashMap<KEY_TYPE, NativeMemoryBuffer>()

    override fun put(key: KEY_TYPE, value: VALUE_TYPE?): NativeMemoryCache.PutResult {
        var result: NativeMemoryCache.PutResult = NativeMemoryCache.PutResult.NO_CHANGE

        cacheMap.compute(key) { _, currentNearCacheBuffer ->

            if (value == null) {
                // free current buffer, deleting entry from map
                if (currentNearCacheBuffer != null) {
                    nativeMemoryAllocator.freeNativeMemoryBuffer(currentNearCacheBuffer)
                    result = NativeMemoryCache.PutResult.FREED_CURRENT_BUFFER
                }
                null
            } else if (currentNearCacheBuffer == null) {
                // allocate a new buffer
                val newValueByteArray = valueSerializer.serializeToByteArray(value = value)
                val newCapacityBytes = newValueByteArray.size

                val newNearCacheBuffer =
                    nativeMemoryAllocator.allocateNativeMemoryBuffer(capacityBytes = newCapacityBytes)

                newNearCacheBuffer.copyFromArray(byteArray = newValueByteArray)

                result = NativeMemoryCache.PutResult.ALLOCATED_NEW_BUFFER

                newNearCacheBuffer
            } else {
                // reuse existing buffer
                val newValueByteArray = valueSerializer.serializeToByteArray(value = value)
                val newCapacityBytes = newValueByteArray.size

                nativeMemoryAllocator.resizeNativeMemoryBuffer(
                    buffer = currentNearCacheBuffer,
                    newCapacityBytes = newCapacityBytes,
                )

                currentNearCacheBuffer.copyFromArray(byteArray = newValueByteArray)

                result = NativeMemoryCache.PutResult.REUSED_EXISTING_BUFFER

                currentNearCacheBuffer
            }
        }

        return result
    }

    private val threadLocalHeapReadBuffer =
        if (useThreadLocalOnHeapReadBuffer) {
            ThreadLocal.withInitial {
                OnHeapMemoryBufferFactory.newOnHeapMemoryBuffer(
                    initialCapacityBytes = threadLocalOnHeapReadBufferInitialCapacityBytes,
                )
            }
        } else {
            null
        }

    override fun get(key: KEY_TYPE): VALUE_TYPE? {
        var onHeapReadBuffer: OnHeapMemoryBuffer? = null

        cacheMap.compute(key) { _, nearCacheBuffer ->

            if (nearCacheBuffer != null) {

                // copy nearCacheBuffer to readBuffer
                val readBuffer = if (threadLocalHeapReadBuffer != null) {
                    threadLocalHeapReadBuffer.get()
                } else {
                    OnHeapMemoryBufferFactory.newOnHeapMemoryBuffer(initialCapacityBytes = nearCacheBuffer.capacityBytes)
                }

                nearCacheBuffer.copyToOnHeapMemoryBuffer(onHeapMemoryBuffer = readBuffer)

                onHeapReadBuffer = readBuffer
            }

            nearCacheBuffer
        }

        return onHeapReadBuffer?.let {
            valueSerializer.deserializeFromOnHeapMemoryBuffer(it)
        }
    }

    override fun getNativeMemoryBuffer(key: KEY_TYPE): NativeMemoryBuffer? =
        cacheMap[key]

    override val entries: Set<Map.Entry<KEY_TYPE, NativeMemoryBuffer>>
        get() = cacheMap.entries

    override val size: Int
        get() = cacheMap.size

}