package com.target.nativememoryallocator.benchmarks

import com.target.nativememoryallocator.benchmarks.impl.NMAOffHeapCache
import com.target.nativememoryallocator.benchmarks.impl.RocksDBOffHeapCache
import com.target.nativememoryallocator.benchmarks.impl.UnimplementedOffHeapCache
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Group
import org.openjdk.jmh.annotations.GroupThreads
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import java.nio.ByteBuffer

private val logger = KotlinLogging.logger {}

/**
 * A benchmark that evaluates the read/write performance of a cache.
 * The cache is pre-populated for a 100% hit rate.
 */
@State(Scope.Group)
open class OffHeapGetPutBenchmark {

    companion object {
        private val NUM_ENTRIES = 50_000
        private val VALUE_SIZE = (100 * 1_024) // 100KB
    }

    @Param(
        "NMA",
        "RocksDB",
    )
    var cacheType: String = ""

    private var cache: OffHeapCache<String, ByteBuffer> = UnimplementedOffHeapCache

    @State(Scope.Thread)
    open class ThreadState {
        var index = 0

        fun nextIndex(): Int {
            index++
            if (index >= NUM_ENTRIES) {
                index = 0
            }
            return index
        }
    }

    @Setup
    fun setup() {
        logger.info { "begin setup" }

        cache = when (cacheType) {
            "NMA" -> {
                NMAOffHeapCache()
            }

            "RocksDB" -> {
                RocksDBOffHeapCache()
            }

            else -> {
                throw IllegalStateException("unknown cacheType $cacheType")
            }
        }

        for (i in 0 until NUM_ENTRIES) {
            cache.put(key = i.toString(), value = ByteBuffer.wrap(ByteArray(VALUE_SIZE)))
        }

        cache.logMetadata()

        logger.info { "end setup cache.size = ${cache.size()}" }
    }

    @TearDown
    fun tearDown() {
        logger.info { "begin tearDown" }
        cache.close()
        logger.info { "end tearDown" }
    }

    @Benchmark
    @Group("read_only")
    @GroupThreads(8)
    fun readOnly(threadState: ThreadState): ByteBuffer? {
        val key = threadState.nextIndex().toString()
        val value = cache.get(key = key)
        if (value == null) {
            logger.warn { "got null value for key '$key'" }
        }
        return value
    }

    @Benchmark
    @Group("write_only")
    @GroupThreads(8)
    fun writeOnly(threadState: ThreadState) {
        val key = threadState.nextIndex().toString()
        cache.put(key = key, value = ByteBuffer.wrap(ByteArray(VALUE_SIZE)))
    }

    @Benchmark
    @Group("readwrite")
    @GroupThreads(6)
    fun readwrite_get(threadState: ThreadState): ByteBuffer? {
        val key = threadState.nextIndex().toString()
        val value = cache.get(key = key)
        if (value == null) {
            logger.warn { "got null value for key '$key'" }
        }
        return value
    }

    @Benchmark
    @Group("readwrite")
    @GroupThreads(2)
    fun readwrite_put(threadState: ThreadState) {
        val key = threadState.nextIndex().toString()
        cache.put(key = key, value = ByteBuffer.wrap(ByteArray(VALUE_SIZE)))
    }

}