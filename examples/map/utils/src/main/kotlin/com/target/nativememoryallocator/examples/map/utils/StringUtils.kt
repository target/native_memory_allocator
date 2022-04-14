package com.target.nativememoryallocator.examples.map.utils

private val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')

/**
 * Build a random alphanumeric string.
 *
 * @param [length] random string length to build.
 * @return random string
 */
fun buildRandomString(length: Int): String {
    val stringBuilder = StringBuilder(length)
    (0 until length).forEach { _ ->
        stringBuilder.append(charset.random())
    }
    return stringBuilder.toString()
}