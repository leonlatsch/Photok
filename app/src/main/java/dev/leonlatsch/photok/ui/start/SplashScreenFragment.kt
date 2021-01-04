/*
 *   Copyright 2020-2021 Leon Latsch
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package dev.leonlatsch.photok.ui.start

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.R

/**
 * Fragment to display a splash screen and check application state.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class SplashScreenFragment : Fragment(R.layout.fragment_splash_screen) {

    private val viewModel: SplashScreenViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.addOnPropertyChange<ApplicationState>(BR.applicationState) {
            when (it) {
                ApplicationState.FIRST_START -> navigate(R.id.action_splashScreenFragment_to_onBoardingFragment)
                ApplicationState.SETUP -> navigate(R.id.action_splashScreenFragment_to_setupFragment)
                ApplicationState.LOCKED -> navigate(R.id.action_splashScreenFragment_to_unlockFragment)
            }
        }
        viewModel.checkApplicationState()
    }


    private fun navigate(fragment: Int) {
        findNavController().navigate(fragment)
    }
}