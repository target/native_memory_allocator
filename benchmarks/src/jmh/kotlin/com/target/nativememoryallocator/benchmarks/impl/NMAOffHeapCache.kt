package com.target.nativememoryallocator.benchmarks.impl

import com.target.nativememoryallocator.allocator.NativeMemoryAllocatorBuilder
import com.target.nativememoryallocator.buffer.OnHeapMemoryBuffer
import com.target.nativememoryallocator.benchmarks.OffHeapCache
import com.target.nativememoryallocator.map.NativeMemoryMapBackend
import com.target.nativememoryallocator.map.NativeMemoryMapBuilder
import com.target.nativememoryallocator.map.NativeMemoryMapSerializer
import mu.KotlinLogging
import java.nio.ByteBuffer

private val logger = KotlinLogging.logger {}


/**
 * NMA implementation of OffHeapCache.
 */
class NMAOffHeapCache : OffHeapCache<String, ByteBuffer> {

    init {
        logger.info { "initializing NMAOffHeapCache with asByteBuffer change" }
    }

    private val nativeMemoryAllocator = NativeMemoryAllocatorBuilder(
        pageSizeBytes = 4_096, //4KB
        nativeMemorySizeBytes = (10L * 1024 * 1024 * 1024), //10gb,
    ).build()

    private val valueSerializer = object : NativeMemoryMapSerializer<ByteBuffer> {
        override fun deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer: OnHeapMemoryBuffer): ByteBuffer {
            return onHeapMemoryBuffer.asByteBuffer()
        }

        override fun serializeToByteArray(value: ByteBuffer): ByteArray {
            return value.array()
        }
    };

    private val nativeMemoryMap = NativeMemoryMapBuilder<String, ByteBuffer>(
        valueSerializer = valueSerializer,
        nativeMemoryAllocator = nativeMemoryAllocator,
        backend = NativeMemoryMapBackend.CAFFEINE,
    ).build()

    override fun get(key: String): ByteBuffer? {
        return nativeMemoryMap.get(key = key)
    }

    override fun put(key: String, value: ByteBuffer) {
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