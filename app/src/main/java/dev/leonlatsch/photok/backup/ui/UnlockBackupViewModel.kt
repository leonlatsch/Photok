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

package dev.leonlatsch.photok.backup.ui

import android.app.Application
import androidx.databinding.Bindable
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.backup.data.toDomain
import dev.leonlatsch.photok.backup.domain.BackupMetaData
import dev.leonlatsch.photok.other.extensions.empty
import dev.leonlatsch.photok.uicomponnets.bindings.ObservableViewModel
import dev.leonlatsch.photok.vaults.domain.VaultService
import org.mindrot.jbcrypt.BCrypt
import javax.inject.Inject

/**
 * ViewModel for unlocking a Backup.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@HiltViewModel
class UnlockBackupViewModel @Inject constructor(
    app: Application,
    private val vaultService: VaultService,
) : ObservableViewModel(app) {

    @Bindable
    var password: String = String.empty
        set(value) {
            field = value
            notifyChange(BR.password, value)
        }

    // Ugly but fuck it. This will be redone anyway
    suspend fun verifyPassword(backupMetaData: BackupMetaData): Boolean =
        when (backupMetaData) {
            is BackupMetaData.V1 -> BCrypt.checkpw(password, backupMetaData.password)
            is BackupMetaData.V2 -> BCrypt.checkpw(password, backupMetaData.password)
            is BackupMetaData.V3 -> BCrypt.checkpw(password, backupMetaData.password)
            is BackupMetaData.V4 -> BCrypt.checkpw(password, backupMetaData.password)
            is BackupMetaData.V5 -> {
                vaultService.unlock(backupMetaData.vault.toDomain(), password).isSuccess
            }
        }
}