package dev.leonlatsch.photok.ui.unlock

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentUnlockBinding
import dev.leonlatsch.photok.other.hideLoadingOverlay
import dev.leonlatsch.photok.other.showLoadingOverlay
import dev.leonlatsch.photok.ui.MainActivity
import dev.leonlatsch.photok.ui.components.BindableFragment
import kotlinx.android.synthetic.main.fragment_unlock.*
import kotlinx.android.synthetic.main.loading_overlay.*

@AndroidEntryPoint
class UnlockFragment : BindableFragment<FragmentUnlockBinding>(R.layout.fragment_unlock, false) {

    private val viewModel: UnlockViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.unlockState.observe(viewLifecycleOwner, {
            when (it) {
                UnlockState.CHECKING -> showLoadingOverlay(loadingOverlay)
                UnlockState.UNLOCKED -> {
                    unlock()
                }
                UnlockState.LOCKED -> {
                    hideLoadingOverlay(loadingOverlay)
                    unlockWrongPasswordWarningTextView.visibility = View.VISIBLE
                }
                else -> return@observe
            }
        })

        viewModel.passwordText.observe(viewLifecycleOwner, {
            if (unlockWrongPasswordWarningTextView.visibility != View.INVISIBLE) {
                unlockWrongPasswordWarningTextView.visibility = View.INVISIBLE
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
            Toast.makeText(requireContext(), getString(R.string.common_error), Toast.LENGTH_LONG)
                .show()
            hideLoadingOverlay(loadingOverlay)
        }
    }

    override fun bind(binding: FragmentUnlockBinding) {
        super.bind(binding)
        binding.viewModel = viewModel
    }
}