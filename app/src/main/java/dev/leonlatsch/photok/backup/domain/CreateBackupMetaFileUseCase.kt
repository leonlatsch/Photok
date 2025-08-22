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

package dev.leonlatsch.photok.backup.domain

import dev.leonlatsch.photok.settings.data.Config
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class CreateBackupMetaFileUseCase @Inject constructor(
    private val dumpDatabaseUseCase: DumpDatabaseUseCase,
    private val backupRepository: BackupRepository,
    private val config: Config,
){
    suspend operator fun invoke(zipOutputStream: ZipOutputStream, version: Int): Result<Unit> {
        val dump = dumpDatabaseUseCase(config.securityPassword!!, version)
        return backupRepository.writeBackupMetadata(dump, zipOutputStream)
    }
}