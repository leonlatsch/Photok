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

package dev.leonlatsch.photok.ui.gallery

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.core.view.setPadding
import androidx.databinding.ObservableList
import androidx.recyclerview.widget.RecyclerView
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.database.entity.PhotoType
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.other.hide
import dev.leonlatsch.photok.other.runOnMain
import dev.leonlatsch.photok.other.show
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * [RecyclerView.ViewHolder] for [Photo].
 * Uses multi selection logic in [PhotoAdapter].
 * Loads the thumbnail
 *
 * @param parent The parent [ViewGroup]
 * @param context Required by [photoRepository]
 * @param photoRepository Used to load the thumbnail
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class PhotoItemViewHolder(
    parent: ViewGroup,
    private val context: Context,
    private val photoRepository: PhotoRepository
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.photo_item, parent, false)
) {
    private val imageView: ImageView = itemView.findViewById(R.id.photoItemImageView)
    private val checkBox: CheckBox = itemView.findViewById(R.id.photoItemCheckBox)
    private val videoIcon: ImageView = itemView.findViewById(R.id.photoItemVideoIcon)

    var photo: Photo? = null
    private lateinit var adapter: PhotoAdapter

    /**
     * Binds the parent adapter and the photo to the ViewHolder.
     */
    fun bindTo(adapter: PhotoAdapter, photo: Photo?) {
        this.photo = photo
        this.adapter = adapter

        if (photo?.type == PhotoType.MP4) {
            videoIcon.show()
        }

        imageView.setOnClickListener {
            if (adapter.isMultiSelectMode.value!!) {
                // If the item clicked is the last selected item
                if (adapter.isLastSelectedItem(layoutPosition)) {
                    adapter.disableSelection()
                    return@setOnClickListener
                }
                // Set checked if not already checked
                setItemChecked(!adapter.isItemSelected(layoutPosition))
            } else {
                adapter.viewPhoto(layoutPosition)
            }
        }

        imageView.setOnLongClickListener {
            if (!adapter.isMultiSelectMode.value!!) {
                adapter.enableSelection()
                setItemChecked(true)
            }
            true
        }

        adapter.isMultiSelectMode.observe(adapter.lifecycleOwner, {
            if (it) { // When selection gets enabled, show the checkbox
                checkBox.show()
            } else {
                checkBox.hide()
            }
        })

        adapter.selectedItems.addOnListChangedCallback(onSelectedItemsChanged)

        listChanged()
        loadThumbnail()
    }

    /**
     * Listener for changes in selected images.
     * Calls [listChanged] whatever happens.
     */
    private val onSelectedItemsChanged =
        object : ObservableList.OnListChangedCallback<ObservableList<Int>>() {

            override fun onChanged(sender: ObservableList<Int>?) {
                listChanged()
            }

            override fun onItemRangeChanged(
                sender: ObservableList<Int>?,
                positionStart: Int,
                itemCount: Int
            ) {
                listChanged()
            }

            override fun onItemRangeInserted(
                sender: ObservableList<Int>?,
                positionStart: Int,
                itemCount: Int
            ) {
                listChanged()
            }

            override fun onItemRangeMoved(
                sender: ObservableList<Int>?,
                fromPosition: Int,
                toPosition: Int,
                itemCount: Int
            ) {
                listChanged()
            }

            override fun onItemRangeRemoved(
                sender: ObservableList<Int>?,
                positionStart: Int,
                itemCount: Int
            ) {
                listChanged()
            }

        }

    private fun listChanged() {
        val isSelected = adapter.isItemSelected(layoutPosition)
        val padding = if (isSelected) 20 else 0

        checkBox.isChecked = isSelected
        imageView.setPadding(padding)
    }

    private fun setItemChecked(checked: Boolean) {
        layoutPosition.let {
            if (checked) {
                adapter.addItemToSelection(it)
            } else {
                adapter.removeItemFromSelection(it)
            }
        }
    }

    /**
     * Load the thumbnail for the [photo].
     */
    private fun loadThumbnail() {
        GlobalScope.launch(Dispatchers.IO) {
            val thumbnailBytes =
                photoRepository.readPhotoThumbnailFromInternal(context, photo?.id!!)
            if (thumbnailBytes == null) {
                Timber.d("Error loading thumbnail for photo: $photo.id")
                return@launch
            }
            val thumbnailBitmap =
                BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.size)
            runOnMain { // Set thumbnail in main thread
                imageView.setImageBitmap(thumbnailBitmap)
            }
        }
    }
}