package dev.leonlatsch.photok.appstart.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R

/**
 * Fragment to display a splash screen and check application state.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class InitialFragment : Fragment() {

    private val viewModel: InitialViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.checkApplicationState {
            when (it) {
                AppStartState.FIRST_START -> navigate(R.id.action_initialFragment_to_onBoardingFragment)
                AppStartState.SETUP -> navigate(R.id.action_initialFragment_to_setupFragment)
                AppStartState.LOCKED -> navigate(R.id.action_initialFragment_to_unlockFragment)
            }
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun navigate(fragment: Int) {
        findNavController().navigate(fragment)
    }
}