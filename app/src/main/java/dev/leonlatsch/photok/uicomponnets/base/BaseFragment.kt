


package dev.leonlatsch.photok.uicomponnets.base

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment

/**
 * Base for all fragments.
 *
 * @since 1.3.0
 * @author Leon Latsch
 */
abstract class BaseFragment : Fragment() {

    /**
     * Set the action toolbar for the activity.
     */
    internal fun setToolbar(toolbar: Toolbar, showTitle: Boolean = false) {
        val activity = (requireActivity() as AppCompatActivity)
        activity.setSupportActionBar(toolbar)

        activity.supportActionBar?.setDisplayShowTitleEnabled(showTitle)
    }
}

package dev.leonlatsch.photok.uicomponnets.base

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment

/**
 * Base for all fragments.
 *
 * @since 1.3.0
 * @author Leon Latsch
 */
abstract class BaseFragment : Fragment() {

    /**
     * Set the action toolbar for the activity.
     */
    internal fun setToolbar(toolbar: Toolbar, showTitle: Boolean = false) {
        val activity = (requireActivity() as AppCompatActivity)
        activity.setSupportActionBar(toolbar)

        activity.supportActionBar?.setDisplayShowTitleEnabled(showTitle)
    }
}