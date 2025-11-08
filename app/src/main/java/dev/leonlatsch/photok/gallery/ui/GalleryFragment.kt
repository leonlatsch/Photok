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

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.ui.components.AlbumPickerViewModel
import dev.leonlatsch.photok.gallery.ui.compose.GalleryScreen
import dev.leonlatsch.photok.gallery.ui.importing.ImportBottomSheetDialogFragment
import dev.leonlatsch.photok.gallery.ui.navigation.GalleryNavigator
import dev.leonlatsch.photok.gallery.ui.navigation.PhotoActionsNavigator
import dev.leonlatsch.photok.imageloading.compose.LocalEncryptedImageLoader
import dev.leonlatsch.photok.imageloading.di.EncryptedImageLoader
import dev.leonlatsch.photok.model.repositories.ImportSource
import dev.leonlatsch.photok.other.extensions.finishOnBackWhileStarted
import dev.leonlatsch.photok.other.extensions.launchLifecycleAwareJob
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.ui.compose.LocalConfig
import dev.leonlatsch.photok.ui.theme.positiveButton
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            dispatchIntent(intent = requireActivity().intent)
            ImportSharedQuestion()
            CompositionLocalProvider(
                LocalEncryptedImageLoader provides encryptedImageLoader,
                LocalConfig provides config,
            ) {
                GalleryScreen(viewModel, albumPickerViewModel)
            }
        }
    }

    @Composable
    private fun ImportSharedQuestion(){
        if(viewModel.showImportDialogue) {
            val filePathCollection = viewModel.getSharedUriList() ?: emptyList()
            ImportDialogue(
                content = String.format(
                    requireContext().getString(R.string.import_sharted_question),
                    filePathCollection.size
                ),
                onNegativeTextOnClick = {
                    viewModel.clearSharedUriList()
                },
                onPositiveTextOnClick = {
                    startImportOfSharedUris(filePathCollection)
                }
            )
        }
    }

    /**
     * Start importing after the overview of photos.
     */
    private fun startImportOfSharedUris(uriCollection: List<Uri>) {
        ImportBottomSheetDialogFragment(uriCollection, importSource = ImportSource.Share)
            .apply {
                this.onProcessDone = { this@GalleryFragment.viewModel.clearSharedUriList() }
            }
            .show(
                this.parentFragmentManager,
                ImportBottomSheetDialogFragment::class.qualifiedName
            )
    }

    private fun dispatchIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SEND -> intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { uri ->
                viewModel.setSharedUriList(uri)
            }

            Intent.ACTION_SEND_MULTIPLE ->
                intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.forEach { uri ->
                    viewModel.setSharedUriList(uri)
                }
        }
        if(viewModel.getSharedUriList().isNotEmpty())
        {
            viewModel.showImportDialogue = true
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        finishOnBackWhileStarted()

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

        viewModel.checkForNewFeatures()

    }


    @Composable
    private fun ImportDialogue(content: String = "", onPositiveTextOnClick: ()-> Unit, onNegativeTextOnClick: ()-> Unit) {
        AlertDialog(
            onDismissRequest = onNegativeTextOnClick,
            title =  null,
            text = {
                Text(content, color = Color.Black, fontSize = 15.sp)
            },
            confirmButton = {
                TextButton(onClick = onPositiveTextOnClick) {
                    Text("YES", color = positiveButton, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onNegativeTextOnClick) {
                    Text("NO", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}