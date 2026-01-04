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
import dev.leonlatsch.photok.gallery.components.AlbumPickerViewModel
import dev.leonlatsch.photok.gallery.ui.compose.GalleryScreen
import dev.leonlatsch.photok.gallery.ui.navigation.GalleryNavigator
import dev.leonlatsch.photok.gallery.ui.navigation.PhotoActionsNavigator
import dev.leonlatsch.photok.imageloading.compose.LocalEncryptedImageLoader
import dev.leonlatsch.photok.imageloading.di.EncryptedImageLoader
import dev.leonlatsch.photok.news.newfeatures.ui.ShowNewsDialogUseCase
import dev.leonlatsch.photok.other.extensions.finishOnBackWhileStarted
import dev.leonlatsch.photok.other.extensions.launchLifecycleAwareJob
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.domain.models.StartPage
import dev.leonlatsch.photok.settings.ui.compose.LocalConfig
import dev.leonlatsch.photok.ui.LocalFragment
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
                LocalFragment provides this@GalleryFragment,
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