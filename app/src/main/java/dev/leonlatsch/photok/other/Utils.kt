/*
 *   Copyright 2020-2022 Leon Latsch
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

package dev.leonlatsch.photok.other

import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.unit.LayoutDirection
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import com.google.gson.GsonBuilder
import timber.log.Timber
import java.io.ByteArrayInputStream

/**
 * Get a file's name.
 *
 * @param contentResolver used to get the file name.
 * @param uri the uri to file file.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
    val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
    contentResolver.query(uri, projection, null, null, null)?.use {
        if (it.moveToFirst()) {
            return it.getString(0)

        }
    }
    return null
}

/**
 * Get the size of a file in bytes
 */
fun getFileSize(contentResolver: ContentResolver, uri: Uri): Long {
    contentResolver.openFileDescriptor(uri, "r")?.use {
        return it.statSize
    }

    return -1L
}

/**
 * Post a [operation] to the main looper.
 */
fun onMain(operation: () -> Unit) = Handler(Looper.getMainLooper()).post(operation)

/**
 * Update the app design.
 */
fun setAppDesign(design: String?) {
    design ?: return

    val nightMode = when (design) {
        "system" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        "light" -> AppCompatDelegate.MODE_NIGHT_NO
        "dark" -> AppCompatDelegate.MODE_NIGHT_YES
        else -> null
    }

    nightMode ?: return
    AppCompatDelegate.setDefaultNightMode(nightMode)
}

fun Fragment.openUrl(url: String?) {
    url ?: return
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    startActivity(intent)
}

/**
 * Reset all orientation exif tags for creating thumbnails
 * and displaying photos with exif data properly.
 */
fun normalizeExifOrientation(bytesWithExif: ByteArray?): Bitmap? {
    bytesWithExif ?: return null
    val bitmap = BitmapFactory.decodeByteArray(bytesWithExif, 0, bytesWithExif.size)
    val orientation = ExifInterface(ByteArrayInputStream(bytesWithExif)).getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_NORMAL -> return bitmap

        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)

        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
            matrix.setRotate(180f)
            matrix.postScale(-1f, 1f)
        }

        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.setRotate(90f)
            matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.setRotate(-90f)
            matrix.postScale(-1f, 1f)
        }
        else -> return bitmap
    }
    return try {
        val bmRotated: Bitmap = Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
        bitmap.recycle()
        bmRotated
    } catch (e: OutOfMemoryError) {
        Timber.e(e)
        bitmap
    }
}

/**
 * Create a Gson object with preferences.
 */
fun createGson() = GsonBuilder()
    .setPrettyPrinting()
    .create()

operator fun PaddingValues.plus(other: PaddingValues): PaddingValues = PaddingValues(
    start = this.calculateStartPadding(LayoutDirection.Ltr) +
            other.calculateStartPadding(LayoutDirection.Ltr),
    top = this.calculateTopPadding() + other.calculateTopPadding(),
    end = this.calculateEndPadding(LayoutDirection.Ltr) +
            other.calculateEndPadding(LayoutDirection.Ltr),
    bottom = this.calculateBottomPadding() + other.calculateBottomPadding(),
)

fun View.statusBarPadding() {
    setOnApplyWindowInsetsListener { v, insets ->
        v.setPadding(0, insets.top(), 0, 0)
        insets
    }
}

fun View.systemBarsPadding() {
    setOnApplyWindowInsetsListener { v, insets ->
        v.setPadding(0, insets.top(), 0, insets.bottom())
        insets
    }
}

/**
 * Thx mozilla
 *
 * https://github.com/mozilla-mobile/android-components/pull/9680/files#diff-9d900219329132b059f18f83b6e2952c5509bcfbf063a571ee5d647f76fa6554
 */
fun WindowInsets.top(): Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        this.getInsets(WindowInsets.Type.systemBars()).top

    } else {
        @Suppress("DEPRECATION")
        this.systemWindowInsetTop
    }

fun WindowInsets.bottom(): Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        this.getInsets(WindowInsets.Type.systemBars()).bottom

    } else {
        @Suppress("DEPRECATION")
        this.systemWindowInsetBottom
    }