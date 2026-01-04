package dev.leonlatsch.photok.other.extensions

/**
 * Returns an empty string.
 */
val String.Companion.empty: String
    get() = ""

/**
 * Remove a sequence from a string.
 */
fun String.remove(str: String): String = replace(str, String.empty)