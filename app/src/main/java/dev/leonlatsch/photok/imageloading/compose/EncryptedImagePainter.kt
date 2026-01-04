package dev.leonlatsch.photok.imageloading.compose

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.imageloading.compose.model.EncryptedImageRequestData

/**
 * Image Painter for encrypted images. Uses encrypted image fetcher if [LocalEncryptedImageLoader] provides it
 */
@Composable
fun rememberEncryptedImagePainter(
    data: EncryptedImageRequestData,
    @DrawableRes placeholder: Int = R.color.lightGray,
): AsyncImagePainter {
    val context = LocalContext.current

    return rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(data)
            .placeholder(placeholder)
            .fallback(R.color.design_default_color_error)
            .error(R.color.design_default_color_error)
            .build(),
        imageLoader = LocalEncryptedImageLoader.current ?: LocalContext.current.imageLoader
    )
}