package com.target.oss.nativememoryallocator.impl

import com.target.oss.nativememoryallocator.NativeMemoryBuffer
import com.target.oss.nativememoryallocator.OnHeapMemoryBuffer

class NativeMemoryBufferImpl(
    override val pageSizeBytes: Int,
    override var capacityBytes: Int,
    override var freed: Boolean,
    val pages: ArrayList<NativeMemoryPage>,
) : NativeMemoryBuffer {

    override val numPages: Int
        get() = pages.size

    override fun readByte(offset: Int): Byte {
        val pageIndex = offset / pageSizeBytes
        val offsetInPage = offset % pageSizeBytes
        return pages[pageIndex].readByte(offset = offsetInPage)
    }

    override fun writeByte(offset: Int, byte: Byte) {
        val pageIndex = offset / pageSizeBytes
        val offsetInPage = offset % pageSizeBytes
        return pages[pageIndex].writeByte(offset = offsetInPage, byte = byte)
    }

    override fun readAllToByteArray(): ByteArray {
        val array = ByteArray(capacityBytes)

        copyToArray(array = array)

        return array
    }

    private fun copyToArray(array: ByteArray) {
        var bytesCopied = 0

        pages.forEach { page ->
            val bytesToCopy =
                (capacityBytes - bytesCopied).coerceAtMost(pageSizeBytes)

            page.copyToArray(
                array = array,
                arrayStartIndex = bytesCopied,
                bytesToCopy = bytesToCopy,
            )

            bytesCopied += bytesToCopy
        }
    }

    override fun copyToOnHeapMemoryBuffer(onHeapMemoryBuffer: OnHeapMemoryBuffer) {
        onHeapMemoryBuffer.setReadableBytes(newReadableBytes = capacityBytes)

        copyToArray(array = onHeapMemoryBuffer.array)
    }

    override fun copyFromArray(byteArray: ByteArray) {
        val byteArraySize = byteArray.size
        if (byteArraySize > capacityBytes) {
            throw IllegalStateException("byteArray.size = $byteArraySize > capacityBytes = $capacityBytes")
        }

        var bytesCopied = 0

        pages.forEach { page ->
            val bytesToCopy = (byteArraySize - bytesCopied).coerceAtMost(pageSizeBytes)

            page.copyFromArray(
                array = byteArray,
                arrayStartIndex = bytesCopied,
                bytesToCopy = bytesToCopy,
            )

            bytesCopied += bytesToCopy
        }
    }

    override fun toString(): String {
        return "NativeMemoryBufferImpl(pageSizeBytes=$pageSizeBytes, capacityBytes=$capacityBytes, freed=$freed, numPages=$numPages, pages=$pages)"
    }

}