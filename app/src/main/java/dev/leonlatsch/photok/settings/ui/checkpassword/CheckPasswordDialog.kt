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

package dev.leonlatsch.photok.settings.ui.checkpassword

import android.os.Bundle
import android.view.View
import android.content.DialogInterface
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.DialogCheckPasswordBinding
import dev.leonlatsch.photok.other.extensions.hide
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.uicomponnets.bindings.BindableDialogFragment

@AndroidEntryPoint
class CheckPasswordDialog(
    private val onPasswordValid: (() -> Unit)? = null
) : BindableDialogFragment<DialogCheckPasswordBinding>(R.layout.dialog_check_password) {

    private val viewModel: CheckPasswordViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.addOnPropertyChange<CheckPasswordState>(BR.checkPasswordState) {
            when (it) {
                CheckPasswordState.START -> Unit
                CheckPasswordState.CHECKING_OLD -> {
                    binding.loadingOverlay.show()
                    binding.checkPasswordOldPasswordWrongLabel.hide()
                }
                CheckPasswordState.OLD_VALID -> handleOldValid()
                CheckPasswordState.OLD_INVALID -> {
                    binding.loadingOverlay.hide()
                    binding.checkPasswordOldPasswordWrongLabel.show()
                }
            }
        }
    }

    private fun handleOldValid() {
        binding.loadingOverlay.hide()
        binding.checkPasswordOldPasswordEditText.hide()
        binding.checkPasswordCheckOldButton.hide()
        dismiss()
    }

    override fun bind(binding: DialogCheckPasswordBinding) {
        super.bind(binding)
        binding.context = this
        binding.viewModel = viewModel
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (viewModel.checkPasswordState == CheckPasswordState.OLD_VALID) {
            onPasswordValid?.invoke()
        }
    }
}

