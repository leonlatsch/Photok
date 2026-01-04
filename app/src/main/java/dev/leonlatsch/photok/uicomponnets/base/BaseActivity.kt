package dev.leonlatsch.photok.uicomponnets.base

import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.settings.data.Config

/**
 * Base for all activities.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!config.securityAllowScreenshots) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    /**
     * Abstract [Config], must be injected in implementations.
     */
    abstract var config: Config

    /**
     * Hide the soft-keyboard of displayed.
     */
    fun hideKeyboard() {
        val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
}