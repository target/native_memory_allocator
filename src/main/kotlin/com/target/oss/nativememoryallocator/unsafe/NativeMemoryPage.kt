package com.target.oss.nativememoryallocator.unsafe

/**
 * A fixed-size native memory page, with methods to read and write bytes at a certain offset.
 *
 * @property startAddress starting native memory address for this page.
 */
internal data class NativeMemoryPage(
    private val startAddress: Long,
) {
    /**
     * Read a byte of native memory at the specified [offset] into this page.
     *
     * @param offset offset into this page
     * @return byte value at [offset] into this page
     */
    fun readByte(offset: Int): Byte =
        UnsafeContainer.unsafe.getByte(startAddress + offset.toLong())

    /**
     * Write [byte] to native memory at the specified [offset] into this page.
     */
    fun writeByte(offset: Int, byte: Byte) {
        UnsafeContainer.unsafe.putByte(startAddress + offset.toLong(), byte)
    }

    /**
     * Copy data from [array] to this native memory page.
     *
     * @param array [ByteArray] to copy from.
     * @param arrayStartIndex starting index into [array] from which to copy.
     * @param bytesToCopy number of bytes to copy.
     */
    fun copyFromArray(
        array: ByteArray,
        arrayStartIndex: Int,
        bytesToCopy: Int,
    ) {
        if (bytesToCopy > 0) {
            UnsafeContainer.unsafe.copyMemory(
                array, UnsafeContainer.BYTE_ARRAY_BASE_OFFSET + arrayStartIndex.toLong(),
                null, startAddress,
                bytesToCopy.toLong()
            )
        }
    }

    /**
     * Copy data from this native memory page to [array].
     *
     * @param array [ByteArray] to copy to.
     * @param arrayStartIndex starting index into [array] at which to copy.
     * @param bytesToCopy number of bytes to copy.
     */
    fun copyToArray(
        array: ByteArray,
        arrayStartIndex: Int,
        bytesToCopy: Int,
    ) {
        if (bytesToCopy > 0) {
            UnsafeContainer.unsafe.copyMemory(
                null, startAddress,
                array, UnsafeContainer.BYTE_ARRAY_BASE_OFFSET + arrayStartIndex.toLong(),
                bytesToCopy.toLong()
            )
        }
    }
}