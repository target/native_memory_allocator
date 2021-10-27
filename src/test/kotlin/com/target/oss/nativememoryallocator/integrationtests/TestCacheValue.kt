package com.target.oss.nativememoryallocator.integrationtests

import com.target.oss.nativememoryallocator.buffer.OnHeapMemoryBuffer
import com.target.oss.nativememoryallocator.map.NativeMemoryMapSerializer
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

data class TestCacheValue(
    val foo: String,
)

class TestCacheValueSerializer : NativeMemoryMapSerializer<TestCacheValue> {
    override fun serializeToByteArray(value: TestCacheValue): ByteArray {
        logger.info { "in serializeToByteArray value = $value" }
        return value.foo.toByteArray()
    }

    override fun deserializeFromOnHeapMemoryBuffer(onHeapMemoryBuffer: OnHeapMemoryBuffer): TestCacheValue {
        logger.info { "in deserializeFromOnHeapMemoryBuffer onHeapMemoryBuffer = $onHeapMemoryBuffer" }
        return TestCacheValue(foo = String(onHeapMemoryBuffer.toTrimmedArray()))
    }

}