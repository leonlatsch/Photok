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
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.ApplicationState
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentUnlockBinding
import dev.leonlatsch.photok.other.extensions.getBaseApplication
import dev.leonlatsch.photok.other.extensions.hide
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.other.extensions.vanish
import dev.leonlatsch.photok.other.systemBarsPadding
import dev.leonlatsch.photok.security.LegacyEncryptionMigrator
import dev.leonlatsch.photok.uicomponnets.Dialogs
import dev.leonlatsch.photok.uicomponnets.base.BaseActivity
import dev.leonlatsch.photok.uicomponnets.bindings.BindableFragment
import kotlinx.coroutines.flow.update
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.systemBarsPadding()

        if (BuildConfig.DEBUG) {
            viewModel.password = "abc123"
        }

        viewModel.addOnPropertyChange<UnlockState>(BR.unlockState) {
            when (it) {
                UnlockState.CHECKING -> binding.loadingOverlay.show()
                UnlockState.UNLOCKED -> unlock()
                UnlockState.LOCKED -> {
                    binding.loadingOverlay.hide()
                    binding.unlockWrongPasswordWarningTextView.show()
                }
                else -> return@addOnPropertyChange
            }
        }

        viewModel.addOnPropertyChange<String>(BR.password) {
            if (binding.unlockWrongPasswordWarningTextView.visibility != View.INVISIBLE) {
                binding.unlockWrongPasswordWarningTextView.vanish()
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    private fun unlock() {
        val activity = activity

        (activity as? BaseActivity)?.hideKeyboard()
        binding.loadingOverlay.hide()

        if (activity == null || !viewModel.encryptionManager.isReady) {
            Dialogs.showLongToast(requireContext(), getString(R.string.common_error))
            return
        }

        activity.getBaseApplication().state.update { ApplicationState.UNLOCKED }

        if (legacyEncryptionMigrator.migrationNeeded()) {
            findNavController().navigate(R.id.action_unlockFragment_to_encryptionMigrationFragment)
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