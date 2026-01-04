package dev.leonlatsch.photok.forwarddialer

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Forwarder Activity. Opens phone dialer and finishes.
 *
 * @since 1.2.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class ForwardDialerActivity : AppCompatActivity() {

    private val viewModel: ForwardDialerViewModel by viewModels()

    @Inject
    lateinit var navigator: ForwardDialerNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.navigationEvent.observe(this) {
            navigator.navigate(it, this)
        }

        viewModel.evaluateNavigation()
    }
}