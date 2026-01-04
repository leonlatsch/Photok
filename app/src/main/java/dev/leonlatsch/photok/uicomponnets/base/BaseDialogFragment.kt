


package dev.leonlatsch.photok.uicomponnets.base

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import dev.leonlatsch.photok.R

/**
 * Base for all Dialog Fragments.
 * Sets drawables for rounded corners.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
abstract class BaseDialogFragment : DialogFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set necessary drawables for rounded corners
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.background =
            ResourcesCompat.getDrawable(resources, R.drawable.bg_dialog_round, null)
    }

}

package dev.leonlatsch.photok.uicomponnets.base

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import dev.leonlatsch.photok.R

/**
 * Base for all Dialog Fragments.
 * Sets drawables for rounded corners.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
abstract class BaseDialogFragment : DialogFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set necessary drawables for rounded corners
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.background =
            ResourcesCompat.getDrawable(resources, R.drawable.bg_dialog_round, null)
    }

}