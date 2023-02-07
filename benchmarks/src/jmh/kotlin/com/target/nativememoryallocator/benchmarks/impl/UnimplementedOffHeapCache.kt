package com.target.nativememoryallocator.benchmarks.impl

import com.target.nativememoryallocator.benchmarks.OffHeapCache
import java.nio.ByteBuffer

/**
 * Unimplemented OffHeapCache.
 * All methods throw NotImplementedError.
 */
object UnimplementedOffHeapCache : OffHeapCache<String, ByteBuffer> {

    override fun get(key: String): ByteBuffer? {
        throw NotImplementedError()
    }

    override fun put(key: String, value: ByteBuffer) {
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