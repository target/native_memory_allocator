package com.target.oss.nativememoryallocator.unsafe

import org.objenesis.instantiator.util.UnsafeUtils
import sun.misc.Unsafe

/**
 * Container for [Unsafe].
 *
 * Uses [UnsafeUtils] to get the [Unsafe] instance.
 */
object UnsafeContainer {
    /**
     * Unsafe instance.
     */
    val unsafe = UnsafeUtils.getUnsafe()!!

    /**
     * Array base offset for [ByteArray].
     */
    val BYTE_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(ByteArray::class.java).toLong()
}
