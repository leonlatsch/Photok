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

package dev.leonlatsch.photok.gallery.ui

import androidx.core.view.setPadding
import androidx.databinding.ObservableList
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.leonlatsch.photok.databinding.PhotoItemBinding
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import dev.leonlatsch.photok.other.extensions.hide
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.other.onMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * [RecyclerView.ViewHolder] for [Photo].
 * Uses multi selection logic in [PhotoAdapter].
 * Loads the thumbnail
 *
 * @param binding
 * @param photoRepository Used to load the thumbnail
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class PhotoItemViewHolder(
    private val binding: PhotoItemBinding,
    private val lifecycleOwner: LifecycleOwner,
    private val photoRepository: PhotoRepository
) : RecyclerView.ViewHolder(binding.root) {

    var photo: Photo? = null
    private lateinit var adapter: PhotoAdapter

    /**
     * Binds the parent adapter and the photo to the ViewHolder.
     */
    fun bindTo(adapter: PhotoAdapter, photoItem: Photo?) {
        this.photo = photoItem
        this.adapter = adapter

        photo ?: return

        if (photo!!.type.isVideo) {
            binding.photoItemVideoIcon.show()
        } else {
            binding.photoItemVideoIcon.hide()
        }

        binding.photoItemImageView.setOnClickListener {
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

        binding.photoItemImageView.setOnLongClickListener {
            if (!adapter.isMultiSelectMode.value!!) {
                adapter.enableSelection()
                setItemChecked(true)
            }
            true
        }

        adapter.isMultiSelectMode.observe(adapter.lifecycleOwner, {
            if (it) { // When selection gets enabled, show the checkbox
                binding.photoItemCheckBox.show()
            } else {
                binding.photoItemCheckBox.hide()
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

        binding.photoItemCheckBox.isChecked = isSelected
        binding.photoItemImageContainer.setPadding(padding)
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
     * TODO: Move this somewhere else. Data should not be loaded in the view layer
     */
    private fun loadThumbnail() {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            photo ?: return@launch

            val thumbnailBytes = photoRepository.loadThumbnail(photo!!)
            if (thumbnailBytes == null) {
                Timber.d("Error loading thumbnail for photo: $photo.id")
                return@launch
            }

            onMain {
                Glide.with(binding.root.context)
                    .asBitmap()
                    .load(thumbnailBytes)
                    .into(binding.photoItemImageView)
            }
        }
    }
}