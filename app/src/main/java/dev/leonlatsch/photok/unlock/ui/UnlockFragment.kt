/*
 *   Copyright 2020-2021 Leon Latsch
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

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.ApplicationState
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentUnlockBinding
import dev.leonlatsch.photok.other.extensions.getBaseApplication
import dev.leonlatsch.photok.other.extensions.hide
import dev.leonlatsch.photok.other.extensions.launchLifecycleAwareJob
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.other.extensions.vanish
import dev.leonlatsch.photok.other.systemBarsPadding
import dev.leonlatsch.photok.security.biometric.BiometricUnlock
import dev.leonlatsch.photok.security.migration.LegacyEncryptionMigrator
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.uicomponnets.Dialogs
import dev.leonlatsch.photok.uicomponnets.base.BaseActivity
import dev.leonlatsch.photok.uicomponnets.bindings.BindableFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Unlock fragment.
 * Handles state and login.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class UnlockFragment : BindableFragment<FragmentUnlockBinding>(R.layout.fragment_unlock) {

    private val viewModel: UnlockViewModel by viewModels()

    @Inject
    lateinit var legacyEncryptionMigrator: LegacyEncryptionMigrator

    @Inject
    lateinit var config: Config

    @Inject
    lateinit var biometricUnlock: BiometricUnlock

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.systemBarsPadding()

        if (BuildConfig.DEBUG) {
            viewModel.password = "abc123"
        }

        launchLifecycleAwareJob {
            viewModel.unlockState.collectLatest {
                when (it) {
                    UnlockState.CHECKING -> binding.loadingOverlay.show()
                    UnlockState.UNLOCKED -> goToGallery()
                    UnlockState.LOCKED -> {
                        binding.loadingOverlay.hide()
                        binding.unlockWrongPasswordWarningTextView.show()
                    }

                    UnlockState.UNDEFINED -> Unit
                }
            }
        }

        viewModel.addOnPropertyChange<String>(BR.password) {
            if (binding.unlockWrongPasswordWarningTextView.visibility != View.INVISIBLE) {
                binding.unlockWrongPasswordWarningTextView.vanish()
            }
        }

        super.onViewCreated(view, savedInstanceState)

        // Check for migration should not be needed. But double check because in this case we don't have the legacy key
        if (biometricUnlock.isAvailableAndSetup && !legacyEncryptionMigrator.migrationNeeded()) {
            binding.unlockUseBiometricUnlockButton.show()
            launchBiometricUnlock()
        } else {
            binding.unlockUseBiometricUnlockButton.hide()
        }
    }

    fun launchBiometricUnlock(delay: Long = 500L) {
        lifecycleScope.launch {
            delay(delay)

            biometricUnlock.unlock(this@UnlockFragment)
                .onSuccess { goToGallery() }
                .onFailure {
                    Dialogs.showLongToast(
                        context = requireContext(),
                        message = getString(R.string.biometric_unlock_error),
                    )
                }
        }
    }


    private fun goToGallery() {
        val activity = activity

        (activity as? BaseActivity)?.hideKeyboard()
        binding.loadingOverlay.hide()

        if (activity == null || !viewModel.encryptionManager.isReady) {
            Dialogs.showLongToast(requireContext(), getString(R.string.common_error))
            return
        }

        activity.getBaseApplication().state.update { ApplicationState.UNLOCKED }

        if (config.legacyCurrentlyMigrating || legacyEncryptionMigrator.migrationNeeded()) {
            lifecycleScope.launch {
                findNavController().navigate(R.id.action_unlockFragment_to_encryptionMigrationFragment)
            }
        } else {
            findNavController().navigate(R.id.action_unlockFragment_to_galleryFragment)
        }
    }

    override fun bind(binding: FragmentUnlockBinding) {
        super.bind(binding)
        binding.context = this
        binding.viewModel = viewModel
    }
}