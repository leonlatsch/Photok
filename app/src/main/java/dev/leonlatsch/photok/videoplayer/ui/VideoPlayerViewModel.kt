


package dev.leonlatsch.photok.videoplayer.ui

import android.app.Application
import android.net.Uri
import androidx.annotation.OptIn
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.other.onMain
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.uicomponnets.bindings.ObservableViewModel
import dev.leonlatsch.photok.videoplayer.data.AesDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for playing videos.
 *
 * @since 1.3.0
 * @author Leon Latsch
 */
@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    private val app: Application,
    private val photoRepository: PhotoRepository,
    private val encryptionManager: EncryptionManager,
) : ObservableViewModel(app) {

    @get:Bindable
    var player: ExoPlayer? = null
        set(value) {
            field = value
            notifyChange(BR.player, value)
        }

    /**
     * Create and prepare the [player] to play the passed video.
     */
    fun setupPlayer(photoUUID: String) {
        releasePlayer()

        viewModelScope.launch(Dispatchers.IO) {
            val photo = photoRepository.get(photoUUID)

            player = ExoPlayer.Builder(app)
                .setMediaSourceFactory(createMediaSourceFactory())
                .build()
                .apply {
                    onMain {
                        setMediaItem(createMediaItem(photo))
                        prepare()
                        playWhenReady = true
                    }
                }
        }
    }

    @OptIn(UnstableApi::class)
    private fun createMediaSourceFactory(): MediaSource.Factory {
        val aesDataSource = AesDataSource(
            encryptionManager = encryptionManager,
        )

        val factory = DataSource.Factory {
            aesDataSource
        }

        return ProgressiveMediaSource.Factory(factory)
    }

    private fun createMediaItem(photo: Photo): MediaItem {
        val uri = Uri.fromFile(app.getFileStreamPath(photo.internalFileName).canonicalFile)

        return MediaItem.Builder()
            .setMimeType(photo.type.mimeType)
            .setUri(uri)
            .build()
    }

    /**
     * Release the current player
     */
    fun releasePlayer() {
        player?.release()
        player = null
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}

package dev.leonlatsch.photok.videoplayer.ui

import android.app.Application
import android.net.Uri
import androidx.annotation.OptIn
import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.other.onMain
import dev.leonlatsch.photok.security.EncryptionManager
import dev.leonlatsch.photok.uicomponnets.bindings.ObservableViewModel
import dev.leonlatsch.photok.videoplayer.data.AesDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for playing videos.
 *
 * @since 1.3.0
 * @author Leon Latsch
 */
@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    private val app: Application,
    private val photoRepository: PhotoRepository,
    private val encryptionManager: EncryptionManager,
) : ObservableViewModel(app) {

    @get:Bindable
    var player: ExoPlayer? = null
        set(value) {
            field = value
            notifyChange(BR.player, value)
        }

    /**
     * Create and prepare the [player] to play the passed video.
     */
    fun setupPlayer(photoUUID: String) {
        releasePlayer()

        viewModelScope.launch(Dispatchers.IO) {
            val photo = photoRepository.get(photoUUID)

            player = ExoPlayer.Builder(app)
                .setMediaSourceFactory(createMediaSourceFactory())
                .build()
                .apply {
                    onMain {
                        setMediaItem(createMediaItem(photo))
                        prepare()
                        playWhenReady = true
                    }
                }
        }
    }

    @OptIn(UnstableApi::class)
    private fun createMediaSourceFactory(): MediaSource.Factory {
        val aesDataSource = AesDataSource(
            encryptionManager = encryptionManager,
        )

        val factory = DataSource.Factory {
            aesDataSource
        }

        return ProgressiveMediaSource.Factory(factory)
    }

    private fun createMediaItem(photo: Photo): MediaItem {
        val uri = Uri.fromFile(app.getFileStreamPath(photo.internalFileName).canonicalFile)

        return MediaItem.Builder()
            .setMimeType(photo.type.mimeType)
            .setUri(uri)
            .build()
    }

    /**
     * Release the current player
     */
    fun releasePlayer() {
        player?.release()
        player = null
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}