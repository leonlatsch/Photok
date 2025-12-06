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

package dev.leonlatsch.photok.gallery.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.gallery.ui.components.AlbumPickerViewModel
import dev.leonlatsch.photok.gallery.ui.compose.GalleryScreen
import dev.leonlatsch.photok.gallery.ui.navigation.GalleryNavigator
import dev.leonlatsch.photok.gallery.ui.navigation.PhotoActionsNavigator
import dev.leonlatsch.photok.imageloading.compose.LocalEncryptedImageLoader
import dev.leonlatsch.photok.imageloading.di.EncryptedImageLoader
import dev.leonlatsch.photok.news.newfeatures.ui.ShowNewsDialogUseCase
import dev.leonlatsch.photok.other.extensions.finishOnBackWhileStarted
import dev.leonlatsch.photok.other.extensions.launchLifecycleAwareJob
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.data.StartPage
import dev.leonlatsch.photok.settings.ui.compose.LocalConfig
import javax.inject.Inject

@AndroidEntryPoint
class GalleryFragment : Fragment() {

    private val viewModel: GalleryViewModel by viewModels()
    private val albumPickerViewModel: AlbumPickerViewModel by viewModels()

    @Inject
    lateinit var navigator: GalleryNavigator

    @Inject
    lateinit var photoActionsNavigator: PhotoActionsNavigator

    @Inject
    lateinit var config: Config

    @EncryptedImageLoader
    @Inject
    lateinit var encryptedImageLoader: ImageLoader

    @Inject
    lateinit var showNewsDialog: ShowNewsDialogUseCase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            CompositionLocalProvider(
                LocalEncryptedImageLoader provides encryptedImageLoader,
                LocalConfig provides config,
            ) {
                GalleryScreen(viewModel, albumPickerViewModel)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        finishOnBackWhileStarted(
            enabled = config.galleryStartPage == StartPage.AllFiles,
        )

        launchLifecycleAwareJob {
            viewModel.eventsFlow.collect { event ->
                navigator.navigate(event, this)
            }
        }

        launchLifecycleAwareJob {
            viewModel.photoActions.collect { action ->
                photoActionsNavigator.navigate(action, findNavController(), this)
            }
        }

        showNewsDialog(parentFragmentManager)
    }
}