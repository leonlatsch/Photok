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

package dev.leonlatsch.photok.ui.settings.hideapp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.DialogHideAppBinding
import dev.leonlatsch.photok.ui.components.BindableDialogFragment
import dev.leonlatsch.photok.ui.components.Dialogs

@AndroidEntryPoint
class HideAppDialog : BindableDialogFragment<DialogHideAppBinding>(R.layout.dialog_hide_app) {

    private val viewModel: HideAppViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.updateState()
    }

    fun hideApp() {
        Dialogs.showConfirmDialog(requireContext(), "Hide???") { _, _ ->
            viewModel.disableMainComponent()
        }
    }

    fun showApp() {
        Dialogs.showConfirmDialog(requireContext(), "Show???") { _, _ ->
            viewModel.enableMainComponent()
        }
    }


    override fun bind(binding: DialogHideAppBinding) {
        super.bind(binding)
        binding.context = this
        binding.viewModel = viewModel
    }
}