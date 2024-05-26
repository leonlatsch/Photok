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

import dev.leonlatsch.photok.other.extensions.lazyClose
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BackupLocalDataSource @Inject constructor() {

    suspend fun writeZipEntry(
        filename: String,
        input: InputStream,
        zipOutputStream: ZipOutputStream,
    ): Result<Unit> = suspendCoroutine { continuation ->
        try {
            val entry = ZipEntry(filename)
            zipOutputStream.putNextEntry(entry)

            input.copyTo(zipOutputStream)

            input.lazyClose()
            zipOutputStream.closeEntry()
            continuation.resume(Result.success(Unit))
        } catch (e: IOException) {
            continuation.resume(Result.failure(e))
        }
    }
}