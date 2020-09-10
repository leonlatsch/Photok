package dev.leonlatsch.photok.ui

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.other.PrefManager
import dev.leonlatsch.photok.ui.components.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    @Inject
    lateinit var prefManager: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(mainToolbar)

        mainNavHostFragment.findNavController()
            .addOnDestinationChangedListener {_, destination, _ ->
                when (destination.id) {
                    R.id.galleryFragment, R.id.settingsFragment -> mainAppBarLayout.visibility =
                        View.VISIBLE
                    else -> mainAppBarLayout.visibility = View.GONE
                }
            }
    }
}