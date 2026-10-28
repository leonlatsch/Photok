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

package dev.leonlatsch.photok.gallery.components

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
class MultiSelectionBackHandlerTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val selection = MultiSelectionState(listOf("first", "second"))
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
        composeRule.setContent { MultiSelectionBackHandler(selection) }
    }

    @Test
    fun `back during selection only clears selection without reaching the parent`() {
        composeRule.runOnIdle {
            selection.selectItem("first")
            selection.selectItem("second")
        }

        pressBack()

        assertFalse(selection.isActive.value)
        assertEquals(emptyList<String>(), selection.selectedItems.value)
        assertEquals(0, parentBackPresses)
    }

    @Test
    fun `back without selection reaches the parent`() {
        pressBack()

        assertEquals(1, parentBackPresses)
    }

    @Test
    fun `back after clearing selection reaches the parent`() {
        composeRule.runOnIdle { selection.selectItem("first") }

        pressBack()
        assertEquals(0, parentBackPresses)
        pressBack()

        assertEquals(1, parentBackPresses)
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }
}
