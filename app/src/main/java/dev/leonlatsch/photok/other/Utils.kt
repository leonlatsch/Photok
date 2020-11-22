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

package dev.leonlatsch.photok.other

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.view.Window
import androidx.fragment.app.Fragment
import dev.leonlatsch.photok.BaseApplication
import dev.leonlatsch.photok.ui.StartActivity

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
 * Toggle the system ui.
 * Used for fullscreen.
 */
fun toggleSystemUI(window: Window?) {
    window ?: return
    val uiOptions: Int = window.decorView.systemUiVisibility
    var newUiOptions = uiOptions

    newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_FULLSCREEN
    newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_IMMERSIVE

    window.decorView.systemUiVisibility = newUiOptions
}

/**
 * Restart the app. Clear back stack and start [StartActivity]
 */
fun restartAppLifecycle(context: Context) {
    val intent = Intent(context, StartActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    context.startActivity(intent)
}

/**
 * Post a [operation] to the main looper.
 */
fun runOnMain(operation: () -> Unit) = Handler(Looper.getMainLooper()).post(operation)

/**
 * Extension for starting an activity for result and disable lock timer in [BaseApplication].
 */
fun Fragment.startActivityForResultAndIgnoreTimer(intent: Intent, reqCode: Int) {
    startActivityForResult(intent, reqCode)
    BaseApplication.ignoreNextTimeout()
}