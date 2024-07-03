/*
 *   Copyright 2020-2024 Leon Latsch
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

import android.content.res.Resources
import coil.ImageLoader
import coil.fetch.Fetcher
import coil.request.Options
import dev.leonlatsch.photok.imageloading.compose.model.EncryptedImageRequestData
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import javax.inject.Inject

class EncryptedImageFetcherFactory @Inject constructor(
    private val encryptedStorageManager: EncryptedStorageManager,
    private val resources: Resources,
) : Fetcher.Factory<EncryptedImageRequestData> {
    override fun create(data: EncryptedImageRequestData, options: Options, imageLoader: ImageLoader): Fetcher =
        EncryptedImageFetcher(
            encryptedStorageManager = encryptedStorageManager,
            requestData = data,
            resources = resources,
        )

}