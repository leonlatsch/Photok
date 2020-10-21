/*
 *   Copyright 2020 Leon Latsch
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

package dev.leonlatsch.photok.ui.settings

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.DialogChangePasswordBinding
import dev.leonlatsch.photok.other.emptyString
import dev.leonlatsch.photok.other.hide
import dev.leonlatsch.photok.other.show
import dev.leonlatsch.photok.security.PasswordUtils
import dev.leonlatsch.photok.ui.components.BindableDialogFragment
import dev.leonlatsch.photok.ui.components.Dialogs
import dev.leonlatsch.photok.ui.process.ReEncryptBottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_change_password.*
import kotlinx.android.synthetic.main.loading_overlay.*

/**
 * Dialog for chaging the password. Validates that the old password os known and collects the new password.
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

        viewModel.changePasswordState.observe(viewLifecycleOwner, {
            when (it) {
                ChangePasswordState.START -> {
                    changePasswordNewPasswordLayout.hide()
                }
                ChangePasswordState.CHECKING_OLD -> {
                    loadingOverlay.show()
                    changePasswordOldPasswordWrongLabel.hide()
                }
                ChangePasswordState.OLD_VALID -> {
                    loadingOverlay.hide()
                    changePasswordOldPasswordEditText.hide()
                    changePasswordCheckOldButton.hide()
                    changePasswordOldStatusIcon.show()
                    changePasswordNewPasswordLayout.show()
                    changePasswordNewPasswordLayout.requestFocus()
                }
                ChangePasswordState.OLD_INVALID -> {
                    loadingOverlay.hide()
                    changePasswordOldPasswordWrongLabel.show()
                }
                ChangePasswordState.NEW_VALID -> {
                    loadingOverlay.hide()
                    Dialogs.showConfirmDialog(
                        requireContext(),
                        getString(R.string.change_password_confirm_message)
                    ) { _, _ ->
                        val reEncryptDialog = ReEncryptBottomSheetDialogFragment(
                            viewModel.newPasswordTextValue.value!!
                        )
                        reEncryptDialog.show(
                            requireActivity().supportFragmentManager,
                            ReEncryptBottomSheetDialogFragment::class.qualifiedName
                        )
                        dismiss()
                    }
                }
                ChangePasswordState.NEW_INVALID -> {
                    loadingOverlay.hide()
                }
                else -> return@observe
            }
        })

        viewModel.newPasswordTextValue.observe(viewLifecycleOwner, {
            if (PasswordUtils.validatePassword(viewModel.newPasswordTextValue)) {
                changePasswordNewPasswordConfirmEditText.show()
            } else {
                changePasswordNewPasswordConfirmEditText.setTextValue(emptyString())
                changePasswordNewPasswordConfirmEditText.hide()
            }
            enableOrDisableSetup()
        })
        viewModel.newPasswordConfirmTextValue.observe(viewLifecycleOwner, {
            enableOrDisableSetup()
        })
    }

    private fun enableOrDisableSetup() {
        if (!PasswordUtils.passwordsNotEmptyAndEqual(
                viewModel.newPasswordTextValue,
                viewModel.newPasswordConfirmTextValue
            )
            && changePasswordNewPasswordConfirmEditText.isVisible
        ) {
            changePasswordNewPasswordNotEqualLabel.show()
            changePasswordButton.isEnabled = false
        } else {
            changePasswordNewPasswordNotEqualLabel.hide()
            if (PasswordUtils.validatePasswords(
                    viewModel.newPasswordTextValue,
                    viewModel.newPasswordConfirmTextValue
                )
            ) {
                changePasswordButton.isEnabled = true
            }
        }
    }

    override fun bind(binding: DialogChangePasswordBinding) {
        super.bind(binding)
        binding.context = this
        binding.viewModel = viewModel
    }
}