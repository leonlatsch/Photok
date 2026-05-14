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

package dev.leonlatsch.photok.encryption.domain

import dev.leonlatsch.photok.encryption.domain.models.LegacySession
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

private const val SHA_256 = "SHA-256"
private const val AES = "AES"
private const val AES_ALGORITHM = "AES/GCM/NoPadding"

class LegacyEncryption @Inject constructor() {

    fun obtainSession(password: String): LegacySession {
        val key = genSecKey(password)
        val iv = genLegacyIv(password)

        return LegacySession(
            key = key,
            iv = iv,
        )
    }

    private fun genSecKey(password: String): SecretKeySpec {
        val md = MessageDigest.getInstance(SHA_256)
        val bytes = md.digest(password.toByteArray(StandardCharsets.UTF_8))
        return SecretKeySpec(bytes, AES)
    }

    private fun genLegacyIv(password: String): IvParameterSpec {
        val iv = ByteArray(16)
        val charArray = password.toCharArray()
        val firstChars = charArray.take(16)
        for (i in firstChars.indices) {
            iv[i] = firstChars[i].toByte()
        }

        return IvParameterSpec(iv)
    }
}