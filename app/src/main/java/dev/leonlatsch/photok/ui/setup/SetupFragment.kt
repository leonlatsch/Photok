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

package dev.leonlatsch.photok.ui.setup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentSetupBinding
import dev.leonlatsch.photok.other.emptyString
import dev.leonlatsch.photok.other.hideLoadingOverlay
import dev.leonlatsch.photok.other.showLoadingOverlay
import dev.leonlatsch.photok.ui.MainActivity
import dev.leonlatsch.photok.ui.components.BindableFragment
import kotlinx.android.synthetic.main.fragment_setup.*
import kotlinx.android.synthetic.main.loading_overlay.*

@AndroidEntryPoint
class SetupFragment : BindableFragment<FragmentSetupBinding>(R.layout.fragment_setup, false) {

    private val viewModel: SetupViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.passwordText.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                val value = when (it.length) {
                    1, 2, 3, 4, 5 -> {
                        setupPasswordStrengthValue.setTextColor(requireContext().getColor(R.color.darkRed))
                        getString(R.string.setup_password_strength_weak)
                    }
                    6, 7, 8, 9, 10 -> {
                        setupPasswordStrengthValue.setTextColor(requireContext().getColor(R.color.darkYellow))
                        getString(R.string.setup_password_strength_moderate)
                    }
                    else -> {
                        setupPasswordStrengthValue.setTextColor(requireContext().getColor(R.color.darkGreen))
                        getString(R.string.setup_password_strength_strong)
                    }
                }
                setupPasswordStrengthLayout.visibility = View.VISIBLE
                setupPasswordStrengthValue.text = value
            } else {
                setupPasswordStrengthLayout.visibility = View.GONE
            }

            if (viewModel.validatePassword()) {
                setupConfirmPasswordEditText.visibility = View.VISIBLE
            } else {
                setupConfirmPasswordEditText.setTextValue(emptyString())
                setupConfirmPasswordEditText.visibility = View.GONE
            }

            enableOrDisableSetup()
        })

        viewModel.confirmPasswordText.observe(viewLifecycleOwner, {
            enableOrDisableSetup()
        })

        viewModel.setupState.observe(viewLifecycleOwner, {
            when(it) {
                SetupState.LOADING -> showLoadingOverlay(loadingOverlay)
                SetupState.SETUP -> hideLoadingOverlay(loadingOverlay)
                SetupState.FINISHED -> {
                    hideLoadingOverlay(loadingOverlay)

                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                }
                else -> return@observe
            }
        })

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun enableOrDisableSetup() {
        if (!viewModel.passwordsEqual()
            && setupConfirmPasswordEditText.visibility == View.VISIBLE) {
            setupPasswordMatchWarningTextView.visibility = View.VISIBLE
            setupButton.isEnabled = false
        } else {
            setupPasswordMatchWarningTextView.visibility = View.GONE
            if (viewModel.validateBothPasswords()) {
                setupButton.isEnabled = true
            }
        }
    }

    override fun bind(binding: FragmentSetupBinding) {
        super.bind(binding)
        binding.viewModel = viewModel
    }
}