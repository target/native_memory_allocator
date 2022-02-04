package com.target.oss.nativememoryallocator.allocator.impl

import com.target.oss.nativememoryallocator.allocator.NativeMemoryAllocator
import com.target.oss.nativememoryallocator.allocator.NativeMemoryAllocatorMetadata
import com.target.oss.nativememoryallocator.buffer.NativeMemoryBuffer
import com.target.oss.nativememoryallocator.buffer.impl.NativeMemoryBufferImpl
import com.target.oss.nativememoryallocator.unsafe.UnsafeContainer
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

// All fields in this class are immutable except freeList.  freeList manages its own synchronization.
class NativeMemoryAllocatorImpl(
    override val pageSizeBytes: Int,
    override val nativeMemorySizeBytes: Long,
    zeroNativeMemoryOnStartup: Boolean,
) : NativeMemoryAllocator {

    private val baseNativeMemoryPointer: Long

    private val freeList: FreeList

    init {
        logger.info { "begin init pageSizeBytes = $pageSizeBytes nativeMemorySizeBytes = $nativeMemorySizeBytes" }

        if ((nativeMemorySizeBytes % pageSizeBytes) != 0L) {
            throw IllegalStateException("nativeMemorySizeBytes = $nativeMemorySizeBytes is not evenly divisible by pageSizeBytes = $pageSizeBytes")
        }

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

        if (false) {
            println("hello")
        }
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

    override fun allocateNativeMemoryBuffer(capacityBytes: Int): NativeMemoryBuffer {
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
        val bufferImpl = buffer as NativeMemoryBufferImpl
        if (bufferImpl.freed) {
            throw IllegalStateException("attempt to resize already freed buffer $buffer")
        }

        if (bufferImpl.capacityBytes == newCapacityBytes) {
            return
        }

        val newPagesNeeded = computePagesNeeded(newCapacityBytes)
        if (newPagesNeeded == bufferImpl.pages.size) {
            bufferImpl.capacityBytes = newCapacityBytes
            return
        }

        if (newPagesNeeded < bufferImpl.pages.size) {
            shrinkNativeMemoryBuffer(
                bufferImpl = bufferImpl,
                newCapacityBytes = newCapacityBytes,
                newPagesNeeded = newPagesNeeded,
            )
        } else {
            expandNativeMemoryBuffer(
                bufferImpl = bufferImpl,
                newCapacityBytes = newCapacityBytes,
                newPagesNeeded = newPagesNeeded,
            )
        }
    }

}