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

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.encryption.domain.RecoveryPhraseStore
import dev.leonlatsch.photok.encryption.domain.crypto.IV_SIZE
import dev.leonlatsch.photok.encryption.domain.models.Algorithm
import dev.leonlatsch.photok.encryption.domain.models.RecoveryPhrase
import dev.leonlatsch.photok.encryption.domain.models.VaultSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64

private const val PREFS_FILE = "recovery_phrase_store"
private const val KEY_IV = "rp_iv"
private const val KEY_CIPHERTEXT = "rp_ciphertext"

@Singleton
class RecoveryPhraseStoreImpl @Inject constructor(
    @ApplicationContext context: Context,
): RecoveryPhraseStore {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)

    override fun store(phrase: RecoveryPhrase, session: VaultSession) {
        try {
            val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }
            val cipher = Cipher.getInstance(Algorithm.AesCbcPkcs7Padding.value).apply {
                init(Cipher.ENCRYPT_MODE, session.vmk, IvParameterSpec(iv))
            }
            val ciphertext = cipher.doFinal(phrase.toMnemonicString().toByteArray(Charsets.UTF_8))
            prefs.edit {
                putString(KEY_IV, Base64.encode(iv))
                putString(KEY_CIPHERTEXT, Base64.encode(ciphertext))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to store recovery phrase")
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun observe(session: VaultSession): Flow<RecoveryPhrase?> = callbackFlow {
        send(load(session))

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            scope.launch { send(load(session)) }
        }

        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    private fun load(session: VaultSession): RecoveryPhrase? {
        val ivStr = prefs.getString(KEY_IV, null) ?: return null
        val ciphertextStr = prefs.getString(KEY_CIPHERTEXT, null) ?: return null

        return try {
            val iv = Base64.decode(ivStr)
            val ciphertext = Base64.decode(ciphertextStr)
            val cipher = Cipher.getInstance(Algorithm.AesCbcPkcs7Padding.value).apply {
                init(Cipher.DECRYPT_MODE, session.vmk, IvParameterSpec(iv))
            }
            val plaintext = cipher.doFinal(ciphertext)
            val string = String(plaintext, Charsets.UTF_8)
            RecoveryPhrase.from(string)
        } catch(e: Exception) {
            Timber.e(e, "Failed to load recovery phrase")
            null
        }
    }

    override fun clear() {
        prefs.edit {
            remove(KEY_IV)
            remove(KEY_CIPHERTEXT)
        }
    }
}
