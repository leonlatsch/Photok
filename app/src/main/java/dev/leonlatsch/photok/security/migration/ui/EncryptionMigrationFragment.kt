


package dev.leonlatsch.photok.security.migration.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.findNavController
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
        return ComposeView(requireContext()).apply {
            setContent {

                BackHandler { activity?.finish() }

                AppTheme {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    LaunchedEffect(uiState is LegacyEncryptionMigrationUiState.Success) {
                        if (uiState is LegacyEncryptionMigrationUiState.Success) {
                            delay(2000)
                            findNavController().navigate(R.id.action_encryptionMigrationFragment_to_galleryFragment)
                        }
                    }

                    when (uiState) {
                        is LegacyEncryptionMigrationUiState.Initial -> EncryptionMigrationScreenInitial(
                            uiState as LegacyEncryptionMigrationUiState.Initial,
                            viewModel::handleUiEvent
                        )

                        is LegacyEncryptionMigrationUiState.Migrating -> EncryptionMigrationScreenMigrating(
                            uiState as LegacyEncryptionMigrationUiState.Migrating
                        )

                        is LegacyEncryptionMigrationUiState.Success -> EncryptionMigrationScreenSuccess(
                            uiState as LegacyEncryptionMigrationUiState.Success
                        )

                        is LegacyEncryptionMigrationUiState.Error -> EncryptionMigrationScreenError(
                            uiState as LegacyEncryptionMigrationUiState.Error,
                            viewModel::handleUiEvent
                        )
                    }
                }
            }
        }
    }
}

package dev.leonlatsch.photok.security.migration.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.findNavController
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
        return ComposeView(requireContext()).apply {
            setContent {

                BackHandler { activity?.finish() }

                AppTheme {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    LaunchedEffect(uiState is LegacyEncryptionMigrationUiState.Success) {
                        if (uiState is LegacyEncryptionMigrationUiState.Success) {
                            delay(2000)
                            findNavController().navigate(R.id.action_encryptionMigrationFragment_to_galleryFragment)
                        }
                    }

                    when (uiState) {
                        is LegacyEncryptionMigrationUiState.Initial -> EncryptionMigrationScreenInitial(
                            uiState as LegacyEncryptionMigrationUiState.Initial,
                            viewModel::handleUiEvent
                        )

                        is LegacyEncryptionMigrationUiState.Migrating -> EncryptionMigrationScreenMigrating(
                            uiState as LegacyEncryptionMigrationUiState.Migrating
                        )

                        is LegacyEncryptionMigrationUiState.Success -> EncryptionMigrationScreenSuccess(
                            uiState as LegacyEncryptionMigrationUiState.Success
                        )

                        is LegacyEncryptionMigrationUiState.Error -> EncryptionMigrationScreenError(
                            uiState as LegacyEncryptionMigrationUiState.Error,
                            viewModel::handleUiEvent
                        )
                    }
                }
            }
        }
    }
}