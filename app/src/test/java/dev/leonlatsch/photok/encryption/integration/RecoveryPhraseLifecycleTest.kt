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

package dev.leonlatsch.photok.encryption.integration

import dev.leonlatsch.photok.encryption.domain.RecoveryPhraseStore
import dev.leonlatsch.photok.encryption.domain.crypto.Bip39MnemonicGenerator
import dev.leonlatsch.photok.encryption.domain.crypto.Bip39WordCount
import dev.leonlatsch.photok.encryption.domain.crypto.KeyGen
import dev.leonlatsch.photok.encryption.domain.handlers.RecoveryPhraseVaultProtectionHandler
import dev.leonlatsch.photok.encryption.domain.models.CreateRequest
import dev.leonlatsch.photok.encryption.domain.models.RecoveryPhrase
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import dev.leonlatsch.photok.encryption.domain.models.VaultSession
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * End-to-end tests for the recovery phrase vault protection flow.
 * A regression here means users cannot recover their vault with a valid phrase.
 */
@RunWith(RobolectricTestRunner::class)
class RecoveryPhraseLifecycleTest {

    private val keyGen = KeyGen()
    private val fixedPhrase = RecoveryPhrase(List(12) { "abandon" } + listOf("about"))

    private val mockMnemonicGenerator = mockk<Bip39MnemonicGenerator>()
    private val noopStore = object : RecoveryPhraseStore {
        override fun store(phrase: RecoveryPhrase, session: VaultSession) = Unit
        override fun observe(session: VaultSession): Flow<RecoveryPhrase?> = emptyFlow()
        override fun clear() = Unit
    }

    private val handler = RecoveryPhraseVaultProtectionHandler(keyGen, mockMnemonicGenerator, noopStore)

    @Before
    fun setup() {
        every { mockMnemonicGenerator.generate(any()) } returns fixedPhrase.words
    }

    /**
     * Creating a protection and unlocking with the same phrase must return the original VMK.
     * This is the core recovery guarantee: the phrase must restore access to all encrypted files.
     */
    @Test
    fun `correct phrase unlocks vault and recovers original VMK`() = runTest {
        val vmk = keyGen.generateVaultMasterKey()
        val session = VaultSession(vmk)

        val protection = handler.create(CreateRequest.RecoveryPhrase(session, Bip39WordCount.Twelve))
        val recovered = handler.unlock(UnlockRequest.RecoveryPhrase(fixedPhrase), protection)

        assertArrayEquals(
            "Correct phrase must recover the exact original VMK",
            vmk.encoded,
            recovered.encoded,
        )
    }

    /**
     * Two create() calls with the same phrase must produce different wrappedVMK blobs
     * (each call uses a fresh random salt and IV).
     */
    @Test
    fun `each create produces unique salt and IV`() = runTest {
        val session = VaultSession(keyGen.generateVaultMasterKey())

        val p1 = handler.create(CreateRequest.RecoveryPhrase(session, Bip39WordCount.Twelve))
        val p2 = handler.create(CreateRequest.RecoveryPhrase(session, Bip39WordCount.Twelve))

        assertTrue("Salt must differ between create calls", p1.params.salt != p2.params.salt)
        assertTrue("IV must differ between create calls", p1.params.iv != p2.params.iv)
    }

    /**
     * A wrong phrase must never unlock the vault — it must always fail.
     */
    @Test
    fun `wrong phrase cannot unlock vault`() = runTest {
        val session = VaultSession(keyGen.generateVaultMasterKey())
        val protection = handler.create(CreateRequest.RecoveryPhrase(session, Bip39WordCount.Twelve))

        val wrongPhrase = RecoveryPhrase(List(12) { "zoo" })
        val result = runCatching {
            handler.unlock(UnlockRequest.RecoveryPhrase(wrongPhrase), protection)
        }

        assertTrue("Wrong phrase must fail to unlock the vault", result.isFailure)
    }
}
