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

package dev.leonlatsch.photok.imageviewer.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.imageviewer.ui.compose.ImageViewerScreen
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.ui.compose.LocalConfig
import dev.leonlatsch.photok.transcoding.compose.LocalEncryptedImageLoader
import dev.leonlatsch.photok.transcoding.di.EncryptedImageLoader
import dev.leonlatsch.photok.ui.theme.AppTheme
import javax.inject.Inject

/**
 * Fragment to display photos in full size.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class ImageViewerFragment : Fragment() {

    private val args: ImageViewerFragmentArgs by navArgs()

    @EncryptedImageLoader
    @Inject
    lateinit var encryptedImageLoader: ImageLoader

    @Inject
    lateinit var config: Config

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setBackgroundColor(Color.BLACK)
            setContent {
                AppTheme {
                    CompositionLocalProvider(
                        LocalEncryptedImageLoader provides encryptedImageLoader,
                        LocalConfig provides config,
                    ) {
                        ImageViewerScreen(
                            navController = findNavController(),
                            photoUuid = args.photoUuid,
                            albumUuid = args.albumUuid.takeIf { it.isNotEmpty() },
                        )
                    }
                }
            }
        }
    }
}