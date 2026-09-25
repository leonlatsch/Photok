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

package dev.leonlatsch.photok.encryption.domain.handlers

import dev.leonlatsch.photok.encryption.domain.crypto.VAULT_MASTER_KEY_SIZE
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * Turns the bytes that came out of unwrapping into the vault master key.
 *
 * AES/CBC/PKCS7 is unauthenticated, so unwrapping with the wrong key normally fails on the
 * padding — but roughly one in 256 wrong keys happens to produce a byte sequence that reads as
 * valid padding, and the unwrap then quietly returns a key of the wrong length. Without this
 * check that looks like a successful unlock, right up to the point where every file decrypts to
 * noise. Checking the length pushes that from one in 256 down to negligible: hitting the right
 * length needs a full block of valid padding.
 */
fun toVaultMasterKey(vmkBytes: ByteArray): SecretKey {
    require(vmkBytes.size == VAULT_MASTER_KEY_SIZE) {
        "Unwrapped vault master key is ${vmkBytes.size} bytes, expected $VAULT_MASTER_KEY_SIZE"
    }

    return SecretKeySpec(vmkBytes, "AES")
}
