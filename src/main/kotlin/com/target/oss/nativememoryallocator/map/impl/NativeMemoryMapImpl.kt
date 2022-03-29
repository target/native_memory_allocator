package com.target.oss.nativememoryallocator.map.impl

import com.target.oss.nativememoryallocator.allocator.NativeMemoryAllocator
import com.target.oss.nativememoryallocator.buffer.NativeMemoryBuffer
import com.target.oss.nativememoryallocator.buffer.OnHeapMemoryBuffer
import com.target.oss.nativememoryallocator.buffer.OnHeapMemoryBufferFactory
import com.target.oss.nativememoryallocator.map.NativeMemoryMap
import com.target.oss.nativememoryallocator.map.NativeMemoryMapOperationCounters
import com.target.oss.nativememoryallocator.map.NativeMemoryMapSerializer
import com.target.oss.nativememoryallocator.map.NativeMemoryMapStats
import java.util.concurrent.ConcurrentMap

/**
 * This class is part of the implementation of NativeMemoryMap and should not be used directly.
 *
 * All non-private methods in this class are safe for use by multiple threads.
 *
 * [put] uses [ConcurrentMap.compute] to ensure serialized access to a particular key/value mapping.
 *
 * [get] uses [ConcurrentMap.computeIfPresent] to ensure serialized access to a particular key/value mapping.
 *
 * @param valueSerializer [NativeMemoryMapSerializer] for serializing values.
 * @param nativeMemoryAllocator instance used to allocate and free value storage buffers.
 * @param useThreadLocalOnHeapReadBuffer if true enable [ThreadLocal] storage of on-heap read buffers.
 * @param threadLocalOnHeapReadBufferInitialCapacityBytes initial capacity in bytes for [ThreadLocal] on-heap read buffers.
 * @param cacheMap [ConcurrentMap] of keys to [NativeMemoryBuffer] backing this map instance.
 */
class NativeMemoryMapImpl<KEY_TYPE, VALUE_TYPE>(
    private val valueSerializer: NativeMemoryMapSerializer<VALUE_TYPE>,
    private val nativeMemoryAllocator: NativeMemoryAllocator,
    useThreadLocalOnHeapReadBuffer: Boolean,
    private val threadLocalOnHeapReadBufferInitialCapacityBytes: Int,
    private val cacheMap: ConcurrentMap<KEY_TYPE, NativeMemoryBuffer>,
) : NativeMemoryMap<KEY_TYPE, VALUE_TYPE> {

    override fun put(key: KEY_TYPE, value: VALUE_TYPE?): NativeMemoryMap.PutResult {
        var result: NativeMemoryMap.PutResult = NativeMemoryMap.PutResult.NO_CHANGE

        cacheMap.compute(key) { _, currentNearCacheBuffer ->

            if (value == null) {
                // free current buffer, deleting entry from map
                if (currentNearCacheBuffer != null) {
                    nativeMemoryAllocator.freeNativeMemoryBuffer(currentNearCacheBuffer)
                    result = NativeMemoryMap.PutResult.FREED_CURRENT_BUFFER
                }
                null
            } else if (currentNearCacheBuffer == null) {
                // allocate a new buffer
                val newValueByteArray = valueSerializer.serializeToByteArray(value = value)
                val newCapacityBytes = newValueByteArray.size

                val newNearCacheBuffer =
                    nativeMemoryAllocator.allocateNativeMemoryBuffer(capacityBytes = newCapacityBytes)

                newNearCacheBuffer.copyFromArray(byteArray = newValueByteArray)

                result = NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER

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

                result = NativeMemoryMap.PutResult.REUSED_EXISTING_BUFFER

                currentNearCacheBuffer
            }
        }

        return result
    }

    override fun delete(key: KEY_TYPE): Boolean {
        return (put(key = key, value = null) == NativeMemoryMap.PutResult.FREED_CURRENT_BUFFER)
    }

    /**
     * Non-private for unit test only.
     */
    val threadLocalHeapReadBuffer =
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

        cacheMap.computeIfPresent(key) { _, nearCacheBuffer ->

            onHeapReadBuffer =
                run {
                    // copy nearCacheBuffer to readBuffer
                    val readBuffer = if (threadLocalHeapReadBuffer != null) {
                        threadLocalHeapReadBuffer.get()
                    } else {
                        OnHeapMemoryBufferFactory.newOnHeapMemoryBuffer(initialCapacityBytes = nearCacheBuffer.capacityBytes)
                    }

                    nearCacheBuffer.copyToOnHeapMemoryBuffer(onHeapMemoryBuffer = readBuffer)

                    readBuffer
                }

            nearCacheBuffer
        }

        return onHeapReadBuffer?.let {
            valueSerializer.deserializeFromOnHeapMemoryBuffer(it)
        }
    }

    override val entries: Set<Map.Entry<KEY_TYPE, NativeMemoryBuffer>>
        get() = cacheMap.entries

    override val size: Int
        get() = cacheMap.size

    override val stats: NativeMemoryMapStats
        get() = NativeMemoryMapStats()

    override val operationCounters: NativeMemoryMapOperationCounters? = null

}