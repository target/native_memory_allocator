package com.target.oss.nativememoryallocator

// NativeMemoryCacheSerializer is an interface used to serialize and deserialize values stored in a NativeMemoryCache.
interface NativeMemoryCacheSerializer<VALUE_TYPE> {

    // serialize value to a ByteArray
    fun serializeToByteArray(value: VALUE_TYPE): ByteArray

    // deserialize value from an OnHeapMemoryBuffer
    fun deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer: OnHeapMemoryBuffer): VALUE_TYPE

}

// NativeMemoryCache is mapping of keys to values backed by a ConcurrentHashMap.
// NativeMemoryCache uses NativeMemoryAllocator to allocate, resize, and free NativeMemoryBuffers.
// Keys are stored as normal on-heap objects in the map.
// Each value is serialized using NativeMemoryCacheSerializer and copied into a NativeMemoryBuffer.
// NativeMemoryCache is safe for use by multiple concurrent threads.
interface NativeMemoryCache<KEY_TYPE, VALUE_TYPE> {

    // The result of a put operation on the cache.
    enum class PutResult {
        NO_CHANGE,
        FREED_CURRENT_BUFFER,
        ALLOCATED_NEW_BUFFER,
        REUSED_EXISTING_BUFFER,
    }

    // Put a mapping for the specified key and value into the cache.
    // If value is null this is equivalent to delete.
    fun put(key: KEY_TYPE, value: VALUE_TYPE?): PutResult

    // Delete a key from the cache.
    fun delete(key: KEY_TYPE) {
        put(key = key, value = null)
    }

    // Get a value from the map using the key.
    // If there is no mapping for the specified key, the returned value is null.
    fun get(key: KEY_TYPE): VALUE_TYPE?

    // Get the NativeMemoryBuffer from the map using the key.
    // If there is no mapping for the specified key, the returned value is null.
    fun getNativeMemoryBuffer(key: KEY_TYPE): NativeMemoryBuffer?

    // Get the entry set of the cache.
    val entries: Set<Map.Entry<KEY_TYPE, NativeMemoryBuffer>>

    // Get the size of the cache.
    val size: Int

}