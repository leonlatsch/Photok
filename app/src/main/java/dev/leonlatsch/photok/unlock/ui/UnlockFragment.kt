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

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentUnlockBinding
import dev.leonlatsch.photok.encryption.domain.VaultService
import dev.leonlatsch.photok.encryption.domain.models.VaultProtectionType
import dev.leonlatsch.photok.other.extensions.finishOnBackWhileStarted
import dev.leonlatsch.photok.other.extensions.hide
import dev.leonlatsch.photok.other.extensions.launchLifecycleAwareJob
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.other.extensions.vanish
import dev.leonlatsch.photok.other.systemBarsPadding
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.domain.models.StartPage
import dev.leonlatsch.photok.uicomponnets.Dialogs
import dev.leonlatsch.photok.uicomponnets.base.hideKeyboard
import dev.leonlatsch.photok.uicomponnets.bindings.BindableFragment
import kotlinx.coroutines.delay
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
    lateinit var config: Config

    @Inject
    lateinit var vaultService: VaultService

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.systemBarsPadding()
        finishOnBackWhileStarted()

        if (BuildConfig.DEBUG) {
            viewModel.password = "abc123"
        }

        launchLifecycleAwareJob {
            viewModel.unlockState.collect {
                when (it) {
                    UnlockState.Initial -> Unit
                    UnlockState.PasswordError -> {
                        binding.loadingOverlay.hide()
                        binding.unlockWrongPasswordWarningTextView.show()
                    }

                    UnlockState.Loading -> binding.loadingOverlay.show()
                    UnlockState.Unlocked -> {
                        binding.loadingOverlay.hide()
                        activity?.hideKeyboard()

                        val startPageDest = when (config.galleryStartPage) {
                            StartPage.AllFiles -> R.id.action_unlockFragment_to_galleryFragment
                            StartPage.Albums -> R.id.action_unlockFragment_to_albumsFragment
                        }

                        findNavController().navigate(startPageDest)
                    }

                    UnlockState.StartLegacyMigration -> {
                        binding.loadingOverlay.hide()
                        activity?.hideKeyboard()

                        findNavController().navigate(R.id.action_unlockFragment_to_encryptionMigrationFragment)
                    }

                    UnlockState.Error -> showErrorToast()
                }
            }
        }

        viewModel.addOnPropertyChange<String>(BR.password) {
            if (binding.unlockWrongPasswordWarningTextView.visibility != View.INVISIBLE) {
                binding.unlockWrongPasswordWarningTextView.vanish()
            }
        }

        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            if (vaultService.isSetup(VaultProtectionType.Biometric) || vaultService.canMigrate(VaultProtectionType.Biometric)) {
                binding.unlockUseBiometricUnlockButton.show()

                delay(500L)
                viewModel.unlockWithBiometric(fragment = this@UnlockFragment)
            } else {
                binding.unlockUseBiometricUnlockButton.hide()
            }
        }
    }

    private fun showErrorToast() {
        binding.loadingOverlay.hide()
        Dialogs.showLongToast(requireContext(), getString(R.string.common_error))
    }

    override fun bind(binding: FragmentUnlockBinding) {
        super.bind(binding)
        binding.context = this
        binding.viewModel = viewModel
    }
}