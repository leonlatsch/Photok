/*
 *   Copyright 2020-2023 Leon Latsch
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

package dev.leonlatsch.photok.imageloading.compose

import android.content.Context
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.imageloading.compose.model.EncryptedImageRequestData
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import okio.buffer
import okio.source

class EncryptedImageFetcher(
    @ApplicationContext private val context: Context,
    private val encryptedStorageManager: EncryptedStorageManager,
    private val requestData: EncryptedImageRequestData
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val inputStream = encryptedStorageManager.internalOpenEncryptedFileInput(requestData.internalFileName)
        inputStream ?: return null

        val imageSource = ImageSource(inputStream.source().buffer(), context)

        return SourceResult(
            source = imageSource,
            mimeType = requestData.mimeType,
            dataSource = DataSource.DISK
        )
    }
}