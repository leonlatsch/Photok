package dev.leonlatsch.photok.ui.gallery

import android.content.Context
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository

class PhotoAdapter(
    private val context: Context,
    private val photoRepository: PhotoRepository
) : PagingDataAdapter<Photo, PhotoViewHolder>(differCallback) {

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder = PhotoViewHolder(parent, context, photoRepository)

    companion object {
        private val differCallback = object : DiffUtil.ItemCallback<Photo>() {

            override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean =
                oldItem == newItem

        }
    }

}