/*
 *   Copyright 2020-2024 Leon Latsch
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

package dev.leonlatsch.photok.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.LocalFragment

/**
 * Fragment to display a info about the app and some links.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class AboutFragment : Fragment() {

    private val viewModel: AboutViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                val vaultStats by viewModel.vaultStats.collectAsStateWithLifecycle()

                CompositionLocalProvider(
                    LocalFragment provides this@AboutFragment
                ) {
                    AboutScreen(
                        vaultStats = vaultStats,
                        handleUiEvent = {
                            when (it) {
                                AboutUiEvent.Close -> findNavController().navigateUp()
                                AboutUiEvent.OpenThirdParty -> findNavController().navigate(R.id.action_aboutFragment_to_ossLicensesFragment)
                            }
                        }
                    )
                }
            }
        }
    }
}
