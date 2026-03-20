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

package dev.leonlatsch.photok.security

const val ENC_VERSION_BYTE: Byte = 0x01
const val IV_SIZE = 16
const val SALT_SIZE = 16
const val KEY_SIZE = 256
const val ITERATION_COUNT = 100_000
const val KEY_ALGORITHM = "PBKDF2WithHmacSHA256"
const val AES = "AES"
const val AES_ALGORITHM = "AES/CBC/PKCS7Padding"

const val HEADER_SIZE = 1 + SALT_SIZE + IV_SIZE
const val BLOCK_SIZE = 16

val VERIFIER_PLAINTEXT = "verifier".encodeToByteArray()