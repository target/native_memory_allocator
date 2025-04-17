package com.target.integrationtests

import com.target.nativememoryallocator.buffer.OnHeapMemoryBuffer
import com.target.nativememoryallocator.map.NativeMemoryMapSerializer
import io.github.oshai.kotlinlogging.KotlinLogging

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