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

package dev.leonlatsch.photok.model.repositories

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.model.database.entity.LEGACY_PHOTOK_FILE_EXTENSION
import dev.leonlatsch.photok.model.database.entity.PHOTOK_FILE_EXTENSION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CleanupDeadFilesUseCase @Inject constructor(
    private val photoRepository: PhotoRepository,
    @ApplicationContext private val context: Context,
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    operator fun invoke() {
        scope.launch {
            val allExisting = photoRepository.getAll()

            val allFiles = context.fileList().filter {
                it.contains(LEGACY_PHOTOK_FILE_EXTENSION) || it.contains(PHOTOK_FILE_EXTENSION)
            }

            for (file in allFiles) {
                if (allExisting.none { file.contains(it.uuid) }) {
                    Timber.i("Deleting dead file: $file")
                    context.deleteFile(file)
                }
            }
        }
    }
}