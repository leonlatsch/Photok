/*
 *   Copyright 2020–2026 Leon Latsch
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

package dev.leonlatsch.photok.main.ui.navigation

import android.app.Application
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import dev.leonlatsch.photok.gallery.components.MultiSelectionBackHandler
import dev.leonlatsch.photok.gallery.components.MultiSelectionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ExitConfirmationBackHandlerTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var now = 0L
    private val confirmation = ExitConfirmation { now }
    private val enabled = mutableStateOf(true)
    private val configurationOrientation = mutableStateOf(Configuration.ORIENTATION_PORTRAIT)
    private val selection = MultiSelectionState(listOf("photo"))
    private var hints = 0
    private var exits = 0
    private var parentBackPresses = 0

    @Before
    fun setup() {
        composeRule.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.addCallback(
                composeRule.activity,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        parentBackPresses++
                    }
                },
            )
        }
        composeRule.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply {
                orientation = configurationOrientation.value
            }
            CompositionLocalProvider(
                LocalResetExitConfirmation provides confirmation::reset,
                LocalConfiguration provides configuration,
            ) {
                ExitConfirmationBackHandler(
                    enabled = enabled.value,
                    confirmation = confirmation,
                    onShowHint = { hints++ },
                    onExit = { exits++ },
                )
                MultiSelectionBackHandler(selection)
            }
        }
    }

    @Test
    fun `first back shows a hint without exiting`() {
        pressBack()

        assertEquals(1, hints)
        assertEquals(0, exits)
        assertEquals(0, parentBackPresses)
    }

    @Test
    fun `second back within two seconds exits`() {
        pressBack()
        now = 1_999L

        pressBack()

        assertEquals(1, hints)
        assertEquals(1, exits)
    }

    @Test
    fun `back after two seconds shows another hint and waits for confirmation`() {
        pressBack()
        now = 2_000L
        pressBack()

        assertEquals(2, hints)
        assertEquals(0, exits)

        now = 2_100L
        pressBack()
        assertEquals(1, exits)
    }

    @Test
    fun `returning to the start screen clears pending exit confirmation`() {
        pressBack()
        composeRule.runOnIdle { enabled.value = false }
        pressBack()
        assertEquals(1, parentBackPresses)
        composeRule.runOnIdle { enabled.value = true }
        now = 500L

        pressBack()

        assertEquals(2, hints)
        assertEquals(0, exits)
    }

    @Test
    fun `returning from the background clears pending exit confirmation`() {
        pressBack()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        now = 500L

        pressBack()

        assertEquals(2, hints)
        assertEquals(0, exits)
    }

    @Test
    fun `a brief pause clears pending exit confirmation`() {
        pressBack()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        now = 500L

        pressBack()

        assertEquals(2, hints)
        assertEquals(0, exits)
    }

    @Test
    fun `rotation without activity recreation clears pending exit confirmation`() {
        pressBack()
        composeRule.runOnIdle { configurationOrientation.value = Configuration.ORIENTATION_LANDSCAPE }
        now = 500L

        pressBack()

        assertEquals(2, hints)
        assertEquals(0, exits)
    }

    @Test
    fun `clearing photo selection requires two fresh back presses to exit`() {
        pressBack()
        composeRule.runOnIdle { selection.selectItem("photo") }
        now = 500L
        pressBack()

        assertFalse(selection.isActive.value)
        assertEquals(1, hints)
        assertEquals(0, exits)

        now = 1_000L
        pressBack()
        assertEquals(2, hints)
        assertEquals(0, exits)

        now = 1_500L
        pressBack()
        assertEquals(1, exits)
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }
}
