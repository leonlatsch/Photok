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

package dev.leonlatsch.photok.ui.unlock

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentUnlockBinding
import dev.leonlatsch.photok.other.hide
import dev.leonlatsch.photok.other.show
import dev.leonlatsch.photok.other.vanish
import dev.leonlatsch.photok.ui.MainActivity
import dev.leonlatsch.photok.ui.components.BindableFragment
import dev.leonlatsch.photok.ui.components.Dialogs
import dev.leonlatsch.photok.ui.unlock.options.UnlockOptionsContainerDialog

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.unlockState.observe(viewLifecycleOwner, {
            when (it) {
                UnlockState.CHECKING -> binding.loadingOverlay.show()
                UnlockState.UNLOCKED -> {
                    unlock()
                }
                UnlockState.LOCKED -> {
                    binding.loadingOverlay.hide()
                    binding.unlockWrongPasswordWarningTextView.show()
                }
                else -> return@observe
            }
        })

        viewModel.passwordText.observe(viewLifecycleOwner, {
            if (binding.unlockWrongPasswordWarningTextView.visibility != View.INVISIBLE) {
                binding.unlockWrongPasswordWarningTextView.vanish()
            }
        })

        super.onViewCreated(view, savedInstanceState)
    }

    private fun unlock() {
        if (viewModel.encryptionManager.isReady) {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
        } else {
            Dialogs.showLongToast(requireContext(), getString(R.string.common_error))
            binding.loadingOverlay.hide()
        }
    }

    /**
     * Shows [UnlockOptionsContainerDialog].
     * Called by ui.
     */
    fun showOptions() {
        val dialog = UnlockOptionsContainerDialog()
        dialog.show(
            requireActivity().supportFragmentManager,
            UnlockOptionsContainerDialog::class.qualifiedName
        )
    }

    override fun bind(binding: FragmentUnlockBinding) {
        super.bind(binding)
        binding.context = this
        binding.viewModel = viewModel
    }
}