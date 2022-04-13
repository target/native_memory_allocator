package com.target.nativememoryallocator.examples

private val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')

fun buildRandomString(length: Int): String {
    val stringBuilder = StringBuilder(length)
    (0 until length).forEach { _ ->
        stringBuilder.append(charset.random())
    }
    return stringBuilder.toString()
}