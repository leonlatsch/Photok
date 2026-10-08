/*
 *   Copyright 2020–2026 Leon Latsch
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

package dev.leonlatsch.photok.model.io

import dev.leonlatsch.photok.model.io.ThumbnailFiles.VERSION
import java.io.File

/**
 * Layout of the thumbnails in the cache dir.
 *
 * Bump [VERSION] when the thumbnail format changes. Outdated versions are deleted and recreated by [ThumbnailMaintainer].
 */
object ThumbnailFiles {
    const val DIR = "thumbnails"
    const val VERSION = 1
    const val CURRENT_DIR = "$DIR/v$VERSION"

    private const val EXTENSION = ".jpg"
    private const val TMP_SUFFIX = ".tmp"

    fun path(uuid: String) = "$CURRENT_DIR/$uuid$EXTENSION"

    fun tmpPath(uuid: String) = "$CURRENT_DIR/$uuid$EXTENSION$TMP_SUFFIX"

    fun pathInCurrentDir(file: File) = "$CURRENT_DIR/${file.name}"

    fun uuidOf(file: File) = file.name.substringBefore(".")

    fun isTmp(file: File) = file.name.endsWith(TMP_SUFFIX)
}
