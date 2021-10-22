package com.target.oss.nativememoryallocator.buffer

// A NativeMemoryBuffer is a list of native memory pages, with methods to read and write the native memory contents.
// NativeMemoryBuffer is not synchronized - each instance should be used in a single thread, or the user must provide synchronization.
interface NativeMemoryBuffer {

    val pageSizeBytes: Int

    val capacityBytes: Int

    val freed: Boolean

    val numPages: Int

    fun readByte(offset: Int): Byte

    fun writeByte(offset: Int, byte: Byte)

    fun readAllToByteArray(): ByteArray

    fun copyToOnHeapMemoryBuffer(onHeapMemoryBuffer: OnHeapMemoryBuffer)

    fun copyFromArray(byteArray: ByteArray)
}