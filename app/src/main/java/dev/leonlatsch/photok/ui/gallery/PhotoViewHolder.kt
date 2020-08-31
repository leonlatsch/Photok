package dev.leonlatsch.photok.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.model.database.entity.Photo

class PhotoViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.photo_item, parent, false)
) {

    private val fileName = itemView.findViewById<TextView>(R.id.photoItemFileName)
    var photo: Photo? = null

    fun bindTo(photo: Photo?) {
        this.photo = photo
        fileName.text = photo?.fileName
    }
}