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

package dev.leonlatsch.photok.model.domain

import java.io.File

/**
 * Proxy for the vault import machinery, which lives in the app module.
 *
 * Declared here and implemented in the app module, so feature modules
 * (e.g. :pro) can import into the vault without depending on the app module.
 *
 * FIXME: This proxy only exists because of the current app/core module split.
 *  PhotoRepository and its dependencies (IO, CreateThumbnailsUseCase, ImageStorage,
 *  file metadata helpers) still live in :app, so :pro cannot reach them directly.
 *  Move them into :core and let feature modules use PhotoRepository itself,
 *  then delete this interface and its app side implementation.
 */
interface PhotoImportProxy {
    suspend fun importFile(file: File, fileName: String): Result<Unit>
}
