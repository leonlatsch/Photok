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

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject

private const val AndroidKeyStore = "AndroidKeyStore"
private const val KeyAlias = "PhotokEncryptionKey"

class KeyStorage @Inject constructor(@ApplicationContext context: Context) {

    fun getOrCreateKey(): Result<SecretKey> {
        val storedKey = loadAESKey()

        if (storedKey != null) {
            return Result.success(storedKey)
        }

        val newKey = generateAESKey()
        if (newKey != null) {
            return Result.success(newKey)
        }

        return Result.failure(Exception("Could not load or create encryption key"))
    }

    private fun generateAESKey(): SecretKey? = try {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, AndroidKeyStore)

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(KeyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT, )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    } catch (e: Exception) {
        null
    }

    private fun loadAESKey(): SecretKey? = try {
        val keyStore = KeyStore.getInstance(AndroidKeyStore).apply {
            load(null)
        }

        val secretKeyEntry = keyStore.getEntry(KeyAlias, null) as KeyStore.SecretKeyEntry
        secretKeyEntry.secretKey
    } catch (e: Exception) {
        null
    }
}