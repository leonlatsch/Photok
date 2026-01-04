


package dev.leonlatsch.photok.gallery.albums.ui.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.albums.ui.AlbumsUiEvent
import dev.leonlatsch.photok.gallery.albums.ui.AlbumsViewModel
import dev.leonlatsch.photok.gallery.components.ImportSharedDialog
import dev.leonlatsch.photok.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(viewModel: AlbumsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    AppTheme {
        Scaffold(
            topBar = {
                LargeTopAppBar(
                    title = { Text(stringResource(R.string.gallery_albums_label)) },
                    scrollBehavior = scrollBehavior,
                )
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        ) { contentPadding ->
            val modifier = Modifier.padding(top = contentPadding.calculateTopPadding())

            when (uiState) {
                is AlbumsUiState.Empty -> AlbumsPlaceholder(
                    handleUiEvent = { viewModel.handleUiEvent(it) },
                    modifier = modifier,

                    )

                is AlbumsUiState.Content -> AlbumsContent(
                    content = uiState as AlbumsUiState.Content,
                    handleUiEvent = { viewModel.handleUiEvent(it) },
                    modifier = modifier,
                )
            }

            CreateAlbumDialog(
                show = uiState.showCreateDialog,
                onDismissRequest = {
                    viewModel.handleUiEvent(AlbumsUiEvent.HideCreateDialog)
                },
            )

            ImportSharedDialog()
        }
    }
}


package dev.leonlatsch.photok.gallery.albums.ui.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.albums.ui.AlbumsUiEvent
import dev.leonlatsch.photok.gallery.albums.ui.AlbumsViewModel
import dev.leonlatsch.photok.gallery.components.ImportSharedDialog
import dev.leonlatsch.photok.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(viewModel: AlbumsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    AppTheme {
        Scaffold(
            topBar = {
                LargeTopAppBar(
                    title = { Text(stringResource(R.string.gallery_albums_label)) },
                    scrollBehavior = scrollBehavior,
                )
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        ) { contentPadding ->
            val modifier = Modifier.padding(top = contentPadding.calculateTopPadding())

            when (uiState) {
                is AlbumsUiState.Empty -> AlbumsPlaceholder(
                    handleUiEvent = { viewModel.handleUiEvent(it) },
                    modifier = modifier,

                    )

                is AlbumsUiState.Content -> AlbumsContent(
                    content = uiState as AlbumsUiState.Content,
                    handleUiEvent = { viewModel.handleUiEvent(it) },
                    modifier = modifier,
                )
            }

            CreateAlbumDialog(
                show = uiState.showCreateDialog,
                onDismissRequest = {
                    viewModel.handleUiEvent(AlbumsUiEvent.HideCreateDialog)
                },
            )

            ImportSharedDialog()
        }
    }
}
