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

import android.net.Uri
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.TransferListener
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import java.io.IOException
import javax.crypto.CipherInputStream

class EncryptedDataSource(
    private val encryptedStorageManager: EncryptedStorageManager,
    private val fileName: String
) : DataSource {
    private var inputStream: CipherInputStream? = null
    private lateinit var uri: Uri

    override fun addTransferListener(transferListener: TransferListener) {}

    override fun open(dataSpec: DataSpec): Long {
        uri = dataSpec.uri
        try {
            inputStream = encryptedStorageManager.internalOpenEncryptedFileInput(fileName)
        } catch (e: Exception) {

        }
        return dataSpec.length
    }

    @Throws(IOException::class)
    override fun read(buffer: ByteArray, offset: Int, readLength: Int): Int =
        if (readLength == 0) {
            0
        } else {
            inputStream?.read(buffer, offset, readLength) ?: 0
        }

    override fun getUri(): Uri = uri

    @Throws(IOException::class)
    override fun close() {
        inputStream?.close()
    }
}