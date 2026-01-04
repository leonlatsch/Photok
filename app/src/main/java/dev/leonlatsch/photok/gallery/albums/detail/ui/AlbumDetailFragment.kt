


package dev.leonlatsch.photok.gallery.albums.detail.ui

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
import dev.leonlatsch.photok.gallery.albums.detail.ui.compose.AlbumDetailScreen
import dev.leonlatsch.photok.gallery.ui.navigation.PhotoActionsNavigator
import dev.leonlatsch.photok.imageloading.compose.LocalEncryptedImageLoader
import dev.leonlatsch.photok.imageloading.di.EncryptedImageLoader
import dev.leonlatsch.photok.other.extensions.assistedViewModel
import dev.leonlatsch.photok.other.extensions.launchLifecycleAwareJob
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.ui.compose.LocalConfig
import dev.leonlatsch.photok.ui.theme.AppTheme
import javax.inject.Inject

@AndroidEntryPoint
class AlbumDetailFragment : Fragment() {

    private val args: AlbumDetailFragmentArgs by navArgs()

    private val viewModel by assistedViewModel<AlbumDetailViewModel.Factory, AlbumDetailViewModel> {
        it.create(args.albumUuid)
    }

    @Inject
    lateinit var photoActionsNavigator: PhotoActionsNavigator

    @Inject
    lateinit var albumDetailNavigator: AlbumDetailNavigator

    @Inject
    lateinit var config: Config

    @EncryptedImageLoader
    @Inject
    lateinit var encryptedImageLoader: ImageLoader

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    CompositionLocalProvider(
                        LocalEncryptedImageLoader provides encryptedImageLoader,
                        LocalConfig provides config,
                    ) {
                        AlbumDetailScreen(viewModel, findNavController())
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launchLifecycleAwareJob {
            viewModel.photoActions.collect { action ->
                photoActionsNavigator.navigate(action, findNavController(), this)
            }
        }

        launchLifecycleAwareJob {
            viewModel.navEvents.collect { event ->
                albumDetailNavigator.navigate(event, this)
            }
        }
    }
}


package dev.leonlatsch.photok.gallery.albums.detail.ui

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
import dev.leonlatsch.photok.gallery.albums.detail.ui.compose.AlbumDetailScreen
import dev.leonlatsch.photok.gallery.ui.navigation.PhotoActionsNavigator
import dev.leonlatsch.photok.imageloading.compose.LocalEncryptedImageLoader
import dev.leonlatsch.photok.imageloading.di.EncryptedImageLoader
import dev.leonlatsch.photok.other.extensions.assistedViewModel
import dev.leonlatsch.photok.other.extensions.launchLifecycleAwareJob
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.settings.ui.compose.LocalConfig
import dev.leonlatsch.photok.ui.theme.AppTheme
import javax.inject.Inject

@AndroidEntryPoint
class AlbumDetailFragment : Fragment() {

    private val args: AlbumDetailFragmentArgs by navArgs()

    private val viewModel by assistedViewModel<AlbumDetailViewModel.Factory, AlbumDetailViewModel> {
        it.create(args.albumUuid)
    }

    @Inject
    lateinit var photoActionsNavigator: PhotoActionsNavigator

    @Inject
    lateinit var albumDetailNavigator: AlbumDetailNavigator

    @Inject
    lateinit var config: Config

    @EncryptedImageLoader
    @Inject
    lateinit var encryptedImageLoader: ImageLoader

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    CompositionLocalProvider(
                        LocalEncryptedImageLoader provides encryptedImageLoader,
                        LocalConfig provides config,
                    ) {
                        AlbumDetailScreen(viewModel, findNavController())
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launchLifecycleAwareJob {
            viewModel.photoActions.collect { action ->
                photoActionsNavigator.navigate(action, findNavController(), this)
            }
        }

        launchLifecycleAwareJob {
            viewModel.navEvents.collect { event ->
                albumDetailNavigator.navigate(event, this)
            }
        }
    }
}
