package com.target.nativememoryallocator.benchmarks

/**
 * A facade for benchmark implementations.
 */
interface OffHeapCache<K, V> {

    /**
     * Get a value from the cache.
     *
     * @param key
     * @return value
     */
    fun get(key: K): V?

    /**
     * Put a value into the cache.
     *
     * @param key
     * @param value
     */
    fun put(key: K, value: V)

    /**
     * Get the size of the cache.
     *
     * @return cache size
     */
    fun size(): Int

    /**
     * Log cache metadata.
     */
    fun logMetadata()

    /**
     * Close the cache.
     */
    fun close()
}