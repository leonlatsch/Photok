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

package dev.leonlatsch.photok.unlock.ui

import androidx.fragment.app.Fragment

sealed interface UnlockUiEvent {
    data class PasswordChanged(val password: String) : UnlockUiEvent
    data object UnlockWithPassword : UnlockUiEvent

    /** Needs the hosting [Fragment] to show the system biometric prompt. */
    data class UnlockWithBiometric(val fragment: Fragment) : UnlockUiEvent
    data object ForgotPassword : UnlockUiEvent
}

sealed interface UnlockNavigationEvent {
    data object Unlocked : UnlockNavigationEvent
    data object StartLegacyMigration : UnlockNavigationEvent
    data object ShowRecoveryPhraseSetup : UnlockNavigationEvent
    data object ShowRecoveryPhraseRestore : UnlockNavigationEvent
    data object ShowError : UnlockNavigationEvent
}
