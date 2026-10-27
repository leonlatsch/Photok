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

enum class KekSpec(
    val kdf: Kdf,
    val kdfIterations: Int,
    val keySize: Int,
    val algorithm: Algorithm,
) {
    Password(
        kdf = Kdf.PBKDF2WithHmacSHA256,
        kdfIterations = 300_000,
        keySize = 256,
        algorithm = Algorithm.AesCbcPkcs7Padding,
    ),

    // A BIP39 phrase has at least 128 bits of entropy, so more iterations add nothing.
    RecoveryPhrase(
        kdf = Kdf.PBKDF2WithHmacSHA256,
        kdfIterations = 100_000,
        keySize = 256,
        algorithm = Algorithm.AesCbcPkcs7Padding,
    );

    fun matches(params: VaultProtectionParams): Boolean {
        return params.kdf == kdf
            && params.kdfIterations == kdfIterations
            && params.keySize == keySize
            && params.algorithm == algorithm
    }
}
