/*
 *   Copyright 2020–2026 Leon Latsch
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

package dev.leonlatsch.photok.unlock.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.ui.navigation.NavigateToGallery
import dev.leonlatsch.photok.other.extensions.finishOnBackWhileStarted
import dev.leonlatsch.photok.other.extensions.launchLifecycleAwareJob
import dev.leonlatsch.photok.ui.LocalFragment
import dev.leonlatsch.photok.uicomponnets.Dialogs
import dev.leonlatsch.photok.uicomponnets.base.hideKeyboard
import timber.log.Timber
import javax.inject.Inject

/**
 * Unlock fragment.
 * Hosts [UnlockScreen] and performs the navigation it asks for.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class UnlockFragment : Fragment() {

    private val viewModel: UnlockViewModel by viewModels()

    @Inject
    lateinit var navigateToGallery: NavigateToGallery

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setContent {
            CompositionLocalProvider(
                LocalFragment provides this@UnlockFragment,
            ) {
                UnlockScreen(viewModel)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        finishOnBackWhileStarted()

        launchLifecycleAwareJob {
            viewModel.navigationEvents.collect { event ->
                navigate(event)
            }
        }
    }

    private fun navigate(event: UnlockNavigationEvent) {
        if (event == UnlockNavigationEvent.ShowError) {
            showErrorToast()
            return
        }

        activity?.hideKeyboard()

        try {
            when (event) {
                UnlockNavigationEvent.Unlocked -> navigateToGallery(findNavController())

                UnlockNavigationEvent.StartLegacyMigration ->
                    findNavController().navigate(R.id.action_unlockFragment_to_encryptionMigrationFragment)

                UnlockNavigationEvent.ShowRecoveryPhraseSetup ->
                    findNavController().navigate(R.id.action_global_recoveryPhraseSetupFragment)

                UnlockNavigationEvent.ShowRecoveryPhraseRestore ->
                    findNavController().navigate(R.id.action_unlockFragment_to_recoveryPhraseRestoreFragment)

                UnlockNavigationEvent.ShowError -> Unit
            }
        } catch (e: Exception) {
            Timber.e(e)
            showErrorToast()
        }
    }

    private fun showErrorToast() {
        Dialogs.showLongToast(requireContext(), getString(R.string.common_error))
    }
}
