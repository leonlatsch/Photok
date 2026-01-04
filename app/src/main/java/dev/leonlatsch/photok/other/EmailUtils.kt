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