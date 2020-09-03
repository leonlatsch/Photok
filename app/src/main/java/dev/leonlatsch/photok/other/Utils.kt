package dev.leonlatsch.photok.other

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import android.view.View

/**
 * Get a file's name.
 *
 * @param contentResolver used to get the file name.
 * @param uri the uri to file file.
 *
 * TODO: Resolve resource not closed error
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

fun hideLoadingOverlay(overlay: View?) {
    overlay?.visibility = View.GONE
}

fun showLoadingOverlay(overlay: View?) {
    overlay?.visibility = View.VISIBLE
}

fun emptyString(): String {
    return ""
}