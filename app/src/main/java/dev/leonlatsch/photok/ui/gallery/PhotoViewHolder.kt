/*
 *   Copyright 2020 Leon Latsch
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

package dev.leonlatsch.photok.ui.gallery

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class PhotoViewHolder(
    parent: ViewGroup,
    private val context: Context,
    private val photoRepository: PhotoRepository
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.photo_item, parent, false)
) {
    private val imageView: ImageView = itemView.findViewById(R.id.photoItemImageView)
    private val checkBox: CheckBox = itemView.findViewById(R.id.photoItemCheckBox)
    var photo: Photo? = null
    var position: Int? = null

    fun bindTo(adapter: PhotoAdapter, position: Int, photo: Photo?) {
        this.photo = photo
        this.position = position
        loadThumbnail()
        imageView.setOnClickListener {
            if (adapter.isMultiSelectMode.value!!) {
                if (adapter.selectedItems.size == 1 && adapter.selectedItems.contains(position)) {
                    adapter.selectedItems.clear()
                    adapter.isMultiSelectMode.postValue(false)
                    return@setOnClickListener
                }
                if (adapter.selectedItems.contains(position)) {
                    checkBox.isChecked = false
                    adapter.selectedItems.remove(position)
                } else {
                    checkBox.isChecked = true
                    adapter.selectedItems.add(position)
                }
            } else {
                adapter.viewPhoto(position)
            }
        }
        imageView.setOnLongClickListener {
            if (!adapter.isMultiSelectMode.value!!) {
                adapter.isMultiSelectMode.postValue(true)
                adapter.selectedItems.add(position)
                checkBox.isChecked = true
            }
            true
        }

        adapter.isMultiSelectMode.observe(adapter.lifecycleOwner, {
            if (it) {
                checkBox.visibility = View.VISIBLE
            } else {
                checkBox.visibility = View.GONE
            }
        })
    }

    private fun loadThumbnail() {
        GlobalScope.launch {
            val thumbnailBytes = photoRepository.readPhotoThumbnailData(context, photo?.id!!)
            if (thumbnailBytes == null) {
                Timber.d("Error loading thumbnail for photo: $photo.id")
                return@launch
            }
            val thumbnailBitmap =
                BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.size)
            Handler(context.mainLooper).post {
                imageView.setImageBitmap(thumbnailBitmap)
            }
        }
    }
}