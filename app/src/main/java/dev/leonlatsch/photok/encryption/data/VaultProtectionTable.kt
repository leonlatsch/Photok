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

package dev.leonlatsch.photok.encryption.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class VaultProtectionType {
    Password,
    Biometric,
    // Recovery Key
}

data class WrappedVMKJson(
    val cipherText: String,
    val salt: String,
    val kdf: String,
    val kdfIterations: String,
    val algorithm: String,
    val version: Int = 1,
)

@Entity(tableName = "vault_protection")
data class VaultProtectionTable(
    @PrimaryKey
    val id: String,
    val type: VaultProtectionType,
    val wrappedVMK: WrappedVMKJson,
)