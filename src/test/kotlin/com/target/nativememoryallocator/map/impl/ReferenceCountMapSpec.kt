package com.target.nativememoryallocator.map.impl

import com.target.nativememoryallocator.allocator.NativeMemoryAllocatorBuilder
import com.target.nativememoryallocator.buffer.OnHeapMemoryBuffer
import com.target.nativememoryallocator.buffer.OnHeapMemoryBufferFactory
import com.target.nativememoryallocator.map.NativeMemoryMapSerializer
import com.target.nativememoryallocator.map.ReferenceCountMap
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

// TODO make unit test
fun main() {
    class TestSerializer : NativeMemoryMapSerializer<String> {

        override fun deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer: OnHeapMemoryBuffer): String {
            return String(onHeapMemoryBuffer.toTrimmedArray())
        }

        override fun serializeToByteArray(value: String): ByteArray {
            return value.toByteArray()
        }

    }

    val nativeMemoryAllocator = NativeMemoryAllocatorBuilder(
        zeroNativeMemoryOnStartup = false,
        nativeMemorySizeBytes = 100 * 1024 * 1024,
        pageSizeBytes = 1024,
    ).build()

    val map = ReferenceCountMap<String, String>(
        nativeMemoryAllocator = nativeMemoryAllocator,
        valueSerializer = TestSerializer(),
    )

    logger.info { "map.size = ${map.size}" }

    map.put(
        key = "123", value = "234",
    )

    logger.info { "map.size = ${map.size}" }

    var value = map.get(key = "123")
    logger.info { "get value = $value" }

    val onHeapMemoryBuffer = OnHeapMemoryBufferFactory.newOnHeapMemoryBuffer(initialCapacityBytes = 2)
    value = map.getWithBuffer(key = "123", onHeapMemoryBuffer = onHeapMemoryBuffer)

    logger.info { "getWithBuffer value = $value" }

    map.put(
        key = "123", value = "345",
    )

    for (i in 0 until 100) {
        map.put(
            key = "234", value = "234",
        )
    }

    logger.info { "map.size = ${map.size}" }

    logger.info { "nma metadata = ${nativeMemoryAllocator.nativeMemoryAllocatorMetadata}" }

    value = map.get(key = "123")

    logger.info { "got value = $value" }

    for (i in 0 until 100) {
        value = map.get(key = "234")
    }

    logger.info { "got value = $value" }

    map.delete(key = "234")
    logger.info { "after delete 234 nma metadata = ${nativeMemoryAllocator.nativeMemoryAllocatorMetadata}" }

    map.delete(key = "123")

    value = map.get(key = "123")

    logger.info { "after delete got value = $value" }

    logger.info { "map.size = ${map.size}" }

    logger.info { "nma metadata = ${nativeMemoryAllocator.nativeMemoryAllocatorMetadata}" }
}