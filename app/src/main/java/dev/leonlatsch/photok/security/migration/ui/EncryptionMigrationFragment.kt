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

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat.startForegroundService
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.security.migration.MigrationService
import dev.leonlatsch.photok.security.migration.MigrationServiceCompanion
import dev.leonlatsch.photok.ui.theme.AppTheme
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class EncryptionMigrationFragment : Fragment() {

    @Inject
    lateinit var migrationServiceCompanion: MigrationServiceCompanion

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // TODO: Move to viewmodel
        val serviceIntent = Intent(requireContext(), MigrationService::class.java)
        startForegroundService(requireContext(), serviceIntent)

        return ComposeView(requireContext()).apply {
            setContent {
                val progress by migrationServiceCompanion.progress.collectAsStateWithLifecycle()

                LaunchedEffect(progress) {
                    if (progress == 100) {
                        delay(300)
                        findNavController().navigate(R.id.action_encryptionMigrationFragment_to_galleryFragment)
                    }
                }

                AppTheme {
                    EncryptionMigrationScreen(progress)
                }
            }
        }
    }
}