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

    private var lastReadEndPosition = 0L
    private val streamLength = inputStream.available()

    override fun readAt(position: Long, buffer: ByteArray?, offset: Int, size: Int): Int {
//        if (position >= streamLength) return -1

//        if (position + size > streamLength) s -= position.toInt() + size - streamLength


        if (position < lastReadEndPosition) {
            inputStream.close()
            lastReadEndPosition = 0
            return -1
//            `is` = getNewCopyOfInputStreamSomeHow() //new FileInputStream(mediaFile) for example.
        }

        val skipped: Long = inputStream.skip(position - lastReadEndPosition)
        return if (skipped == position - lastReadEndPosition) {
            val bytesRead: Int = inputStream.read(buffer, offset, size)
            lastReadEndPosition = position + bytesRead
            bytesRead
        } else {
            -1
        }


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