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

package dev.leonlatsch.photok.encryption.domain.crypto

import dev.leonlatsch.photok.encryption.domain.models.Kdf
import java.security.SecureRandom
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class KeyGen @Inject constructor() {

    fun generateVaultMasterKey(): SecretKey {
        val keyBytes = ByteArray(32) // 256-bit
        SecureRandom().nextBytes(keyBytes)
        return SecretKeySpec(keyBytes, "AES")
    }

    fun derivePasswordKeyEncryptionKey(
        password: String,
        salt: ByteArray,
        kdf: Kdf,
        kdfIterations: Int,
        keySize: Int,
    ): SecretKey {
        val factory = SecretKeyFactory.getInstance(kdf.value)
        val spec = PBEKeySpec(password.toCharArray(), salt, kdfIterations, keySize)
        val keyBytes = factory.generateSecret(spec).encoded

        return SecretKeySpec(keyBytes, "AES")
    }
}