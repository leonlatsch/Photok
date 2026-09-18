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

package dev.leonlatsch.photok.encryption.domain.handlers

import dev.leonlatsch.photok.encryption.domain.RecoveryPhraseStore
import dev.leonlatsch.photok.encryption.domain.crypto.Bip39MnemonicGenerator
import dev.leonlatsch.photok.encryption.domain.crypto.IV_SIZE
import dev.leonlatsch.photok.encryption.domain.crypto.KeyGen
import dev.leonlatsch.photok.encryption.domain.crypto.SALT_SIZE
import dev.leonlatsch.photok.encryption.domain.models.Algorithm
import dev.leonlatsch.photok.encryption.domain.models.CreateRequest
import dev.leonlatsch.photok.encryption.domain.models.Kdf
import dev.leonlatsch.photok.encryption.domain.models.RecoveryPhrase
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import dev.leonlatsch.photok.encryption.domain.models.VaultProtection
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionParams
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import kotlin.io.encoding.Base64

private const val KEK_SIZE = 256
private const val KEK_ITERATIONS = 100_000

class RecoveryPhraseVaultProtectionHandler @Inject constructor(
    private val keyGen: KeyGen,
    private val mnemonicGenerator: Bip39MnemonicGenerator,
    private val recoveryPhraseStore: RecoveryPhraseStore,
) : VaultProtectionHandler<UnlockRequest.RecoveryPhrase, CreateRequest.RecoveryPhrase> {

    override suspend fun unlock(
        request: UnlockRequest.RecoveryPhrase,
        protection: VaultProtection,
    ): javax.crypto.SecretKey {
        val params = protection.params

        val salt = requireNotNull(params.salt)
        val kdf = requireNotNull(params.kdf)
        val kdfIterations = requireNotNull(params.kdfIterations)

        val kek = keyGen.derivePasswordKeyEncryptionKey(
            password = request.phrase.toMnemonicString(),
            salt = Base64.decode(salt),
            kdf = kdf,
            kdfIterations = kdfIterations,
            keySize = params.keySize,
        )

        val cipher = Cipher.getInstance(params.algorithm.value).apply {
            init(Cipher.DECRYPT_MODE, kek, IvParameterSpec(Base64.decode(params.iv)))
        }

        val vmkBytes = cipher.doFinal(protection.wrappedVMK)
        return SecretKeySpec(vmkBytes, "AES")
    }

    override suspend fun create(request: CreateRequest.RecoveryPhrase): VaultProtection {
        val vmk = request.session.vmk
        val phrase = RecoveryPhrase(mnemonicGenerator.generate(request.wordCount))

        val salt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }
        val kdf = Kdf.PBKDF2WithHmacSHA256

        val params = VaultProtectionParams(
            salt = Base64.encode(salt),
            iv = Base64.encode(iv),
            kdf = kdf,
            kdfIterations = KEK_ITERATIONS,
            algorithm = Algorithm.AesCbcPkcs7Padding,
            keySize = KEK_SIZE,
        )

        val kek = keyGen.derivePasswordKeyEncryptionKey(
            password = phrase.toMnemonicString(),
            salt = salt,
            kdf = kdf,
            kdfIterations = KEK_ITERATIONS,
            keySize = KEK_SIZE,
        )

        val cipher = Cipher.getInstance(params.algorithm.value).apply {
            init(Cipher.ENCRYPT_MODE, kek, IvParameterSpec(iv))
        }

        val wrappedVmk = cipher.doFinal(vmk.encoded)

        recoveryPhraseStore.store(phrase, request.session)

        return VaultProtection(
            id = UUID.randomUUID().toString(),
            type = request.protectionType,
            wrappedVMK = wrappedVmk,
            params = params,
        )
    }

    override suspend fun canMigrate(): Boolean = false

    override suspend fun migrate(request: UnlockRequest.RecoveryPhrase): VaultProtection {
        throw UnsupportedOperationException("RecoveryPhrase protection has no legacy migration path")
    }

    override suspend fun reset() {
        recoveryPhraseStore.clear()
    }
}
