


package dev.leonlatsch.photok.imageviewer.ui

import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import dev.leonlatsch.photok.model.database.entity.Photo

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
    private val photos: List<Photo>,
    private val encryptedImageLoader: ImageLoader,
    private val navController: NavController,
    private val onClick: () -> Unit
) : RecyclerView.Adapter<PhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder =
        PhotoViewHolder(
            parent = parent,
            encryptedImageLoader = encryptedImageLoader,
            context = parent.context,
            onClick = onClick,
            navController = navController
        )

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bindTo(photos[position])
    }

    override fun getItemCount(): Int = photos.size
}

package dev.leonlatsch.photok.imageviewer.ui

import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import dev.leonlatsch.photok.model.database.entity.Photo

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
    private val photos: List<Photo>,
    private val encryptedImageLoader: ImageLoader,
    private val navController: NavController,
    private val onClick: () -> Unit
) : RecyclerView.Adapter<PhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder =
        PhotoViewHolder(
            parent = parent,
            encryptedImageLoader = encryptedImageLoader,
            context = parent.context,
            onClick = onClick,
            navController = navController
        )

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bindTo(photos[position])
    }

    override fun getItemCount(): Int = photos.size
}