package dev.leonlatsch.photok.ui.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentSetupBinding

@AndroidEntryPoint
class SetupFragment : Fragment() {

    private val viewModel: SetupViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentSetupBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_setup, container, false)
        binding.viewModel = viewModel
        binding.submitClickListener = submitClickListener
        binding.lifecycleOwner = this
        return binding.root
    }

    private val submitClickListener = View.OnClickListener {
        println("Submit")
    }
}