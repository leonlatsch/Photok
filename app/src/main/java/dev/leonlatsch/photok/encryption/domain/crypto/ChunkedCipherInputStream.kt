/*
 *   Copyright 2020–2026 Leon Latsch
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

package dev.leonlatsch.photok.encryption.domain.crypto

import java.io.IOException
import java.io.InputStream
import java.security.GeneralSecurityException
import javax.crypto.Cipher
import javax.crypto.CipherInputStream

/**
 * [CipherInputStream] feeds the cipher in 512 byte steps, which makes decrypting large files slow.
 * This decrypts in [CHUNK_SIZE] blocks instead. None of the parent's read methods are used.
 */
class ChunkedCipherInputStream(
    private val input: InputStream,
    private val cipher: Cipher,
) : CipherInputStream(input, cipher) {

    private val inputBuffer = ByteArray(CHUNK_SIZE)
    private var outputBuffer = ByteArray(0)
    private var outputPosition = 0
    private var finished = false
    private var closed = false

    override fun read(): Int {
        if (!fillIfEmpty()) {
            return -1
        }

        return outputBuffer[outputPosition++].toInt() and 0xFF
    }

    override fun read(b: ByteArray): Int = read(b, 0, b.size)

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (len == 0) {
            return 0
        }

        if (!fillIfEmpty()) {
            return -1
        }

        val count = minOf(len, outputBuffer.size - outputPosition)
        System.arraycopy(outputBuffer, outputPosition, b, off, count)
        outputPosition += count
        return count
    }

    override fun skip(n: Long): Long {
        var skipped = 0L
        while (skipped < n && fillIfEmpty()) {
            val count = minOf(n - skipped, (outputBuffer.size - outputPosition).toLong()).toInt()
            outputPosition += count
            skipped += count
        }

        return skipped
    }

    override fun available(): Int = outputBuffer.size - outputPosition

    override fun markSupported(): Boolean = false

    override fun close() {
        if (closed) {
            return
        }

        closed = true
        input.close()
    }

    private fun fillIfEmpty(): Boolean {
        while (outputPosition >= outputBuffer.size) {
            if (finished) {
                return false
            }

            val read = input.read(inputBuffer)
            val output = try {
                if (read == -1) {
                    finished = true
                    cipher.doFinal()
                } else {
                    cipher.update(inputBuffer, 0, read)
                }
            } catch (e: GeneralSecurityException) {
                throw IOException(e)
            }

            outputBuffer = output ?: ByteArray(0)
            outputPosition = 0
        }

        return true
    }

    private companion object {
        const val CHUNK_SIZE = 64 * 1024
    }
}
