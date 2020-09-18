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
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import kotlin.reflect.KFunction1

class PhotoAdapter(
    private val context: Context,
    private val photoRepository: PhotoRepository,
    private val viewPhotoCallback: KFunction1<Int, Unit>,
    val lifecycleOwner: LifecycleOwner
) : PagingDataAdapter<Photo, PhotoViewHolder>(differCallback) {

    val selectedItems = mutableListOf<Int>()
    var isMultiSelectMode: MutableLiveData<Boolean> = MutableLiveData(false)

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bindTo(this, position, getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder =
        PhotoViewHolder(parent, context, photoRepository)

    fun viewPhoto(position: Int) {
        viewPhotoCallback.invoke(getItem(position)?.id!!)
    }

    companion object {
        private val differCallback = object : DiffUtil.ItemCallback<Photo>() {

            override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean =
                oldItem == newItem

        }
    }

}