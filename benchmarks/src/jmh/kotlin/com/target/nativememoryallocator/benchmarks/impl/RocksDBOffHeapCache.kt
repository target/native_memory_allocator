package com.target.nativememoryallocator.benchmarks.impl

import com.target.nativememoryallocator.benchmarks.OffHeapCache
import mu.KotlinLogging
import org.rocksdb.RocksDB
import java.io.File
import java.nio.ByteBuffer

private val logger = KotlinLogging.logger {}

/**
 * RocksDB implementation of OffHeapCache.
 */
class RocksDBOffHeapCache : OffHeapCache<String, ByteBuffer> {

    private val rocksDbDir: String

    private val rocksDB: RocksDB

    init {
        RocksDB.loadLibrary()

        rocksDbDir = System.getProperty("rocksdb.dir") ?: "/tmp/simpleRocksDb"
        logger.info { "rocksDbDir = $rocksDbDir" }

        val deletedDataDir = File(rocksDbDir).deleteRecursively()
        logger.info { "initializing RocksDBOffHeapCache deletedDataDir = $deletedDataDir" }

        rocksDB = RocksDB.open(rocksDbDir)
    }

    override fun get(key: String): ByteBuffer? {
        return rocksDB.get(key.toByteArray())?.let { ByteBuffer.wrap(it) }
    }

    override fun put(key: String, value: ByteBuffer) {
        rocksDB.put(key.toByteArray(), value.array())
    }

    override fun size(): Int {
        return rocksDB.getProperty("rocksdb.estimate-num-keys").toInt()
    }

    override fun logMetadata() {
        logger.info { "rocksdb.estimate-live-data-size = ${rocksDB.getProperty("rocksdb.estimate-live-data-size")}" }
    }

    override fun close() {
        rocksDB.close()
    }

}