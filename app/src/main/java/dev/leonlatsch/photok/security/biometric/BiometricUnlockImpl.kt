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

package dev.leonlatsch.photok.security.biometric

import android.content.res.Resources
import androidx.fragment.app.Fragment
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.settings.data.Config
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricUnlockImpl @Inject constructor(
    private val config: Config,
    private val resources: Resources,
    private val encryptionManager: EncryptionManager,
    private val biometricKeyStore: BiometricKeyStore,
    private val unlockCipher: UnlockCipherUseCase,
) : BiometricUnlock {

    override val isAvailableAndSetup: Boolean
        get() = config.biometricAuthenticationEnabled && biometricKeyStore.userKeyExists()

    override suspend fun setup(fragment: Fragment): Result<Unit> {
        val currentUserKey = encryptionManager.getKeyOrNull()
        if (currentUserKey == null) {
            return Result.failure(IllegalStateException("EncryptionManager not ready"))
        }

        val encryptionCipher = biometricKeyStore.getEncryptionCipher().onFailure {
            Timber.e("Getting encryption cipher failed: $it")
            return Result.failure(it)
        }.getOrThrow()

        val unlockedCipher = unlockCipher(
            fragment = fragment,
            cipher = encryptionCipher,
            title = resources.getString(R.string.biometric_unlock_setup_title),
            subtitle = resources.getString(R.string.biometric_unlock_setup_subtitle),
            negativeButtonText = resources.getString(R.string.common_cancel),
        ).onFailure {
            Timber.e("Unlocking cipher failed: $it")
            return Result.failure(it)
        }.getOrThrow()

        return biometricKeyStore.encryptUserKey(currentUserKey, unlockedCipher).onFailure {
                Timber.e("Encrypting user key failed: $it")
            }
    }

    override suspend fun unlock(fragment: Fragment): Result<Unit> {
        val encryptionCipher = biometricKeyStore.getDecryptionCipher().onFailure {
            Timber.e("Getting decryption cipher failed: $it")
            return Result.failure(it)
        }.getOrThrow()

        val unlockedCipher = unlockCipher(
            fragment = fragment,
            cipher = encryptionCipher,
            title = resources.getString(R.string.biometric_unlock_title),
            subtitle = resources.getString(R.string.biometric_unlock_subtitle),
            negativeButtonText = resources.getString(R.string.biometric_unlock_cancel),
        ).onFailure {
            Timber.e("Unlocking cipher failed: $it")
            return Result.failure(it)
        }.getOrThrow()

        val userKey = biometricKeyStore.decryptUserKey(unlockedCipher).onFailure {
            Timber.e("Decrypting user key failed: $it")
            return Result.failure(it)
        }.getOrThrow()

        return encryptionManager.initialize(userKey)
    }

    override suspend fun reset(): Result<Unit> = runCatching {
        biometricKeyStore.removeStoredUserKey()
    }
}

