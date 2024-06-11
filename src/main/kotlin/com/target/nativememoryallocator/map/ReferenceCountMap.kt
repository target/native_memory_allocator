package com.target.nativememoryallocator.map

import com.target.nativememoryallocator.allocator.NativeMemoryAllocator
import com.target.nativememoryallocator.allocator.NativeMemoryAllocatorBuilder
import com.target.nativememoryallocator.buffer.NativeMemoryBuffer
import com.target.nativememoryallocator.buffer.OnHeapMemoryBuffer
import com.target.nativememoryallocator.buffer.OnHeapMemoryBufferFactory
import java.lang.annotation.Native
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicInteger

private class ReferenceCountValue(
    val nativeMemoryBuffer: NativeMemoryBuffer,
) {
    private val referenceCount = AtomicInteger(0)

    fun incrementReferenceCount(): Int {
        return referenceCount.incrementAndGet()
    }

    fun decrementReferenceCount(): Int {
        return referenceCount.decrementAndGet()
    }

}

class ReferenceCountMap<KEY_TYPE : Any, VALUE_TYPE : Any>(
    private val valueSerializer: NativeMemoryMapSerializer<VALUE_TYPE>,
    private val nativeMemoryAllocator: NativeMemoryAllocator,
) {

    private val innerMap = ConcurrentHashMap<KEY_TYPE, ReferenceCountValue>()

    private fun freeNativeMemoryBuffer(
        nativeMemoryBuffer: NativeMemoryBuffer,
    ) {
        nativeMemoryAllocator.freeNativeMemoryBuffer(nativeMemoryBuffer)
    }

    private fun decrementReferenceCount(
        referenceCountValue: ReferenceCountValue,
    ) {
        if (referenceCountValue.decrementReferenceCount() == 0) {
            freeNativeMemoryBuffer(referenceCountValue.nativeMemoryBuffer)
        }
    }

    fun put(key: KEY_TYPE, value: VALUE_TYPE) {

        val newValueByteArray = valueSerializer.serializeToByteArray(value = value)
        val newCapacityBytes = newValueByteArray.size

        val nativeMemoryBuffer =
            nativeMemoryAllocator.allocateNativeMemoryBuffer(capacityBytes = newCapacityBytes)

        nativeMemoryBuffer.copyFromArray(byteArray = newValueByteArray)

        val newRefCountedValue = ReferenceCountValue(
            nativeMemoryBuffer = nativeMemoryBuffer,
        )
        newRefCountedValue.incrementReferenceCount()

        val previousValue = innerMap.put(key = key, value = newRefCountedValue)

        if (previousValue != null) {
            decrementReferenceCount(previousValue)
        }
    }

    fun get(key: KEY_TYPE): VALUE_TYPE? {
        val mapValue = innerMap.computeIfPresent(key) { _, currentValue ->
            currentValue.incrementReferenceCount()

            currentValue
        } ?: return null

        try {
            // copy NMA to onheap buffer
            val onHeapMemoryBuffer =
                OnHeapMemoryBufferFactory.newOnHeapMemoryBuffer(initialCapacityBytes = mapValue.nativeMemoryBuffer.capacityBytes)

            mapValue.nativeMemoryBuffer.copyToOnHeapMemoryBuffer(onHeapMemoryBuffer)

            val deserializedValue =
                valueSerializer.deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer = onHeapMemoryBuffer)

            return deserializedValue
        } finally {
            decrementReferenceCount(mapValue)
        }
    }

    fun getWithBuffer(
        key: KEY_TYPE,
        onHeapMemoryBuffer: OnHeapMemoryBuffer,
    ): VALUE_TYPE? {
        val mapValue = innerMap.computeIfPresent(key) { _, currentValue ->
            currentValue.incrementReferenceCount()

            currentValue
        } ?: return null

        try {
            mapValue.nativeMemoryBuffer.copyToOnHeapMemoryBuffer(onHeapMemoryBuffer)

            val deserializedValue =
                valueSerializer.deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer = onHeapMemoryBuffer)

            return deserializedValue
        } finally {
            decrementReferenceCount(mapValue)
        }
    }

    fun delete(key: KEY_TYPE) {
        val previousValue = innerMap.remove(key)

        if (previousValue != null) {
            decrementReferenceCount(previousValue)
        }
    }

    val size: Int
        get() = innerMap.size
}

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