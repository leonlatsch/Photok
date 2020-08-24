package dev.leonlatsch.photok.ui.locked

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentLockedBinding
import dev.leonlatsch.photok.ui.BaseFragment

@AndroidEntryPoint
class LockedFragment : BaseFragment<FragmentLockedBinding>(R.layout.fragment_locked, false) {

    private val viewModel: LockedViewModel by viewModels()

    override fun bind(binding: FragmentLockedBinding) {
        super.bind(binding)
        binding.viewModel = viewModel
    }
}