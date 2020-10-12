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
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.DialogChangePasswordBinding
import dev.leonlatsch.photok.other.hide
import dev.leonlatsch.photok.other.show
import dev.leonlatsch.photok.ui.components.BindableDialogFragment
import kotlinx.android.synthetic.main.dialog_change_password.*
import kotlinx.android.synthetic.main.loading_overlay.*

@AndroidEntryPoint
class ChangePasswordDialog : BindableDialogFragment<DialogChangePasswordBinding>(R.layout.dialog_change_password) {

    private val viewModel: ChangePasswordViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.changePasswordState.observe(viewLifecycleOwner, {
            when(it) {
                ChangePasswordState.START -> {
                    changePasswordNewPasswordLayout.hide()
                }
                ChangePasswordState.CHECKING_OLD -> loadingOverlay.show()
                ChangePasswordState.OLD_VALID -> {
                    loadingOverlay.hide()
                    changePasswordCheckOldButton.hide()
                    changePasswordOldStatusIcon.show() // Show different icon on fail
                    changePasswordNewPasswordLayout.show()
                    changePasswordNewPasswordLayout.requestFocus()
                    // TODO: Disable old edit text
                }
                ChangePasswordState.OLD_INVALID -> loadingOverlay.hide()
                ChangePasswordState.CHECKING_NEW -> return@observe
                ChangePasswordState.NEW_VALID -> return@observe
                ChangePasswordState.NEW_INVALID -> return@observe
                else -> return@observe
            }
        })
    }

    override fun bind(binding: DialogChangePasswordBinding) {
        super.bind(binding)
        binding.context = this
        binding.viewModel = viewModel
    }
}