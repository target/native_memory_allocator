package com.target.oss.nativememoryallocator.map.impl

import com.target.oss.nativememoryallocator.map.NativeMemoryMap
import com.target.oss.nativememoryallocator.map.NativeMemoryMapOperationCounters

class NativeMemoryMapWithOperationCountersImpl<KEY_TYPE, VALUE_TYPE>(
    private val nativeMemoryMap: NativeMemoryMap<KEY_TYPE, VALUE_TYPE>,
) : NativeMemoryMap<KEY_TYPE, VALUE_TYPE> by nativeMemoryMap {

    override val operationCounters = NativeMemoryMapOperationCounters()

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
                NativeMemoryMap.PutResult.NO_CHANGE -> numPutsNoChanges.incrementAndGet()
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