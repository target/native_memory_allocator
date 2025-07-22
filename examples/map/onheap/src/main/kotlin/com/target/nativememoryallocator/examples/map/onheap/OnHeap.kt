package com.target.nativememoryallocator.examples.map.onheap

import com.target.nativememoryallocator.examples.map.utils.CacheObject
import com.target.nativememoryallocator.examples.map.utils.buildRandomString
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

/**
 * Demo application that puts 20,000 [CacheObject] instances into a [ConcurrentHashMap].
 *
 * Each [CacheObject] instance contains a random string of length 500KB.
 *
 * This is a total of 10 GB of data stored on the normal Java heap.
 *
 * This application does not make use of any features of the native_memory_allocator library.
 * It is provided for comparison with off-heap demos.
 */
private class OnHeap {

    private val numEntries = 20_000

    private val map = ConcurrentHashMap<Int, CacheObject>()

    private val randomIndex = Random.nextInt(0, numEntries)

    private fun putValueIntoMap(i: Int) {
        if ((i % 100) == 0) {
            logger.info { "put i = $i" }
        }
        val value = buildRandomString(length = 500 * 1_024)
        if (i == randomIndex) {
            logger.info { "put randomIndex = $randomIndex value.length = ${value.length}" }
            logger.info { "value.substring(0,20) = ${value.substring(0, 20)}" }
        }
        map[i] = CacheObject(
            s = value,
        )
    }

    suspend fun run() {
        logger.info { "begin run randomIndex = $randomIndex" }

        coroutineScope {
            (0 until numEntries).forEach { i ->
                launch {
                    putValueIntoMap(i = i)
                }
            }
        }

        logger.info { "map.size = ${map.size}" }

        val randomIndexValue = map[randomIndex]
        randomIndexValue?.let {
            logger.info { "get randomIndex = $randomIndex" }
            logger.info { "randomIndexValue.s.length = ${it.s.length}" }
            logger.info { "randomIndexValue.s.substring(0,20) = ${it.s.substring(0, 20)}" }
        }

        while (true) {
            delay(1_000)
        }
    }

}

suspend fun main() {
    withContext(Dispatchers.Default) {
        OnHeap().run()
    }
}