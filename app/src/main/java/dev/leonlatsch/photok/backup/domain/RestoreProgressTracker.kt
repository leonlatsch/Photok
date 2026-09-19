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

package dev.leonlatsch.photok.backup.domain

import dev.leonlatsch.photok.backup.data.PhotoBackup

/** Emitting on every copied chunk would produce thousands of elements per second. */
private const val EMIT_INTERVAL_MILLIS = 100L

/**
 * Counts restore progress for one backup. Bytes are the sizes declared in the backup
 * metadata, not the bytes actually written, so the total is known upfront and the progress
 * lands exactly on 100%.
 *
 * Thumbnail (`.tn`) and video preview (`.vp`) entries belong to their photo and are not
 * counted separately.
 */
class RestoreProgressTracker(photos: List<PhotoBackup>) {

    private val filesTotal = photos.size
    private val bytesTotal = photos.sumOf { it.size }

    private var filesDone = 0
    private var completedBytes = 0L

    private var currentFileSize = 0L
    private var currentFileBytes = 0L

    private var lastEmitAt = 0L

    fun startFile(photo: PhotoBackup) {
        currentFileSize = photo.size
        currentFileBytes = 0L
    }

    /** Returns `null` while throttled, so callers only emit every [EMIT_INTERVAL_MILLIS]. */
    fun advance(chunk: Long): RestoreProgress.Restoring? {
        currentFileBytes = (currentFileBytes + chunk).coerceAtMost(currentFileSize)

        val now = System.currentTimeMillis()
        if (now - lastEmitAt < EMIT_INTERVAL_MILLIS) return null

        lastEmitAt = now
        return snapshot()
    }

    fun finishFile(): RestoreProgress.Restoring {
        filesDone++
        completedBytes += currentFileSize
        currentFileSize = 0L
        currentFileBytes = 0L
        lastEmitAt = System.currentTimeMillis()

        return snapshot()
    }

    fun snapshot() = RestoreProgress.Restoring(
        filesDone = filesDone,
        filesTotal = filesTotal,
        bytesDone = completedBytes + currentFileBytes,
        bytesTotal = bytesTotal,
    )
}
