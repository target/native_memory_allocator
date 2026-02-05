package com.target.nativememoryallocator.map.impl

import com.target.nativememoryallocator.buffer.OnHeapMemoryBuffer
import com.target.nativememoryallocator.map.NativeMemoryMap
import com.target.nativememoryallocator.map.NativeMemoryMapOperationCounters
import java.util.concurrent.atomic.AtomicLong

/**
 * Implementation of [NativeMemoryMapOperationCounters].
 */
internal data class OperationCountersImpl(
    override val numPutsNoChange: AtomicLong = AtomicLong(0),
    override val numPutsFreedBuffer: AtomicLong = AtomicLong(0),
    override val numPutsReusedBuffer: AtomicLong = AtomicLong(0),
    override val numPutsNewBuffer: AtomicLong = AtomicLong(0),
    override val numDeletesFreedBuffer: AtomicLong = AtomicLong(0),
    override val numDeletesNoChange: AtomicLong = AtomicLong(0),
    override val numGetsNullValue: AtomicLong = AtomicLong(0),
    override val numGetsNonNullValue: AtomicLong = AtomicLong(0),
) : NativeMemoryMapOperationCounters {

    /**
     * Needed because AtomicLong does not implement equals().
     *
     * @see https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/atomic/package-summary.html
     *
     * @param [that] object for equality check.
     * @return true if this has equal counter values to [that]
     */
    fun counterValuesEqual(that: OperationCountersImpl): Boolean {
        val atomicLongGetters = listOf(
            OperationCountersImpl::numPutsNoChange,
            OperationCountersImpl::numPutsFreedBuffer,
            OperationCountersImpl::numPutsReusedBuffer,
            OperationCountersImpl::numPutsNewBuffer,
            OperationCountersImpl::numDeletesFreedBuffer,
            OperationCountersImpl::numDeletesNoChange,
            OperationCountersImpl::numGetsNullValue,
            OperationCountersImpl::numGetsNonNullValue,
        )

        return atomicLongGetters.all { getter ->
            getter(this).toLong() == getter(that).toLong()
        }
    }

}

/**
 * Implementation of [NativeMemoryMap] supporting operation counters.
 *
 * @param nativeMemoryMap [NativeMemoryMap] instance for delegation.
 */
internal class OperationCountedNativeMemoryMapImpl<KEY_TYPE : Any, VALUE_TYPE : Any>(
    private val nativeMemoryMap: NativeMemoryMap<KEY_TYPE, VALUE_TYPE>,
) : NativeMemoryMap<KEY_TYPE, VALUE_TYPE> by nativeMemoryMap {

    /**
     * [NativeMemoryMapOperationCounters] instance.
     */
    override val operationCounters = OperationCountersImpl()

    /**
     * Delegate to [NativeMemoryMap.get] and then update [operationCounters].
     */
    override fun get(key: KEY_TYPE): VALUE_TYPE? {
        val getResult = nativeMemoryMap.get(key = key)

        operationCounters.apply {
            if (getResult != null) {
                numGetsNonNullValue.incrementAndGet()
            } else {
                numGetsNullValue.incrementAndGet()
            }
        }

        return getResult
    }

    /**
     * Delegate to [NativeMemoryMap.getIntoOnHeapMemoryBuffer] and then update [operationCounters].
     */
    override fun getIntoOnHeapMemoryBuffer(key: KEY_TYPE, onHeapMemoryBuffer: OnHeapMemoryBuffer): Boolean {
        val getResult = nativeMemoryMap.getIntoOnHeapMemoryBuffer(key = key, onHeapMemoryBuffer = onHeapMemoryBuffer)

        operationCounters.apply {
            if (getResult) {
                numGetsNonNullValue.incrementAndGet()
            } else {
                numGetsNullValue.incrementAndGet()
            }
        }

        return getResult
    }

    /**
     * Delegate to [NativeMemoryMap.put] and then update [operationCounters].
     */
    override fun put(key: KEY_TYPE, value: VALUE_TYPE?): NativeMemoryMap.PutResult {
        val putResult = nativeMemoryMap.put(key = key, value = value)

        operationCounters.apply {
            when (putResult) {
                NativeMemoryMap.PutResult.NO_CHANGE -> numPutsNoChange.incrementAndGet()
                NativeMemoryMap.PutResult.FREED_CURRENT_BUFFER -> numPutsFreedBuffer.incrementAndGet()
                NativeMemoryMap.PutResult.ALLOCATED_NEW_BUFFER -> numPutsNewBuffer.incrementAndGet()
                NativeMemoryMap.PutResult.REUSED_EXISTING_BUFFER -> numPutsReusedBuffer.incrementAndGet()
            }
        }

        return putResult
    }

    /**
     * Delegate to [NativeMemoryMap.delete] and then update [operationCounters].
     */
    override fun delete(key: KEY_TYPE): Boolean {
        val deleteResult = nativeMemoryMap.delete(key = key)

        operationCounters.apply {
            if (deleteResult) {
                numDeletesFreedBuffer.incrementAndGet()
            } else {
                numDeletesNoChange.incrementAndGet()
            }
        }

        return deleteResult
    }

}