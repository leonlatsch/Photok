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

package dev.leonlatsch.photok.videoplayer.data

import android.net.Uri
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.TransferListener
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import dev.leonlatsch.photok.other.extensions.forceSkip
import dev.leonlatsch.photok.security.EncryptionManager
import java.io.File
import java.io.IOException
import javax.crypto.CipherInputStream

/**
 * DataSource to process AES/GCM/Encrypted Video.
 * Uses [CipherInputStream.forceSkip] extension to be able to skip.
 *
 * @since 1.3.0
 * @author Leon Latsch
 */
class AesDataSource(
    private val encryptionManager: EncryptionManager,
) : DataSource {

    private var inputStream: CipherInputStream? = null
    private lateinit var uri: Uri

    override fun open(dataSpec: DataSpec): Long {
        uri = dataSpec.uri
        uri.path ?: return 0

        val file = File(uri.path!!).canonicalFile
        inputStream = encryptionManager.createCipherInputStream(
            input = file.inputStream(),
            fileName = file.name,
        )
        if (dataSpec.position != 0L) {
            inputStream?.forceSkip(dataSpec.position)
        }

        return dataSpec.length
    }

    @Throws(IOException::class)
    override fun read(target: ByteArray, offset: Int, length: Int): Int =
        if (length == 0) {
            0
        } else {
            inputStream?.read(target, offset, length) ?: 0
        }

    override fun addTransferListener(transferListener: TransferListener) {}

    override fun getUri(): Uri = uri

    override fun close() {
        inputStream?.close()
    }
}