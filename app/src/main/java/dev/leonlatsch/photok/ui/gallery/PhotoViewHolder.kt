package dev.leonlatsch.photok.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.model.repositories.PhotoRepository

class PhotoViewHolder(
    parent: ViewGroup,
    private val photoRepository: PhotoRepository
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.photo_item, parent, false)
) {
    private val imageView: ImageView = itemView.findViewById(R.id.photoItemImageView)
    var photo: Photo? = null

    fun bindTo(photo: Photo?) {
        this.photo = photo
    }
}