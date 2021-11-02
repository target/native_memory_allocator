package com.target.oss.nativememoryallocator.map.impl

import com.target.oss.nativememoryallocator.map.NativeMemoryMap
import com.target.oss.nativememoryallocator.map.NativeMemoryMapOperationCounters
import java.util.concurrent.atomic.AtomicLong

class OperationCountersImpl(
    override val numPutsNoChange: AtomicLong = AtomicLong(0),
    override val numPutsFreedBuffer: AtomicLong = AtomicLong(0),
    override val numPutsReusedBuffer: AtomicLong = AtomicLong(0),
    override val numPutsNewBuffer: AtomicLong = AtomicLong(0),
    override val numDeletesFreedBuffer: AtomicLong = AtomicLong(0),
    override val numDeletesNoChange: AtomicLong = AtomicLong(0),
    override val numGetsNullValue: AtomicLong = AtomicLong(0),
    override val numGetsNonNullValue: AtomicLong = AtomicLong(0),
) : NativeMemoryMapOperationCounters {

    // Needed because AtomicLong does not implement equals()
    // See https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/atomic/package-summary.html
    fun counterValuesEqual(that: OperationCountersImpl): Boolean =
        ((this.numPutsNoChange.toLong() == that.numPutsNoChange.toLong()) &&
                (this.numPutsFreedBuffer.toLong() == that.numPutsFreedBuffer.toLong()) &&
                (this.numPutsReusedBuffer.toLong() == that.numPutsReusedBuffer.toLong()) &&
                (this.numPutsNewBuffer.toLong() == that.numPutsNewBuffer.toLong()) &&
                (this.numDeletesFreedBuffer.toLong() == that.numDeletesFreedBuffer.toLong()) &&
                (this.numDeletesNoChange.toLong() == that.numDeletesNoChange.toLong()) &&
                (this.numGetsNullValue.toLong() == that.numGetsNullValue.toLong()) &&
                (this.numGetsNonNullValue.toLong() == that.numGetsNonNullValue.toLong()))

}

class OperationCountedNativeMemoryMapImpl<KEY_TYPE, VALUE_TYPE>(
    private val nativeMemoryMap: NativeMemoryMap<KEY_TYPE, VALUE_TYPE>,
) : NativeMemoryMap<KEY_TYPE, VALUE_TYPE> by nativeMemoryMap {

    override val operationCounters = OperationCountersImpl()

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