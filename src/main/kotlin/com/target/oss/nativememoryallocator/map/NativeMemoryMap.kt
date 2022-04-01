package com.target.oss.nativememoryallocator.map

import com.github.benmanes.caffeine.cache.stats.CacheStats
import com.target.oss.nativememoryallocator.buffer.NativeMemoryBufferMetadata
import com.target.oss.nativememoryallocator.buffer.OnHeapMemoryBuffer

/**
 * Contains methods for a [NativeMemoryMap] that do not depend on generic types.
 */
interface BaseNativeMemoryMap {
    /**
     * Size of the map.
     */
    val size: Int

    /**
     * [NativeMemoryMapStats] for the map.
     */
    val stats: NativeMemoryMapStats

    /**
     * [NativeMemoryMapOperationCounters] for the map.
     *
     * null if operation counting is disabled.
     */
    val operationCounters: NativeMemoryMapOperationCounters?
}

/**
 * Mapping of keys to values backed by a ConcurrentMap.
 * Uses NativeMemoryAllocator to allocate, resize, and free NativeMemoryBuffers.
 * Keys are stored as normal on-heap objects in the map.
 * Each value is serialized using NativeMemoryMapSerializer and copied into a NativeMemoryBuffer.
 * NativeMemoryMap is safe for use by multiple concurrent threads.
 */
interface NativeMemoryMap<KEY_TYPE, VALUE_TYPE> : BaseNativeMemoryMap {

    /**
     * The result of a put operation on the map.
     */
    enum class PutResult {
        /**
         * occurs when putting a null value for an entry that was not present in the map.
         */
        NO_CHANGE,

        /**
         * occurs when putting a null value for an entry that was present in the map.
         */
        FREED_CURRENT_BUFFER,

        /**
         * occurs when putting a non-null value for an entry that was not present in the map.
         */
        ALLOCATED_NEW_BUFFER,

        /**
         * occurs when putting a non-null value for an entry that was present in the map.
         */
        REUSED_EXISTING_BUFFER,
    }

    /**
     * Put a mapping for the specified [key] and [value] into the map.
     *
     * If [value] is null this is equivalent to [delete].
     *
     * @param key key for entry
     * @param value new value for entry, or null to delete entry
     * @return put result
     */
    fun put(key: KEY_TYPE, value: VALUE_TYPE?): PutResult

    /**
     * Delete an entry by [key] from the map.
     *
     * @return true if entry was found and removed from the map, false otherwise.
     */
    fun delete(key: KEY_TYPE): Boolean

    /**
     * Get a value from the map using the [key].
     *
     * @return value or null if key is not present
     */
    fun get(key: KEY_TYPE): VALUE_TYPE?

    /**
     * [Set] of [KEY_TYPE] for the map.
     *
     * @return [Set] of [KEY_TYPE]
     */
    val keys: Set<KEY_TYPE>

    /**
     * [Set] of [Map.Entry] for the map.  Entries are returned as read-only [NativeMemoryBufferMetadata].
     *
     * @return [Set] of [Map.Entry] for the map
     */
    val entries: Set<Map.Entry<KEY_TYPE, NativeMemoryBufferMetadata>>
}

/**
 * Interface used to serialize and deserialize values stored in a NativeMemoryMap.
 */
interface NativeMemoryMapSerializer<VALUE_TYPE> {

    /**
     * Serialize [value] to a [ByteArray].
     *
     * @param value value to serialize
     * @return serialized value as a [ByteArray]
     */
    fun serializeToByteArray(value: VALUE_TYPE): ByteArray

    /**
     * Deserialize value from an [OnHeapMemoryBuffer].
     *
     * @param onHeapMemoryBuffer [OnHeapMemoryBuffer] for deserialization.
     * @return Deserialized value.
     */
    fun deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer: OnHeapMemoryBuffer): VALUE_TYPE
}

/**
 * Holds statistics information for a NativeMemoryMap.
 */
data class NativeMemoryMapStats(

    /**
     * non-null only if using the caffeine map backend.
     */
    val caffeineStats: CacheStats? = null,
)

/**
 * Holds counters of various operation types on a NativeMemoryMap.
 */
interface NativeMemoryMapOperationCounters {

    /**
     * Number of put operations that did not change the map.
     */
    val numPutsNoChange: Number

    /**
     * Number of put operations that resulted in a buffer being freed.
     */
    val numPutsFreedBuffer: Number

    /**
     * Number of put operations that resulted in a buffer being reused for an overwritten value.
     */
    val numPutsReusedBuffer: Number

    /**
     * Number of put operations that resulted in a buffer being allocated for a new value.
     */
    val numPutsNewBuffer: Number

    /**
     * Number of delete operations that resulted in a buffer being freed.
     */
    val numDeletesFreedBuffer: Number

    /**
     * Number of delete operations that did not change the map.
     */
    val numDeletesNoChange: Number

    /**
     * Number of get operations that returned a null value.
     */
    val numGetsNullValue: Number

    /**
     * Number of get operations that returned a non-null value.
     */
    val numGetsNonNullValue: Number
}