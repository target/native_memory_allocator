package com.target.oss.nativememoryallocator.impl

import org.objenesis.instantiator.util.UnsafeUtils

object UnsafeContainer {
    // Using var so this can be mocked in unit tests.
    var unsafe = UnsafeUtils.getUnsafe()!!

    // for unit test only
    fun restoreUnsafe() {
        unsafe = UnsafeUtils.getUnsafe()!!
    }

    val BYTE_ARRAY_BASE_OFFSET = unsafe.arrayBaseOffset(ByteArray::class.java).toLong()
}
