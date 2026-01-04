package dev.leonlatsch.photok.other.extensions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import javax.crypto.CipherInputStream

/**
 * Schedule a InputStream to be closed by the [Dispatchers.IO]
 * For use in suspend fun.
 */
fun InputStream.lazyClose() = GlobalScope.launch(Dispatchers.IO) {
    close()
}

/**
 * Schedule a OutputStream to be closed by the [Dispatchers.IO].
 * For use in suspend fun.
 */
fun OutputStream.lazyClose() = GlobalScope.launch(Dispatchers.IO) {
    close()
}

/**
 * Skip bytes by reading them to a specific point.
 * This is needed in GCM because the Authorisation Tag wont match when bytes are really skipped.
 */
fun CipherInputStream.forceSkip(bytesToSkip: Long): Long {
    var processedBytes = 0L
    while (processedBytes < bytesToSkip) {
        read()
        processedBytes++
    }

    return processedBytes
}