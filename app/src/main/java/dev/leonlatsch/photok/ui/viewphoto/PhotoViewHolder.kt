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

package dev.leonlatsch.photok.ui.viewphoto

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ortiz.touchview.TouchImageView
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.other.normalizeExifOrientation
import dev.leonlatsch.photok.other.runOnMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

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
    private val context: Context,
    private val photoRepository: PhotoRepository,
    private val onZoomed: (zoomed: Boolean) -> Unit,
    private val onClick: () -> Unit
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.view_photo_item, parent, false)
) {
    private val imageView: TouchImageView = itemView.findViewById(R.id.photoImageView)
    var photoId: Int = 0

    /**
     * Called by Adapters onBindViewHolder.
     *
     * @param id The photo's id
     */
    fun bindTo(id: Int?) {
        id ?: return
        photoId = id

        imageView.setOnTouchImageViewListener(object : TouchImageView.OnTouchImageViewListener {
            override fun onMove() {
                onZoomed(imageView.isZoomed)
            }
        })
        imageView.setOnClickListener {
            onClick()
        }

        loadPhoto()
    }

    private fun loadPhoto() {
        GlobalScope.launch(Dispatchers.IO) {
            val photoBytes = photoRepository.readPhotoFileFromInternal(context, photoId)
            if (photoBytes == null) {
                Timber.d("Error loading photo data for photo: $photoId")
                return@launch
            }

            val bitmap = normalizeExifOrientation(photoBytes)
            runOnMain {
                imageView.setImageBitmap(bitmap)
            }
        }
    }
}