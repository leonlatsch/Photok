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
 * Counts restore progress for one backup.
 *
 * Progress bytes are the sizes declared in the backup metadata, so the total is known
 * upfront and the progress lands exactly on 100%. The speed is measured on the bytes
 * actually copied instead, because the declared sizes are missing in old backups.
 *
 * Thumbnail and video preview entries belong to their photo and are not counted separately.
 */
class RestoreProgressTracker(
    photos: List<PhotoBackup>,
    private val now: () -> Long = System::currentTimeMillis,
) {

    private val filesTotal = photos.size
    private val bytesTotal = photos.sumOf { it.size }

    private val startedAt = now()

    private var filesDone = 0
    private var completedBytes = 0L

    private var filesStarted = 0

    /** Kept after the file finished, so the unthrottled finish emit still names it. */
    private var currentFile: RestoreProgress.CurrentFile? = null

    private var currentFileSize = 0L
    private var currentFileBytes = 0L

    /** Bytes taken off the stream, sidecars included. Unlike the declared sizes always real. */
    private var copiedBytes = 0L

    private var lastEmitAt = 0L

    fun startFile(photo: PhotoBackup) {
        currentFileSize = photo.size
        currentFileBytes = 0L

        filesStarted++
        currentFile = RestoreProgress.CurrentFile(filesStarted, photo.fileName)
    }

    /** Returns `null` while throttled, so callers only emit every [EMIT_INTERVAL_MILLIS]. */
    fun advance(chunk: Long): RestoreProgress.Restoring? {
        copiedBytes += chunk
        currentFileBytes = (currentFileBytes + chunk).coerceAtMost(currentFileSize)

        val now = now()
        if (now - lastEmitAt < EMIT_INTERVAL_MILLIS) return null

        lastEmitAt = now
        return snapshot()
    }

    /** Counts bytes of a sidecar towards the speed, but not towards the progress. */
    fun advanceSidecar(chunk: Long) {
        copiedBytes += chunk
    }

    fun finishFile(): RestoreProgress.Restoring {
        filesDone++
        completedBytes += currentFileSize
        currentFileSize = 0L
        currentFileBytes = 0L
        lastEmitAt = now()

        return snapshot()
    }

    fun snapshot(): RestoreProgress.Restoring {
        val elapsed = now() - startedAt
        val bytesDone = completedBytes + currentFileBytes

        return RestoreProgress.Restoring(
            filesDone = filesDone,
            filesTotal = filesTotal,
            bytesDone = bytesDone,
            bytesTotal = bytesTotal,
            bytesPerSecond = if (elapsed > 0) copiedBytes * 1000 / elapsed else 0,
            millisRemaining = millisRemaining(elapsed, bytesDone),
            currentFile = currentFile,
        )
    }

    /**
     * Estimated from the declared sizes while they look usable, from the file count
     * otherwise. Old backups declare a size of 0 for some photos, which would leave
     * [bytesDone] stuck at [bytesTotal] and the estimate at zero for the whole restore.
     */
    private fun millisRemaining(elapsed: Long, bytesDone: Long): Long? = when {
        filesDone == 0 -> null

        bytesDone in 1 until bytesTotal -> elapsed * (bytesTotal - bytesDone) / bytesDone

        else -> elapsed * (filesTotal - filesDone) / filesDone
    }
}
