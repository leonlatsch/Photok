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

package dev.leonlatsch.photok.backup.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.backup.domain.UnlockBackupUseCase
import dev.leonlatsch.photok.databinding.DialogBackupUnlockBinding
import dev.leonlatsch.photok.other.extensions.hide
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.uicomponnets.bindings.BindableDialogFragment
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Dialog for unlocking a backup.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class UnlockBackupDialogFragment : BindableDialogFragment<DialogBackupUnlockBinding>(R.layout.dialog_backup_unlock) {

    private val viewModel: UnlockBackupViewModel by viewModels()
    private val restoreBackupViewModel: RestoreBackupViewModel by activityViewModels()

    @Inject
    lateinit var unlockBackupUseCase: UnlockBackupUseCase

    @Suppress("DEPRECATION")
    private val uri: Uri by lazy { requireArguments().getParcelable(ARG_URI)!! }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.addOnPropertyChange<String>(BR.password) {
            binding.unlockBackupWrongPasswordWarning.hide()
        }
    }

    fun onUnlock() {
        binding.unlockBackupWrongPasswordWarning.hide()

        val metaData = restoreBackupViewModel.metaData ?: return

        lifecycleScope.launch {
            unlockBackupUseCase(uri, metaData, viewModel.password)
                .onSuccess { session ->
                    restoreBackupViewModel.restoreBackup(session)
                    dismiss()
                }
                .onFailure {
                    binding.unlockBackupWrongPasswordWarning.show()
                }
        }
    }

    override fun bind(binding: DialogBackupUnlockBinding) {
        super.bind(binding)
        binding.viewModel = viewModel
        binding.context = this
    }

    companion object {
        private const val ARG_URI = "uri"

        fun newInstance(uri: Uri): UnlockBackupDialogFragment =
            UnlockBackupDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_URI, uri)
                }
            }
    }
}