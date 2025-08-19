/*
 *   Copyright 2020-2024 Leon Latsch
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

package dev.leonlatsch.photok.security.migration.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.theme.AppTheme
import kotlinx.coroutines.delay

@AndroidEntryPoint
class EncryptionMigrationFragment : Fragment() {

    private val viewModel: LegacyEncryptionMigrationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.startMigration(requireContext().applicationContext)
//        findNavController().navigate(R.id.action_encryptionMigrationFragment_to_galleryFragment)

        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    when (uiState) {
                        is LegacyEncryptionMigrationUiState.Initial -> Unit
                        is LegacyEncryptionMigrationUiState.Error -> Text("")
                        is LegacyEncryptionMigrationUiState.Migrating -> {

                            val progress = (uiState as LegacyEncryptionMigrationUiState.Migrating).progress

                            LaunchedEffect(progress) {
                                if (progress == 100) {
                                    delay(2000)
                                    findNavController().navigate(R.id.action_encryptionMigrationFragment_to_galleryFragment)
                                }
                            }

                            EncryptionMigrationScreen(uiState = uiState as LegacyEncryptionMigrationUiState.Migrating)
                        }
                    }
                }
            }
        }
    }
}