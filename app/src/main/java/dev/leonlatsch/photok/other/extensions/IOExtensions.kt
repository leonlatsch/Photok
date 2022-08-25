/*
 *   Copyright 2020-2022 Leon Latsch
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

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