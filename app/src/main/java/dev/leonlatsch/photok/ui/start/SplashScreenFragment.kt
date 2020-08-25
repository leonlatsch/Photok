package dev.leonlatsch.photok.ui.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R

@AndroidEntryPoint
class SplashScreenFragment : Fragment(R.layout.fragment_splash_screen) {

    private val viewModel: SplashScreenViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.vaultState.observe(viewLifecycleOwner, {
            when(it) {
                VaultState.FIRST_START -> findNavController().navigate(R.id.action_splashScreenFragment_to_onBoardingFragment)
                VaultState.SETUP -> findNavController().navigate(R.id.action_splashScreenFragment_to_setupFragment)
                VaultState.LOCKED -> findNavController().navigate(R.id.action_splashScreenFragment_to_unlockFragment)
                else -> return@observe
            }
        })
        viewModel.checkVaultState()

        return super.onCreateView(inflater, container, savedInstanceState)
    }
}