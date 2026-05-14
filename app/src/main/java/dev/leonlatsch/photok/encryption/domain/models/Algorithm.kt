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

package dev.leonlatsch.photok.encryption.domain.models

import android.security.keystore.KeyProperties

enum class Algorithm(val value: String, val padding: String, val blockMode: String) {
    AesCbcPkcs7Padding(
        value = "AES/CBC/PKCS7Padding",
        padding = KeyProperties.ENCRYPTION_PADDING_PKCS7,
        blockMode = KeyProperties.BLOCK_MODE_CBC
    )
}

enum class EncryptionVersionByte(val value: Byte) {
    One(0x01),
    Two(0x02);

    companion object {
        fun fromValue(value: Byte): EncryptionVersionByte {
            return entries.find { it.value == value } ?: error("Unknown version byte: $value")
        }
    }
}