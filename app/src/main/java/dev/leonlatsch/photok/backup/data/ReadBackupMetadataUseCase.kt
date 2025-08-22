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

import com.google.gson.Gson
import java.util.zip.ZipInputStream
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ReadBackupMetadataUseCase @Inject constructor(
    private val gson: Gson
) {
    suspend operator fun invoke(zipInputStream: ZipInputStream): BackupMetaData =
        suspendCoroutine { continuation ->
            val bytes = zipInputStream.readBytes()
            val string = String(bytes)

            val metaData = gson.fromJson(string, BackupMetaData::class.java)
            metaData ?: error("Error reading meta json from $zipInputStream")

            continuation.resume(metaData)
        }
}