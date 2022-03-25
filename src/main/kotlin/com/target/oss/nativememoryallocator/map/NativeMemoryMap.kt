package com.target.oss.nativememoryallocator.map

import com.github.benmanes.caffeine.cache.stats.CacheStats
import com.target.oss.nativememoryallocator.buffer.NativeMemoryBuffer
import com.target.oss.nativememoryallocator.buffer.OnHeapMemoryBuffer

// BaseNativeMemoryMap contains NativeMemoryMap methods that do not depend on generic types.
interface BaseNativeMemoryMap {
    // Get the size of the map.
    val size: Int

    // Get the NativeMemoryMapStats.
    val stats: NativeMemoryMapStats

    // Get the NativeMemoryMapOperationCounters.
    // Returns null if operation counters are disabled.
    val operationCounters: NativeMemoryMapOperationCounters?
}

// NativeMemoryMap is mapping of keys to values backed by a ConcurrentMap.
// NativeMemoryMap uses NativeMemoryAllocator to allocate, resize, and free NativeMemoryBuffers.
// Keys are stored as normal on-heap objects in the map.
// Each value is serialized using NativeMemoryMapSerializer and copied into a NativeMemoryBuffer.
// NativeMemoryMap is safe for use by multiple concurrent threads.
interface NativeMemoryMap<KEY_TYPE, VALUE_TYPE> : BaseNativeMemoryMap {

    // The result of a put operation on the map.
    enum class PutResult {
        NO_CHANGE,
        FREED_CURRENT_BUFFER,
        ALLOCATED_NEW_BUFFER,
        REUSED_EXISTING_BUFFER,
    }

    // Put a mapping for the specified key and value into the map.
    // If value is null this is equivalent to delete.
    fun put(key: KEY_TYPE, value: VALUE_TYPE?): PutResult

    // Delete an entry by key from the map.
    // Returns true if entry was found and removed from the map, false otherwise.
    fun delete(key: KEY_TYPE): Boolean

    // Get a value from the map using the key.
    // If there is no mapping for the specified key, the returned value is null.
    fun get(key: KEY_TYPE): VALUE_TYPE?

    // Get the entry set of the map.
    val entries: Set<Map.Entry<KEY_TYPE, NativeMemoryBuffer>>
}

// NativeMemoryMapSerializer is an interface used to serialize and deserialize values stored in a NativeMemoryMap.
interface NativeMemoryMapSerializer<VALUE_TYPE> {

    // serialize value to a ByteArray
    fun serializeToByteArray(value: VALUE_TYPE): ByteArray

    // deserialize value from an OnHeapMemoryBuffer
    fun deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer: OnHeapMemoryBuffer): VALUE_TYPE
}

// NativeMemoryMapStats holds statistics information for a NativeMemoryMap.
data class NativeMemoryMapStats(
    // caffeineStats is populated only if using the caffeine map backend.
    val caffeineStats: CacheStats? = null,
)

// NativeMemoryMapOperationCounters holds counters of various operation types on a NativeMemoryMap.
interface NativeMemoryMapOperationCounters {
    val numPutsNoChange: Number

    val numPutsFreedBuffer: Number

    val numPutsReusedBuffer: Number

    val numPutsNewBuffer: Number

    val numDeletesFreedBuffer: Number

    val numDeletesNoChange: Number

    val numGetsNullValue: Number

    val numGetsNonNullValue: Number
}