package dev.leonlatsch.photok.ui.setup

import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentSetupBinding
import dev.leonlatsch.photok.ui.BaseFragment

@AndroidEntryPoint
class SetupFragment : BaseFragment<FragmentSetupBinding>(R.layout.fragment_setup, false) {

    private val viewModel: SetupViewModel by viewModels()

    private val submitClickListener = View.OnClickListener {
        println("Submit")
    }

    override fun insertBindings(binding: FragmentSetupBinding) {
        super.insertBindings(binding)
        binding.viewModel = viewModel
        binding.submitClickListener = submitClickListener
    }
}