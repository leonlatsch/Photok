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

package dev.leonlatsch.photok.onboarding.ui.recoveryphrase

import dev.leonlatsch.photok.encryption.domain.RecoveryPhraseStore
import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.encryption.domain.VaultService
import dev.leonlatsch.photok.encryption.domain.crypto.Bip39WordCount
import dev.leonlatsch.photok.encryption.domain.models.CreateRequest
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionType
import dev.leonlatsch.photok.encryption.domain.models.VaultSession
import dev.leonlatsch.photok.recovery.ui.RecoveryPhraseExporter
import dev.leonlatsch.photok.recovery.ui.RecoveryPhraseSavedTo
import dev.leonlatsch.photok.telemetry.domain.Signal
import dev.leonlatsch.photok.telemetry.domain.TelemetryService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingRecoveryPhraseViewModelTest {

    private val session = mockk<VaultSession>()
    private val sessionRepository = mockk<SessionRepository>(relaxed = true)
    private val recoveryPhraseStore = mockk<RecoveryPhraseStore>(relaxed = true)
    private val vaultService = mockk<VaultService>(relaxed = true)
    private val exporter = mockk<RecoveryPhraseExporter>(relaxed = true)
    private val telemetryService = mockk<TelemetryService>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { sessionRepository.get() } returns session
        every { sessionRepository.observe() } returns MutableStateFlow(session)
        every { recoveryPhraseStore.observe(session) } returns flowOf(null)
        every { exporter.sharedTo } returns emptyFlow()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `creates a recovery phrase when none is set up`() {
        coEvery { vaultService.isSetup(VaultProtectionType.RecoveryPhrase) } returns false

        createViewModel()

        coVerify { vaultService.create(CreateRequest.RecoveryPhrase(session, Bip39WordCount.Twelve)) }
    }

    @Test
    fun `keeps an existing recovery phrase`() {
        coEvery { vaultService.isSetup(VaultProtectionType.RecoveryPhrase) } returns true

        createViewModel()

        coVerify(exactly = 0) { vaultService.create(any()) }
    }

    @Test
    fun `a screenshot counts as saved only on the qr code side`() = runTest {
        val viewModel = createViewModel()

        viewModel.handleUiEvent(OnboardingRecoveryPhraseUiEvent.ScreenshotTaken)
        assertNull(viewModel.uiState.first { !it.showQrCode }.savedTo)

        viewModel.handleUiEvent(OnboardingRecoveryPhraseUiEvent.ShowQrCodeSide)
        viewModel.handleUiEvent(OnboardingRecoveryPhraseUiEvent.ScreenshotTaken)

        assertEquals(
            RecoveryPhraseSavedTo.Screenshot,
            viewModel.uiState.first { it.savedTo != null }.savedTo,
        )
    }

    @Test
    fun `changing the word count creates a new phrase and resets saved state`() = runTest {
        val viewModel = createViewModel()
        viewModel.handleUiEvent(OnboardingRecoveryPhraseUiEvent.ShowQrCodeSide)
        viewModel.handleUiEvent(OnboardingRecoveryPhraseUiEvent.ScreenshotTaken)

        viewModel.handleUiEvent(OnboardingRecoveryPhraseUiEvent.UpdateWordCount(Bip39WordCount.TwentyFour))

        val state = viewModel.uiState.first { it.wordCount == Bip39WordCount.TwentyFour && !it.loading }
        assertNull(state.savedTo)
        verify { recoveryPhraseStore.clear() }
        coVerify { vaultService.create(CreateRequest.RecoveryPhrase(session, Bip39WordCount.TwentyFour)) }
    }

    @Test
    fun `continue reports where the phrase was saved`() = runTest {
        val viewModel = createViewModel()
        viewModel.handleUiEvent(OnboardingRecoveryPhraseUiEvent.ShowQrCodeSide)
        viewModel.handleUiEvent(OnboardingRecoveryPhraseUiEvent.ScreenshotTaken)

        viewModel.handleUiEvent(OnboardingRecoveryPhraseUiEvent.Continue)

        assertEquals(OnboardingRecoveryPhraseNavigationEvent.Continue, viewModel.navigationEvents.first())
        verify {
            telemetryService.signal(
                Signal.OnboardingRecoveryPhraseContinueClicked,
                mapOf("savedTo" to "screenshot", "wordCount" to "12"),
            )
        }
    }

    private fun createViewModel() = OnboardingRecoveryPhraseViewModel(
        sessionRepository = sessionRepository,
        recoveryPhraseStore = recoveryPhraseStore,
        vaultService = vaultService,
        screenshotOverride = mockk(relaxed = true),
        exporter = exporter,
        telemetryService = telemetryService,
    )
}
