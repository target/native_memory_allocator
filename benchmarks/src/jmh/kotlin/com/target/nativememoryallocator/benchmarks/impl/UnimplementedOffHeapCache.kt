package com.target.nativememoryallocator.benchmarks.impl

import com.target.nativememoryallocator.benchmarks.OffHeapCache

/**
 * Unimplemented OffHeapCache.
 * All methods throw NotImplementedError.
 */
object UnimplementedOffHeapCache : OffHeapCache<String, ByteArray> {

    override fun get(key: String): ByteArray? {
        throw NotImplementedError()
    }

    override fun put(key: String, value: ByteArray) {
        throw NotImplementedError()
    }

    override fun size(): Int {
        throw NotImplementedError()
    }

    override fun logMetadata() {
        throw NotImplementedError()
    }

    override fun close() {
        throw NotImplementedError()
    }
}