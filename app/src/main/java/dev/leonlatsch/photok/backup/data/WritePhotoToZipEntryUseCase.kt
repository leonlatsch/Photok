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

package dev.leonlatsch.photok.backup.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.io.EncryptedStorageManager
import dev.leonlatsch.photok.model.io.IO
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class WritePhotoToZipEntryUseCase @Inject constructor(
    private val encryptedStorageManager: EncryptedStorageManager,
    private val io: IO,
    @ApplicationContext private val context: Context,
) {
    suspend operator fun invoke(
        photo: Photo,
        zipOutputStream: ZipOutputStream,
    ): Result<Unit> {
        context.fileList()
            .filter { it.contains(photo.uuid) }
            .map { it to encryptedStorageManager.internalOpenFileInput(it) }
            .forEach { file ->
                val filename = file.first
                val inputStream = file.second

                inputStream
                    ?: return Result.failure(IllegalStateException("Input stream missing for photo"))

                io.zip.writeZipEntry(filename, inputStream, zipOutputStream)
                    .onFailure {
                        return Result.failure(it)
                    }
            }

        return Result.success(Unit)
    }
}