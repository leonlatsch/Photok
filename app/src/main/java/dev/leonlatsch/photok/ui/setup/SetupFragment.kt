package dev.leonlatsch.photok.ui.setup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentSetupBinding
import dev.leonlatsch.photok.other.emptyString
import dev.leonlatsch.photok.ui.BaseFragment
import dev.leonlatsch.photok.ui.MainActivity
import kotlinx.android.synthetic.main.fragment_setup.*

@AndroidEntryPoint
class SetupFragment : BaseFragment<FragmentSetupBinding>(R.layout.fragment_setup, false) {

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
                setupConfirmPasswordEditText.setText(emptyString())
                setupConfirmPasswordEditText.visibility = View.GONE
            }

            enableOrDisableSetup()
        })

        viewModel.confirmPasswordText.observe(viewLifecycleOwner, {
            enableOrDisableSetup()
        })

        viewModel.setupState.observe(viewLifecycleOwner, {
            when(it) {
                SetupState.FINISHED -> {
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