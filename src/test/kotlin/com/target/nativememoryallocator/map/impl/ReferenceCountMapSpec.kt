package com.target.nativememoryallocator.map.impl

import com.target.nativememoryallocator.allocator.NativeMemoryAllocatorBuilder
import com.target.nativememoryallocator.buffer.OnHeapMemoryBuffer
import com.target.nativememoryallocator.buffer.OnHeapMemoryBufferFactory
import com.target.nativememoryallocator.map.NativeMemoryMapSerializer
import com.target.nativememoryallocator.map.ReferenceCountMap

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

    println("map.size = ${map.size}")

    map.put(
        key = "123", value = "234",
    )

    println("map.size = ${map.size}")

    var value = map.get(key = "123")
    println("get value = $value")

    val onHeapMemoryBuffer = OnHeapMemoryBufferFactory.newOnHeapMemoryBuffer(initialCapacityBytes = 2)
    value = map.getWithBuffer(key = "123", onHeapMemoryBuffer = onHeapMemoryBuffer)

    println("getWithBuffer value = $value")

    map.put(
        key = "123", value = "345",
    )

    println("map.size = ${map.size}")

    value = map.get(key = "123")

    println("got value = $value")

    value = map.get(key = "234")

    println("got value = $value")

    map.delete(key = "234")
    map.delete(key = "123")

    value = map.get(key = "123")

    println("after delete got value = $value")

    println("map.size = ${map.size}")

    println("nma metadata = ${nativeMemoryAllocator.nativeMemoryAllocatorMetadata}")
}