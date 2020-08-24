package dev.leonlatsch.photok.ui.setup

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentSetupBinding
import dev.leonlatsch.photok.ui.BaseFragment

@AndroidEntryPoint
class SetupFragment : BaseFragment<FragmentSetupBinding>(R.layout.fragment_setup, false) {

    private val viewModel: SetupViewModel by viewModels()

    override fun bind(binding: FragmentSetupBinding) {
        super.bind(binding)
        binding.viewModel = viewModel
    }
}