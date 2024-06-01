/*
 *   Copyright 2020-2021 Leon Latsch
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

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.imageloading.compose.LocalEncryptedImageLoader
import dev.leonlatsch.photok.imageviewer.ui.compose.PhotoViewHolderContent
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.other.*
import dev.leonlatsch.photok.ui.theme.AppTheme

/**
 * ViewHolder for a fullscreen photo.
 * Loads data async on binding finished.
 *
 * @param parent for inflating layout
 * @param context Needed to load data
 * @param photoRepository To load photo data
 * @param onZoomed Block top be called on image zoomed
 * @param onClick Block to be called on image click
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class PhotoViewHolder(
    parent: ViewGroup,
    private val encryptedImageLoader: ImageLoader,
    private val context: Context,
    private val onClick: () -> Unit,
    private val navController: NavController,
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.view_photo_item, parent, false)
) {

    /**
     * Called by Adapters onBindViewHolder.
     *
     * @param id The photo's id
     */
    fun bindTo(photo: Photo) {
        val composeView = itemView as ComposeView

        composeView.setContent {
            AppTheme {
                CompositionLocalProvider(LocalEncryptedImageLoader provides encryptedImageLoader)  {
                    PhotoViewHolderContent(
                        photo = photo,
                        onClick = onClick,
                        onPlayVideo = { openVideoPlayer(photo) }
                    )
                }
            }
        }
    }

    private fun openVideoPlayer(photo: Photo) {
        val args = bundleOf(INTENT_PHOTO_UUID to photo.uuid)
        navController.navigate(
            R.id.action_imageViewerFragment_to_videoPlayerFragment,
            args
        )
    }
}