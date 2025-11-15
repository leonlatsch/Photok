/*
 *   Copyright 2020-2024 Leon Latsch
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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.gallery.albums.domain.model.Album

class AlbumAdapter(
    private val albums: List<Album>,
    private val onClickAlbum: (Album)-> Unit,
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    // ViewHolder class
    class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumName: TextView = itemView.findViewById(R.id.album_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.album_details, parent, false)
        return AlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.albumName.text = albums[position].name
        holder.albumName.setOnClickListener {
            onClickAlbum(albums[position])
        }
    }

    override fun getItemCount(): Int = albums.size
}
