package com.target.oss.nativememoryallocator

import com.target.oss.nativememoryallocator.allocator.NativeMemoryAllocatorBuilder
import com.target.oss.nativememoryallocator.buffer.OnHeapMemoryBuffer
import com.target.oss.nativememoryallocator.map.NativeMemoryMapBackend
import com.target.oss.nativememoryallocator.map.NativeMemoryMapBuilder
import com.target.oss.nativememoryallocator.map.NativeMemoryMapSerializer
import mu.KotlinLogging
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

private data class TestCacheValue(
    val foo: String,
)

private class TestCacheValueSerializer : NativeMemoryMapSerializer<TestCacheValue> {
    override fun serializeToByteArray(value: TestCacheValue): ByteArray {
        logger.info { "in serializeToByteArray value = $value" }
        return value.foo.toByteArray()
    }

    override fun deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer: OnHeapMemoryBuffer): TestCacheValue {
        logger.info { "in deserializeFromOnHeapMemoryBuffer onHeapMemoryBuffer = $onHeapMemoryBuffer" }
        return TestCacheValue(foo = String(onHeapMemoryBuffer.toTrimmedArray()))
    }

}

fun main(args: Array<String>) {
    val nativeMemoryAllocator = NativeMemoryAllocatorBuilder(
        pageSizeBytes = 4_096,
        nativeMemorySizeBytes = (1L * 1024L * 1024L * 1024L),//1gb
    ).build()

    val nativeMemoryMap = NativeMemoryMapBuilder<Int, TestCacheValue>(
        valueSerializer = TestCacheValueSerializer(),
        nativeMemoryAllocator = nativeMemoryAllocator,
        backend = NativeMemoryMapBackend.CAFFEINE,
        caffeineConfigFunction = { caffeine ->
            caffeine
                .expireAfterWrite(5, TimeUnit.SECONDS)
                .maximumSize(1)
                .recordStats()
        }
    ).build()

    val putResult1 = nativeMemoryMap.put(1, TestCacheValue("1234"))
    val putResult2 = nativeMemoryMap.put(2, TestCacheValue("2345"))
    logger.info { "putResult1 = $putResult1" }
    logger.info { "putResult2 = $putResult2" }
    logger.info { "nativeMemoryMap.size = ${nativeMemoryMap.size}" }
    logger.info { "nativeMemoryMap.stats = ${nativeMemoryMap.stats}" }

    logger.info { "nativeMemoryMap.get(1) = ${nativeMemoryMap.get(1)}" }
    logger.info { "nativeMemoryMap.get(2) = ${nativeMemoryMap.get(2)}" }

    nativeMemoryMap.delete(1)
    logger.info { "after delete nativeMemoryMap.size = ${nativeMemoryMap.size}" }
    logger.info { "nativeMemoryMap.get(1) = ${nativeMemoryMap.get(1)}" }
    logger.info { "nativeMemoryMap.get(2) = ${nativeMemoryMap.get(2)}" }

    logger.info { "before sleep" }
    Thread.sleep(30 * 1000L)
    logger.info { "after sleep" }

    logger.info { "nativeMemoryMap.size = ${nativeMemoryMap.size}" }
    logger.info { "nativeMemoryMap.stats = ${nativeMemoryMap.stats}" }

    Thread.sleep(10 * 1000L)
}