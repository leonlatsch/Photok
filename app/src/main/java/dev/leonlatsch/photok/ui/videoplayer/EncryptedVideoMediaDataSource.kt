/*
 *   Copyright 2020-2021 Leon Latsch
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

package dev.leonlatsch.photok.ui.videoplayer

import android.media.MediaDataSource
import java.io.InputStream

/**
 * [MediaDataSource] implementation for a [InputStream].
 *
 * @since 2.0.0
 * @author Leon Latsch
 */
class EncryptedVideoMediaDataSource(
    private val inputStream: InputStream
) : MediaDataSource() {

    override fun readAt(position: Long, buffer: ByteArray?, offset: Int, size: Int): Int {
        val read = inputStream.read(buffer)
        if (read > size) {
            return -1
        }

        return read


        /*
        Called to request data from the given position.
         Implementations should fill buffer with up to size bytes of data, and return the number of
          valid bytes in the buffer. Return 0 if size is zero (thus no bytes are read).
           Return -1 to indicate that end of stream is reached.
         */
    }

    override fun close() = inputStream.close()

    override fun getSize(): Long = inputStream.available().toLong()
}