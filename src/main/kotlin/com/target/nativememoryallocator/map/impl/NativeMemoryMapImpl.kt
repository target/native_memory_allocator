package com.target.nativememoryallocator.map.impl

import com.target.nativememoryallocator.allocator.NativeMemoryAllocator
import com.target.nativememoryallocator.buffer.NativeMemoryBuffer
import com.target.nativememoryallocator.buffer.NativeMemoryBufferMetadata
import com.target.nativememoryallocator.buffer.OnHeapMemoryBuffer
import com.target.nativememoryallocator.buffer.OnHeapMemoryBufferFactory
import com.target.nativememoryallocator.map.NativeMemoryMap
import com.target.nativememoryallocator.map.NativeMemoryMapOperationCounters
import com.target.nativememoryallocator.map.NativeMemoryMapSerializer
import com.target.nativememoryallocator.map.NativeMemoryMapStats
import java.util.concurrent.ConcurrentMap

/**
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
internal class NativeMemoryMapImpl<KEY_TYPE : Any, VALUE_TYPE : Any>(
    private val valueSerializer: NativeMemoryMapSerializer<VALUE_TYPE>,
    private val nativeMemoryAllocator: NativeMemoryAllocator,
    useThreadLocalOnHeapReadBuffer: Boolean,
    private val threadLocalOnHeapReadBufferInitialCapacityBytes: Int,
    private val cacheMap: ConcurrentMap<KEY_TYPE, NativeMemoryBuffer>,
) : NativeMemoryMap<KEY_TYPE, VALUE_TYPE> {

    override fun put(key: KEY_TYPE, value: VALUE_TYPE?): NativeMemoryMap.PutResult {
        var result: NativeMemoryMap.PutResult = NativeMemoryMap.PutResult.NO_CHANGE

        cacheMap.compute(key) { _, currentBuffer ->

            if (value == null) {
                // free current buffer, deleting entry from map
                if (currentBuffer != null) {
                    nativeMemoryAllocator.freeNativeMemoryBuffer(currentBuffer)
                    result = NativeMemoryMap.PutResult.FREED_CURRENT_BUFFER
                }
                null
            } else if (currentBuffer == null) {
                // allocate a new buffer
                val newValueByteArray = valueSerializer.serializeToByteArray(value = value)
                val newCapacityBytes = newValueByteArray.size

                val newBuffer =
                    nativeMemoryAllocator.allocateNativeMemoryBuffer(capacityBytes = newCapacityBytes)

                newBuffer.copyFromArray(byteArray = newValueByteArray)

                result = NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER

                newBuffer
            } else {
                // reuse existing buffer
                val newValueByteArray = valueSerializer.serializeToByteArray(value = value)
                val newCapacityBytes = newValueByteArray.size

                nativeMemoryAllocator.resizeNativeMemoryBuffer(
                    buffer = currentBuffer,
                    newCapacityBytes = newCapacityBytes,
                )

                currentBuffer.copyFromArray(byteArray = newValueByteArray)

                result = NativeMemoryMap.PutResult.REUSED_EXISTING_BUFFER

                currentBuffer
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
    val threadLocalOnHeapReadBuffer =
        if (useThreadLocalOnHeapReadBuffer) {
            ThreadLocal.withInitial {
                OnHeapMemoryBufferFactory.newOnHeapMemoryBuffer(
                    initialCapacityBytes = threadLocalOnHeapReadBufferInitialCapacityBytes,
                )
            }
        } else {
            null
        }

    private val getOnHeapReadBuffer: (currentBuffer: NativeMemoryBuffer) -> OnHeapMemoryBuffer =
        if (threadLocalOnHeapReadBuffer != null) {
            { threadLocalOnHeapReadBuffer.get() }
        } else {
            { currentBuffer -> OnHeapMemoryBufferFactory.newOnHeapMemoryBuffer(initialCapacityBytes = currentBuffer.capacityBytes) }
        }

    override fun get(key: KEY_TYPE): VALUE_TYPE? {
        var onHeapReadBuffer: OnHeapMemoryBuffer? = null

        cacheMap.computeIfPresent(key) { _, currentBuffer ->

            onHeapReadBuffer =
                run {
                    // copy currentBuffer to readBuffer
                    val readBuffer = getOnHeapReadBuffer(currentBuffer)

                    currentBuffer.copyToOnHeapMemoryBuffer(onHeapMemoryBuffer = readBuffer)

                    readBuffer
                }

            currentBuffer
        }

        return onHeapReadBuffer?.let {
            valueSerializer.deserializeFromOnHeapMemoryBuffer(it)
        }
    }

    override val keys: Set<KEY_TYPE>
        get() = cacheMap.keys

    override val entries: Set<Map.Entry<KEY_TYPE, NativeMemoryBufferMetadata>>
        get() = cacheMap.entries

    override val size: Int
        get() = cacheMap.size

    override val stats: NativeMemoryMapStats
        get() = NativeMemoryMapStats()

    override val operationCounters: NativeMemoryMapOperationCounters? = null

}