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

package dev.leonlatsch.photok.setup.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.encryption.ui.RecoveryPhraseSheet
import dev.leonlatsch.photok.ui.theme.AppTheme

/**
 * Full-screen step shown after setup that displays the generated recovery phrase.
 * Navigates to the gallery when the user confirms they have saved it.
 */
@AndroidEntryPoint
class RecoveryPhraseSetupFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    RecoveryPhraseSheet(
                        onDismiss = {
                            findNavController().navigate(
                                R.id.action_recoveryPhraseSetupFragment_to_galleryFragment
                            )
                        },
                    )
                }
            }
        }
    }
}
