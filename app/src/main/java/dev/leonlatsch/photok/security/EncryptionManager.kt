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

package dev.leonlatsch.photok.security

import java.io.InputStream
import java.io.OutputStream
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey

interface EncryptionManager {
    val isReady: Boolean
    var keyCacheEnabled: Boolean
    fun initialize(password: String): Result<Unit>
    fun initialize(key: SecretKey): Result<Unit>
    fun reset()
    fun getKeyOrNull(): SecretKey?

    fun createCipherInputStream(
        input: InputStream,
        password: String? = null,
    ): CipherInputStream?
    fun createCipherOutputStream(
        output: OutputStream,
        password: String? = null,
    ): CipherOutputStream?
}