package com.target.nativememoryallocator.benchmarks.impl

import com.target.nativememoryallocator.allocator.NativeMemoryAllocatorBuilder
import com.target.nativememoryallocator.buffer.OnHeapMemoryBuffer
import com.target.nativememoryallocator.benchmarks.OffHeapCache
import com.target.nativememoryallocator.map.NativeMemoryMapBackend
import com.target.nativememoryallocator.map.NativeMemoryMapBuilder
import com.target.nativememoryallocator.map.NativeMemoryMapSerializer
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}


/**
 * NMA implementation of OffHeapCache.
 */
class NMAOffHeapCache : OffHeapCache<String, ByteArray> {

    init {
        logger.info { "initializing NMAOffHeapCache" }
    }

    private val nativeMemoryAllocator = NativeMemoryAllocatorBuilder(
        pageSizeBytes = 4_096, //4KB
        nativeMemorySizeBytes = (10L * 1024 * 1024 * 1024), //10gb,
    ).build()

    private val valueSerializer = object : NativeMemoryMapSerializer<ByteArray> {
        override fun deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer: OnHeapMemoryBuffer): ByteArray {
            return onHeapMemoryBuffer.toTrimmedArray()
        }

        override fun serializeToByteArray(value: ByteArray): ByteArray {
            return value
        }
    };

    private val nativeMemoryMap = NativeMemoryMapBuilder<String, ByteArray>(
        valueSerializer = valueSerializer,
        nativeMemoryAllocator = nativeMemoryAllocator,
        backend = NativeMemoryMapBackend.CAFFEINE,
    ).build()

    override fun get(key: String): ByteArray? {
        return nativeMemoryMap.get(key = key)
    }

    override fun put(key: String, value: ByteArray) {
        nativeMemoryMap.put(key = key, value = value)
    }

    override fun size(): Int {
        return nativeMemoryMap.size
    }

    override fun logMetadata() {
        logger.info { "nativeMemoryAllocator.nativeMemoryAllocatorMetadata = ${nativeMemoryAllocator.nativeMemoryAllocatorMetadata}" }
    }

    override fun close() {

    }


}