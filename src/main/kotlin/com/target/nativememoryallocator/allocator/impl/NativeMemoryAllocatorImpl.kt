package com.target.nativememoryallocator.allocator.impl

import com.target.nativememoryallocator.allocator.NativeMemoryAllocator
import com.target.nativememoryallocator.allocator.NativeMemoryAllocatorMetadata
import com.target.nativememoryallocator.buffer.NativeMemoryBuffer
import com.target.nativememoryallocator.buffer.impl.NativeMemoryBufferImpl
import com.target.nativememoryallocator.unsafe.UnsafeContainer
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Validate initial parameters for [NativeMemoryAllocator].
 *
 * @param [pageSizeBytes] page size in bytes.
 * @param [nativeMemorySizeBytes] native memory size in bytes.
 * @throws [IllegalArgumentException] if parameters are invalid.
 */
internal fun validateNativeMemoryAllocatorInitialParameters(
    pageSizeBytes: Int,
    nativeMemorySizeBytes: Long,
) {
    if (pageSizeBytes < 1) {
        throw IllegalArgumentException("pageSizeBytes = $pageSizeBytes < 1")
    }

    if (nativeMemorySizeBytes < 1L) {
        throw IllegalArgumentException("nativeMemorySizeBytes = $nativeMemorySizeBytes < 1L")
    }

    if ((nativeMemorySizeBytes % pageSizeBytes) != 0L) {
        throw IllegalArgumentException("nativeMemorySizeBytes = $nativeMemorySizeBytes is not evenly divisible by pageSizeBytes = $pageSizeBytes")
    }

    val totalNumPages = (nativeMemorySizeBytes / pageSizeBytes)

    if (totalNumPages > Int.MAX_VALUE.toLong()) {
        throw IllegalArgumentException("totalNumPages = $totalNumPages > Int.MAX_VALUE = ${Int.MAX_VALUE.toLong()}")
    }
}

/**
 * Implementation of [NativeMemoryAllocator].
 *
 * All fields in this class are immutable except freeList.  freeList manages its own synchronization.
 *
 * @param pageSizeBytes page size bytes.
 * @param nativeMemorySizeBytes total native memory size bytes.
 * @param zeroNativeMemoryOnStartup If true write zeros to the block of native memory on construction.
 */
internal class NativeMemoryAllocatorImpl(
    override val pageSizeBytes: Int,
    override val nativeMemorySizeBytes: Long,
    zeroNativeMemoryOnStartup: Boolean,
) : NativeMemoryAllocator {

    private val baseNativeMemoryPointer: Long

    private val freeList: FreeList

    init {
        validateNativeMemoryAllocatorInitialParameters(
            pageSizeBytes = pageSizeBytes,
            nativeMemorySizeBytes = nativeMemorySizeBytes,
        )

        logger.info { "begin init pageSizeBytes = $pageSizeBytes nativeMemorySizeBytes = $nativeMemorySizeBytes" }

        logger.info { "allocating nativeMemorySizeBytes = $nativeMemorySizeBytes" }
        baseNativeMemoryPointer = UnsafeContainer.unsafe.allocateMemory(nativeMemorySizeBytes)
        logger.info { "baseNativeMemoryPointer = 0x${baseNativeMemoryPointer.toString(radix = 16)}" }

        if (zeroNativeMemoryOnStartup) {
            logger.info { "begin unsafe.setMemory" }
            UnsafeContainer.unsafe.setMemory(baseNativeMemoryPointer, nativeMemorySizeBytes, 0)
            logger.info { "end unsafe.setMemory" }
        }

        val totalNumPages = (nativeMemorySizeBytes / pageSizeBytes).toInt()

        freeList = FreeListImpl(
            baseNativeMemoryPointer = baseNativeMemoryPointer,
            pageSizeBytes = pageSizeBytes,
            totalNumPages = totalNumPages,
        )
    }

    // for unit test only
    fun baseNativeMemoryPointer(): Long = baseNativeMemoryPointer

    override val numFreePages: Int
        get() = freeList.numFreePages()

    override val totalNumPages: Int
        get() = freeList.totalNumPages

    override val numUsedPages: Int
        get() = freeList.numUsedPages()

    override val numAllocationExceptions: Int
        get() = freeList.numAllocationExceptions()

    override val numFreeExceptions: Int
        get() = freeList.numFreeExceptions()

    override val nativeMemoryAllocatorMetadata: NativeMemoryAllocatorMetadata
        get() = synchronized(freeList) {
            NativeMemoryAllocatorMetadata(
                pageSizeBytes = pageSizeBytes,
                nextFreePageIndex = freeList.nextFreePageIndex(),
                numFreePages = freeList.numFreePages(),
                totalNumPages = freeList.totalNumPages,
                numUsedPages = freeList.numUsedPages(),
                numAllocationExceptions = freeList.numAllocationExceptions(),
                numFreeExceptions = freeList.numFreeExceptions(),
                nativeMemorySizeBytes = nativeMemorySizeBytes,
            )
        }

    private fun computePagesNeeded(capacityBytes: Int): Int =
        if ((capacityBytes % pageSizeBytes) == 0) {
            capacityBytes / pageSizeBytes
        } else {
            (capacityBytes / pageSizeBytes) + 1
        }

    private fun validateNonNegativeCapacityBytes(capacityBytes: Int) {
        if (capacityBytes < 0) {
            throw IllegalArgumentException("capacityBytes = $capacityBytes < 0")
        }
    }

    override fun allocateNativeMemoryBuffer(capacityBytes: Int): NativeMemoryBuffer {
        validateNonNegativeCapacityBytes(capacityBytes = capacityBytes)

        val pagesNeeded = computePagesNeeded(capacityBytes = capacityBytes)

        val pages = freeList.allocatePages(numPagesToAllocate = pagesNeeded)

        return NativeMemoryBufferImpl(
            pages = pages,
            capacityBytes = capacityBytes,
            pageSizeBytes = pageSizeBytes,
            freed = false,
        )
    }

    override fun freeNativeMemoryBuffer(buffer: NativeMemoryBuffer) {
        val bufferImpl = buffer as NativeMemoryBufferImpl
        if (bufferImpl.freed) {
            throw IllegalStateException("attempt to free already freed buffer $buffer")
        }

        freeList.freePages(bufferImpl.pages)

        bufferImpl.capacityBytes = 0
        bufferImpl.freed = true
        bufferImpl.pages.clear()
        bufferImpl.pages.trimToSize()
    }

    private fun shrinkNativeMemoryBuffer(
        bufferImpl: NativeMemoryBufferImpl,
        newCapacityBytes: Int,
        newPagesNeeded: Int,
    ) {
        val numPagesToFree = bufferImpl.pages.size - newPagesNeeded

        val pagesToFree = (0 until numPagesToFree).map {
            bufferImpl.pages.removeLast()
        }
        freeList.freePages(pages = pagesToFree)
        bufferImpl.pages.trimToSize()

        bufferImpl.capacityBytes = newCapacityBytes
    }

    private fun expandNativeMemoryBuffer(
        bufferImpl: NativeMemoryBufferImpl,
        newCapacityBytes: Int,
        newPagesNeeded: Int,
    ) {
        val pagesToAllocate = newPagesNeeded - bufferImpl.pages.size

        val newPages = freeList.allocatePages(numPagesToAllocate = pagesToAllocate)

        bufferImpl.pages.addAll(newPages)
        bufferImpl.pages.trimToSize()

        bufferImpl.capacityBytes = newCapacityBytes
    }

    override fun resizeNativeMemoryBuffer(buffer: NativeMemoryBuffer, newCapacityBytes: Int) {
        validateNonNegativeCapacityBytes(capacityBytes = newCapacityBytes)

        val bufferImpl = buffer as NativeMemoryBufferImpl

        when {
            bufferImpl.freed -> {
                throw IllegalStateException("attempt to resize already freed buffer $buffer")
            }
            bufferImpl.capacityBytes != newCapacityBytes -> {
                val newPagesNeeded = computePagesNeeded(newCapacityBytes)

                when {
                    newPagesNeeded == bufferImpl.pages.size -> {
                        bufferImpl.capacityBytes = newCapacityBytes
                    }
                    newPagesNeeded < bufferImpl.pages.size -> {
                        shrinkNativeMemoryBuffer(
                            bufferImpl = bufferImpl,
                            newCapacityBytes = newCapacityBytes,
                            newPagesNeeded = newPagesNeeded,
                        )
                    }
                    else -> {
                        expandNativeMemoryBuffer(
                            bufferImpl = bufferImpl,
                            newCapacityBytes = newCapacityBytes,
                            newPagesNeeded = newPagesNeeded,
                        )
                    }
                }
            }
        }

    }

}