


package dev.leonlatsch.photok.uicomponnets

import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.other.onMain

/**
 * Holds Dialogs and Toast presets.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
object Dialogs {

    fun showConfirmDialog(
        context: Context,
        title: String,
        onNegativeButtonClicked: DialogInterface.OnClickListener? = null,
        onPositiveButtonClicked: DialogInterface.OnClickListener,
    ) {
        onMain {
            AlertDialog.Builder(context)
                .setMessage(HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_LEGACY))
                .setPositiveButton(R.string.common_yes, onPositiveButtonClicked)
                .setNegativeButton(R.string.common_no, onNegativeButtonClicked)
                .show()
        }
    }

    fun showLongToast(context: Context, message: String) {
        onMain {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    fun showShortToast(context: Context, message: String) {
        onMain {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}


package dev.leonlatsch.photok.uicomponnets

import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.other.onMain

/**
 * Holds Dialogs and Toast presets.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
object Dialogs {

    fun showConfirmDialog(
        context: Context,
        title: String,
        onNegativeButtonClicked: DialogInterface.OnClickListener? = null,
        onPositiveButtonClicked: DialogInterface.OnClickListener,
    ) {
        onMain {
            AlertDialog.Builder(context)
                .setMessage(HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_LEGACY))
                .setPositiveButton(R.string.common_yes, onPositiveButtonClicked)
                .setNegativeButton(R.string.common_no, onNegativeButtonClicked)
                .show()
        }
    }

    fun showLongToast(context: Context, message: String) {
        onMain {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    fun showShortToast(context: Context, message: String) {
        onMain {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
