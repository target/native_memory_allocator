package com.target.nativememoryallocator.buffer

import java.nio.ByteBuffer

/**
 * An onheap memory buffer that expands on demand when setReadableBytes is called.
 *
 * Backed by a [ByteArray].
 *
 * OnHeapMemoryBuffer is not synchronized - each instance should be used in a single thread, or the user must provide synchronization.
 */
interface OnHeapMemoryBuffer {

    /**
     * The [ByteArray] backing this buffer.
     */
    val array: ByteArray

    /**
     * Get readable bytes.
     *
     * @return readable bytes.
     */
    fun getReadableBytes(): Int

    /**
     * Set readable bytes to [newReadableBytes].
     *
     * This buffer will expand on demand when this method is called.
     *
     * @param newReadableBytes new readable bytes.
     */
    fun setReadableBytes(newReadableBytes: Int)

    /**
     * Return a view of this buffer as a [ByteBuffer].
     *
     * This method does not copy the contents of this buffer.
     *
     * @return [ByteBuffer] view of this buffer.
     */
    fun asByteBuffer(): ByteBuffer

    /**
     * Return a copy of this buffer as a [ByteArray].
     *
     * @return a copy of this buffer as a [ByteArray].
     */
    fun toTrimmedArray(): ByteArray
}
