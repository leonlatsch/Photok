/*
 *   Copyright 2020-2022 Leon Latsch
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
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.ApplicationState
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentSetupBinding
import dev.leonlatsch.photok.other.extensions.empty
import dev.leonlatsch.photok.other.extensions.getBaseApplication
import dev.leonlatsch.photok.other.extensions.hide
import dev.leonlatsch.photok.other.extensions.requireActivityAs
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.other.statusBarPadding
import dev.leonlatsch.photok.other.systemBarsPadding
import dev.leonlatsch.photok.uicomponnets.Dialogs
import dev.leonlatsch.photok.uicomponnets.base.BaseActivity
import dev.leonlatsch.photok.uicomponnets.bindings.BindableFragment

/**
 * Fragment for the setup.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class SetupFragment : BindableFragment<FragmentSetupBinding>(R.layout.fragment_setup) {

    private val viewModel: SetupViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.systemBarsPadding()

        viewModel.addOnPropertyChange<String>(BR.password) {
            if (it.isNotEmpty()) {
                val value = when (it.length) {
                    1, 2, 3, 4, 5 -> {
                        binding.setupPasswordStrengthValue.setTextColor(requireContext().getColor(R.color.darkRed))
                        getString(R.string.setup_password_strength_weak)
                    }
                    6, 7, 8, 9, 10 -> {
                        binding.setupPasswordStrengthValue.setTextColor(requireContext().getColor(R.color.darkYellow))
                        getString(R.string.setup_password_strength_moderate)
                    }
                    else -> {
                        binding.setupPasswordStrengthValue.setTextColor(requireContext().getColor(R.color.darkGreen))
                        getString(R.string.setup_password_strength_strong)
                    }
                }
                binding.setupPasswordStrengthLayout.show()
                binding.setupPasswordStrengthValue.text = value
            } else {
                binding.setupPasswordStrengthLayout.hide()
            }

            if (viewModel.validatePassword()) {
                binding.setupConfirmPasswordEditText.show()
            } else {
                binding.setupConfirmPasswordEditText.setTextValue(String.empty)
                binding.setupConfirmPasswordEditText.hide()
            }

            enableOrDisableSetup()
        }

        viewModel.addOnPropertyChange<String>(BR.confirmPassword) {
            enableOrDisableSetup()
        }

        viewModel.addOnPropertyChange<SetupState>(BR.setupState) {
            when (it) {
                SetupState.LOADING -> binding.loadingOverlay.show()
                SetupState.SETUP -> binding.loadingOverlay.hide()
                SetupState.FINISHED -> finishSetup()
            }
        }
    }

    private fun finishSetup() {
        requireActivityAs(BaseActivity::class).hideKeyboard()
        binding.loadingOverlay.hide()

        if (viewModel.encryptionManager.isReady) {
            requireActivity().getBaseApplication().applicationState = ApplicationState.UNLOCKED
            findNavController().navigate(R.id.action_setupFragment_to_galleryFragment)
        } else {
            Dialogs.showLongToast(
                requireContext(),
                getString(R.string.common_error)
            )
        }
    }

    private fun enableOrDisableSetup() {
        if (!viewModel.passwordsEqual()
            && binding.setupConfirmPasswordEditText.isVisible
        ) {
            binding.setupPasswordMatchWarningTextView.show()
            binding.setupButton.isEnabled = false
        } else {
            binding.setupPasswordMatchWarningTextView.hide()
            if (viewModel.validateBothPasswords()) {
                binding.setupButton.isEnabled = true
            }
        }
    }

    override fun bind(binding: FragmentSetupBinding) {
        super.bind(binding)
        binding.viewModel = viewModel
    }
}