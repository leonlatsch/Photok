package dev.leonlatsch.photok.ui.start

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R

@AndroidEntryPoint
class SplashScreenFragment : Fragment(R.layout.fragment_splash_screen) {

    private val viewModel: SplashScreenViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.vaultState.observe(viewLifecycleOwner, {
            when(it) {
                VaultState.FIRST_START -> navigate(R.id.action_splashScreenFragment_to_onBoardingFragment)
                VaultState.SETUP -> navigate(R.id.action_splashScreenFragment_to_setupFragment)
                VaultState.LOCKED -> navigate(R.id.action_splashScreenFragment_to_unlockFragment)
                else -> return@observe
            }
        })
        Handler().postDelayed({
            viewModel.checkVaultState()
        }, 300)
    }



    private fun navigate(fragment: Int) {
        findNavController().navigate(fragment)
    }
}