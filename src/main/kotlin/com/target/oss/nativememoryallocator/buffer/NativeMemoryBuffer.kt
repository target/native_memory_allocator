package com.target.oss.nativememoryallocator.buffer

/**
 * Read-only metadata for [NativeMemoryBuffer].
 */
interface NativeMemoryBufferMetadata {

    /**
     * Page size in bytes.
     */
    val pageSizeBytes: Int

    /**
     * Total buffer capacity in bytes.
     */
    val capacityBytes: Int

    /**
     * True if this object has been freed, false otherwise.
     */
    val freed: Boolean

    /**
     * Number of pages in this object.
     */
    val numPages: Int

}

/**
 * A list of native memory pages, with methods to read and write the native memory contents.
 *
 * This class is not synchronized - each instance should be used in a single thread, or the user must provide synchronization.
 */
interface NativeMemoryBuffer : NativeMemoryBufferMetadata {

    /**
     * Read a byte at the specified [offset].
     *
     * @param offset offset into this buffer
     * @return value read from buffer at offset
     */
    fun readByte(offset: Int): Byte

    /**
     * Write [byte] at the specified [offset].
     *
     * @param offset offset into this buffer
     * @param byte value to write at offset
     */
    fun writeByte(offset: Int, byte: Byte)

    /**
     * Read all bytes in this buffer to a [ByteArray].
     *
     * @return [ByteArray] containing a copy of the contents of this buffer.
     */
    fun readAllToByteArray(): ByteArray

    /**
     * Copy the contents of this buffer to [onHeapMemoryBuffer].
     *
     * @param onHeapMemoryBuffer [OnHeapMemoryBuffer] to copy this buffer to.
     */
    fun copyToOnHeapMemoryBuffer(onHeapMemoryBuffer: OnHeapMemoryBuffer)

    /**
     * Copy the contents of [byteArray] to this buffer.
     *
     * @param byteArray [ByteArray] to copy from into this buffer.
     * @throws [IllegalStateException] if [byteArray] size is > the capacity of this buffer.
     */
    fun copyFromArray(byteArray: ByteArray)
}