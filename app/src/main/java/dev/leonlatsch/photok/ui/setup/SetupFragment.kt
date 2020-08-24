package dev.leonlatsch.photok.ui.setup

import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentSetupBinding
import dev.leonlatsch.photok.other.PASSWORD_REGEX
import dev.leonlatsch.photok.ui.BaseFragment
import kotlinx.android.synthetic.main.fragment_setup.*
import java.util.regex.Pattern

@AndroidEntryPoint
class SetupFragment : BaseFragment<FragmentSetupBinding>(R.layout.fragment_setup, false) {

    private val viewModel: SetupViewModel by viewModels()

    private val submitClickListener = View.OnClickListener {
        if (setupPasswordEditText.text.isNotEmpty()
            && Pattern.matches(PASSWORD_REGEX, setupPasswordEditText.text.toString())) {
            viewModel.savePassword(setupPasswordEditText.text.toString())
            Toast.makeText(requireContext(), "Saving password", Toast.LENGTH_SHORT).show() // TEST
        } else {
            Toast.makeText(requireContext(), "Password does not match", Toast.LENGTH_SHORT).show() // TEST
        }
    }

    override fun insertBindings(binding: FragmentSetupBinding) {
        super.insertBindings(binding)
        binding.viewModel = viewModel
        binding.submitClickListener = submitClickListener
    }
}