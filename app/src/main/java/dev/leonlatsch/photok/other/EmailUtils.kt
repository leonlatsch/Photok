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

package dev.leonlatsch.photok.other

import android.content.Context
import android.content.Intent
import android.net.Uri
import dev.leonlatsch.photok.R
import androidx.core.net.toUri

fun Context.sendEmail(email: String, subject: String, text: String, chooserTitle: String) {
    val emailIntent = Intent(
        Intent.ACTION_SENDTO,
        "mailto:$email?subject=$subject&body=$text".toUri()
    ).apply {
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(
        Intent.createChooser(
            emailIntent,
            chooserTitle
        )
    )
}