package dev.leonlatsch.photok.security.biometric

import android.content.Context
import android.content.res.Resources
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.fragment.app.Fragment
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.settings.data.Config
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [BiometricUnlock] for managing biometric setup and unlock.
 *
 * <p>Uses [BiometricKeyStore], [EncryptionManager], and [UnlockCipherUseCase]
 * to securely store and retrieve the user’s encryption key with biometric protection.</p>
 */
@Singleton
class BiometricUnlockImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val config: Config,
    private val resources: Resources,
    private val encryptionManager: EncryptionManager,
    private val biometricKeyStore: BiometricKeyStore,
    private val unlockCipher: UnlockCipherUseCase,
) : BiometricUnlock {

    override fun areBiometricsAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
    }

    override fun isSetupAndValid(): Boolean {
        val keyStoreValid = biometricKeyStore.validate()

        if (!keyStoreValid) {
            config.biometricAuthenticationEnabled = false
            biometricKeyStore.reset()
        }

        return keyStoreValid && config.biometricAuthenticationEnabled
    }

    override suspend fun setup(fragment: Fragment): Result<Unit> {
        val currentUserKey = encryptionManager.getKeyOrNull()
        if (currentUserKey == null) {
            return Result.failure(IllegalStateException("EncryptionManager not ready"))
        }

        biometricKeyStore.reset() // Reset before attempt to setup

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
        config.biometricAuthenticationEnabled = false
        biometricKeyStore.reset()
    }
}

