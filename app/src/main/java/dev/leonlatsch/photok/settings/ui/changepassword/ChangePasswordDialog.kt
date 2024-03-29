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

package dev.leonlatsch.photok.settings.ui.changepassword

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.DialogChangePasswordBinding
import dev.leonlatsch.photok.other.extensions.empty
import dev.leonlatsch.photok.other.extensions.hide
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.security.PasswordUtils
import dev.leonlatsch.photok.uicomponnets.Dialogs
import dev.leonlatsch.photok.uicomponnets.bindings.BindableDialogFragment

/**
 * Dialog for changing the password. Validates that the old password os known and collects the new password.
 * Starts re-encryption process.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class ChangePasswordDialog :
    BindableDialogFragment<DialogChangePasswordBinding>(R.layout.dialog_change_password) {

    private val viewModel: ChangePasswordViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.addOnPropertyChange<ChangePasswordState>(BR.changePasswordState) {
            when (it) {
                ChangePasswordState.START -> {
                    binding.changePasswordNewPasswordLayout.hide()
                }
                ChangePasswordState.CHECKING_OLD -> {
                    binding.loadingOverlay.show()
                    binding.changePasswordOldPasswordWrongLabel.hide()
                }
                ChangePasswordState.OLD_VALID -> handleOldValid()
                ChangePasswordState.OLD_INVALID -> {
                    binding.loadingOverlay.hide()
                    binding.changePasswordOldPasswordWrongLabel.show()
                }
                ChangePasswordState.NEW_INVALID -> {
                    binding.loadingOverlay.hide()
                }
                ChangePasswordState.NEW_VALID -> {
                    binding.loadingOverlay.hide()
                    viewModel.checkIfReEncryptNeeded()
                }
                ChangePasswordState.RE_ENCRYPT_NEEDED -> handleReEncryptNeeded()
                ChangePasswordState.RE_ENCRYPT_NOT_NEEDED -> dismiss()
            }
        }

        viewModel.addOnPropertyChange<String>(BR.newPassword) {
            if (PasswordUtils.validatePassword(viewModel.newPassword)) {
                binding.changePasswordNewPasswordConfirmEditText.show()
            } else {
                binding.changePasswordNewPasswordConfirmEditText.setTextValue(String.empty)
                binding.changePasswordNewPasswordConfirmEditText.hide()
            }
            enableOrDisableSetup()
        }
        viewModel.addOnPropertyChange<String>(BR.newPasswordConfirm) {
            enableOrDisableSetup()
        }
    }

    private fun handleOldValid() {
        binding.loadingOverlay.hide()
        binding.changePasswordOldPasswordEditText.hide()
        binding.changePasswordCheckOldButton.hide()
        binding.changePasswordOldStatusIcon.show()
        binding.changePasswordNewPasswordLayout.show()
        binding.changePasswordNewPasswordLayout.requestFocus()
    }

    private fun handleReEncryptNeeded() {
        Dialogs.showConfirmDialog(
            requireContext(),
            getString(R.string.change_password_confirm_message)
        ) { _, _ ->
            ReEncryptBottomSheetDialogFragment(
                viewModel.newPassword
            ).show(requireActivity().supportFragmentManager)
            dismiss()
        }
    }

    private fun enableOrDisableSetup() {
        if (!PasswordUtils.passwordsNotEmptyAndEqual(
                viewModel.newPassword,
                viewModel.newPasswordConfirm
            )
            && binding.changePasswordNewPasswordConfirmEditText.isVisible
        ) {
            binding.changePasswordNewPasswordNotEqualLabel.show()
            binding.changePasswordButton.isEnabled = false
        } else {
            binding.changePasswordNewPasswordNotEqualLabel.hide()
            if (PasswordUtils.validatePasswords(
                    viewModel.newPassword,
                    viewModel.newPasswordConfirm
                )
            ) {
                binding.changePasswordButton.isEnabled = true
            }
        }
    }

    override fun bind(binding: DialogChangePasswordBinding) {
        super.bind(binding)
        binding.context = this
        binding.viewModel = viewModel
    }
}