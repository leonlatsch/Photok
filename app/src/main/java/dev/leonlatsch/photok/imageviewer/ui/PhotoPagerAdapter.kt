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

import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import dev.leonlatsch.photok.model.repositories.PhotoRepository

/**
 * Adapter for fullscreen photos in a ViewPager.
 *
 * @param photos [List] of photo ids
 * @param photoRepository To load photo data
 * @param onZoomed Block top be called on image zoomed
 * @param onClick Block to be called on image click
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class PhotoPagerAdapter(
    private val photos: List<String>,
    private val photoRepository: PhotoRepository,
    private val navController: NavController,
    private val onZoomed: (zoomed: Boolean) -> Unit,
    private val onClick: () -> Unit
) : RecyclerView.Adapter<PhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder =
        PhotoViewHolder(parent, parent.context, photoRepository, onZoomed, onClick, navController)

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bindTo(photos[position])
    }

    override fun getItemCount(): Int = photos.size
}