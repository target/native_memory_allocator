package com.target.availability.nativememoryallocator

import java.nio.ByteBuffer

// An onheap memory buffer that expands on demand when setReadableBytes is called.
// This is backed by a kotlin ByteArray.
// OnHeapMemoryBuffer is not synchronized - each instance should be used in a single thread, or the user must provide synchronization.
interface OnHeapMemoryBuffer {

    val array: ByteArray

    fun getReadableBytes(): Int

    fun setReadableBytes(newReadableBytes: Int)

    fun asByteBuffer(): ByteBuffer
}
