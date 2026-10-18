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

import dev.leonlatsch.photok.pro.navigation.ProRoute
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.telemetry.domain.Signal
import dev.leonlatsch.photok.telemetry.domain.TelemetryService
import io.mockk.mockk
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class OnboardingViewModelTest {

    private val context = RuntimeEnvironment.getApplication()
    private val config = Config(context)
    private val telemetryService = mockk<TelemetryService>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `starts at welcome without a saved step`() {
        config.systemOnboardingStep = null

        assertEquals(listOf(OnboardingRoute.Welcome), createViewModel().initialBackStack)
    }

    @Test
    fun `resumes at the saved step`() {
        config.systemOnboardingStep = OnboardingRoute.AddFirstPhotos.stepKey()

        assertEquals(listOf(OnboardingRoute.AddFirstPhotos), createViewModel().initialBackStack)
    }

    @Test
    fun `resumes at the pro offer`() {
        config.systemOnboardingStep = ProRoute.OnboardingOffer.stepKey()

        assertEquals(listOf(ProRoute.OnboardingOffer), createViewModel().initialBackStack)
    }

    @Test
    fun `starts at welcome with an unknown saved step`() {
        config.systemOnboardingStep = "removedStep"

        assertEquals(listOf(OnboardingRoute.Welcome), createViewModel().initialBackStack)
    }

    @Test
    fun `persists the current step`() {
        val viewModel = createViewModel()

        viewModel.handleUiEvent(OnboardingUiEvent.StepChanged(OnboardingRoute.BiometricUnlock))

        assertEquals(OnboardingRoute.BiometricUnlock.stepKey(), config.systemOnboardingStep)
    }

    @Test
    fun `signals a step only once when it is reported again`() {
        val viewModel = createViewModel()

        viewModel.handleUiEvent(OnboardingUiEvent.StepChanged(OnboardingRoute.SetupPassword))
        viewModel.handleUiEvent(OnboardingUiEvent.StepChanged(OnboardingRoute.SetupPassword))

        verify(exactly = 1) { telemetryService.signal(Signal.OnboardingPasswordShown, any()) }
    }

    @Test
    fun `marks only the resumed step as resumed`() {
        config.systemOnboardingStep = OnboardingRoute.RecoveryPhraseSetup.stepKey()
        val viewModel = createViewModel()

        viewModel.handleUiEvent(OnboardingUiEvent.StepChanged(OnboardingRoute.RecoveryPhraseSetup))
        viewModel.handleUiEvent(OnboardingUiEvent.StepChanged(OnboardingRoute.AddFirstPhotos))

        verify {
            telemetryService.signal(
                Signal.OnboardingRecoveryPhraseShown,
                mapOf("resumed" to "true"),
            )
            telemetryService.signal(
                Signal.OnboardingAddPhotosShown,
                mapOf("resumed" to "false"),
            )
        }
    }

    @Test
    fun `finish completes the onboarding`() = runTest {
        config.systemFirstStart = true
        config.systemOnboardingStep = OnboardingRoute.Done.stepKey()
        val viewModel = createViewModel()

        viewModel.handleUiEvent(OnboardingUiEvent.Finish)

        assertEquals(OnboardingNavigationEvent.Finished, viewModel.navigationEvents.first())
        assertFalse(config.systemFirstStart)
        assertNull(config.systemOnboardingStep)
        verify { telemetryService.signal(Signal.OnboardingFinished, any()) }
    }

    private fun createViewModel() = OnboardingViewModel(
        config = config,
        vaultService = mockk(relaxed = true),
        sessionRepository = mockk(relaxed = true),
        telemetryService = telemetryService,
        context = context,
    )
}
