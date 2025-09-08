/*
 *   Copyright 2020-2022 Leon Latsch
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