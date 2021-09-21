package com.target.availability.nativememoryallocator.impl

// A NativeMemoryPage is a fixed-size native memory page, with methods to read and write bytes at a certain offset.
data class NativeMemoryPage(
    val startAddress: Long,
) {
    fun readByte(offset: Int): Byte =
        UnsafeContainer.unsafe.getByte(startAddress + offset.toLong())

    fun writeByte(offset: Int, byte: Byte) {
        UnsafeContainer.unsafe.putByte(startAddress + offset.toLong(), byte)
    }

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