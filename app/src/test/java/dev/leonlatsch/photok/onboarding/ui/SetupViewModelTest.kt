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

package dev.leonlatsch.photok.onboarding.ui

import android.app.Activity
import dev.leonlatsch.photok.encryption.domain.SessionRepository
import dev.leonlatsch.photok.encryption.domain.VaultService
import dev.leonlatsch.photok.encryption.domain.crypto.Bip39WordCount
import dev.leonlatsch.photok.encryption.domain.models.CreateRequest
import dev.leonlatsch.photok.encryption.domain.models.UnlockRequest
import dev.leonlatsch.photok.encryption.domain.models.VaultSession
import dev.leonlatsch.photok.other.extensions.areBiometricsAvailable
import dev.leonlatsch.photok.settings.data.Config
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SetupViewModelTest {

    private val config = Config(RuntimeEnvironment.getApplication())
    private val vaultService = mockk<VaultService>(relaxed = true)
    private val sessionRepository = mockk<SessionRepository>(relaxed = true)
    private val session = mockk<VaultSession>()
    private val activity = mockk<Activity>()

    private lateinit var viewModel: SetupViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        mockkStatic(EXTENSIONS)
        every { activity.areBiometricsAvailable() } returns true

        coEvery { vaultService.unlock(UnlockRequest.Password(PASSWORD)) } returns
            Result.success(session)

        viewModel = SetupViewModel(
            config = config,
            vaultService = vaultService,
            sessionRepository = sessionRepository,
            telemetryService = mockk(relaxed = true),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(EXTENSIONS)
    }

    @Test
    fun `hides the confirm field while the password is too short`() = runTest {
        viewModel.handleUiEvent(SetupUiEvent.PasswordChanged("abc"))

        val state = viewModel.uiState.first { it.password == "abc" }

        assertFalse(state.showConfirmPassword)
        assertFalse(state.canSetup)
    }

    @Test
    fun `can set up with matching valid passwords`() = runTest {
        enterPasswords(PASSWORD, PASSWORD)

        val state = viewModel.uiState.first { it.confirmPassword == PASSWORD }

        assertTrue(state.showConfirmPassword)
        assertFalse(state.passwordsMismatch)
        assertTrue(state.canSetup)
    }

    @Test
    fun `reports mismatching passwords`() = runTest {
        enterPasswords(PASSWORD, "different")

        val state = viewModel.uiState.first { it.confirmPassword == "different" }

        assertTrue(state.passwordsMismatch)
        assertFalse(state.canSetup)
    }

    @Test
    fun `clears the confirmation once the password gets too short`() = runTest {
        enterPasswords(PASSWORD, PASSWORD)
        viewModel.handleUiEvent(SetupUiEvent.PasswordChanged("abc"))

        val state = viewModel.uiState.first { it.password == "abc" }

        assertEquals("", state.confirmPassword)
    }

    @Test
    fun `estimates the strength of a valid password`() = runTest {
        viewModel.handleUiEvent(SetupUiEvent.PasswordChanged(PASSWORD))

        val state = viewModel.uiState.first { it.passwordStrength != null }

        assertEquals(PASSWORD, state.password)
    }

    @Test
    fun `setup creates the vault and continues to biometric unlock`() = runTest {
        enterPasswords(PASSWORD, PASSWORD)

        viewModel.handleUiEvent(SetupUiEvent.Setup(activity))

        assertEquals(
            SetupNavigationEvent.SetupBiometricUnlock,
            viewModel.navigationEvents.first(),
        )
        coVerify {
            vaultService.create(CreateRequest.Password.NewVault(PASSWORD))
            vaultService.create(CreateRequest.RecoveryPhrase(session, Bip39WordCount.Twelve))
        }
        verify { sessionRepository.set(session) }
        assertEquals(OnboardingRoute.BiometricUnlock.stepKey(), config.systemOnboardingStep)
        assertFalse(viewModel.uiState.first { !it.loading }.loading)
    }

    @Test
    fun `setup continues to recovery phrase without biometrics`() = runTest {
        every { activity.areBiometricsAvailable() } returns false
        enterPasswords(PASSWORD, PASSWORD)

        viewModel.handleUiEvent(SetupUiEvent.Setup(activity))

        assertEquals(
            SetupNavigationEvent.SetupRecoveryPhrase,
            viewModel.navigationEvents.first(),
        )
        assertEquals(OnboardingRoute.RecoveryPhraseSetup.stepKey(), config.systemOnboardingStep)
    }

    @Test
    fun `setup shows an error when unlocking fails`() = runTest {
        coEvery { vaultService.unlock(any()) } returns Result.failure(IllegalStateException())
        enterPasswords(PASSWORD, PASSWORD)

        viewModel.handleUiEvent(SetupUiEvent.Setup(activity))

        assertEquals(SetupNavigationEvent.ShowError, viewModel.navigationEvents.first())
        verify(exactly = 0) { sessionRepository.set(any()) }
    }

    @Test
    fun `setup with mismatching passwords keeps the form editable`() = runTest {
        enterPasswords(PASSWORD, "different")

        viewModel.handleUiEvent(SetupUiEvent.Setup(activity))
        viewModel.handleUiEvent(SetupUiEvent.ConfirmPasswordChanged(PASSWORD))

        val state = viewModel.uiState.first { it.confirmPassword == PASSWORD }
        assertFalse(state.loading)
        assertTrue(state.canSetup)
        coVerify(exactly = 0) { vaultService.create(any()) }
    }

    private fun enterPasswords(password: String, confirmPassword: String) {
        viewModel.handleUiEvent(SetupUiEvent.PasswordChanged(password))
        viewModel.handleUiEvent(SetupUiEvent.ConfirmPasswordChanged(confirmPassword))
    }

    private companion object {
        const val PASSWORD = "correct horse battery"
        const val EXTENSIONS = "dev.leonlatsch.photok.other.extensions.ExtensionsKt"
    }
}
