package com.target.oss.nativememoryallocator.unsafe

import org.objenesis.instantiator.util.UnsafeUtils

object UnsafeContainer {
    val unsafe = UnsafeUtils.getUnsafe()!!

    val BYTE_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(ByteArray::class.java).toLong()
}