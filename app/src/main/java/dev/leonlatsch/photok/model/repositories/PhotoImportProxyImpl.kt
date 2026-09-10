/*
 *   Copyright 2020-2026 Leon Latsch
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

package dev.leonlatsch.photok.model.repositories

import android.webkit.MimeTypeMap
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.model.domain.PhotoImportProxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject

class PhotoImportProxyImpl @Inject constructor(
    private val photoRepository: PhotoRepository,
) : PhotoImportProxy {

    override suspend fun importFile(file: File, fileName: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            if (!file.exists()) {
                return@withContext Result.failure(FileNotFoundException(file.name))
            }

            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
            val type = PhotoType.fromMimeType(mimeType)
            if (type == PhotoType.UNDEFINED) {
                return@withContext Result.failure(
                    IllegalArgumentException("Unsupported file type: ${file.extension}")
                )
            }

            val uuid = photoRepository.safeImportPhotoFile(file, fileName, type)

            if (uuid.isEmpty()) {
                Result.failure(IOException("Could not import $fileName into the vault"))
            } else {
                Result.success(Unit)
            }
        }
}
