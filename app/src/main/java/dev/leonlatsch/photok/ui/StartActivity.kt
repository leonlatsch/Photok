package dev.leonlatsch.photok.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import kotlinx.android.synthetic.main.activity_start.*

@AndroidEntryPoint
class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        // TODO: Determine if it's the first start and navigate to tutorial.
        // TODO: Determine if a password is set and decide if navigate to LockedFragment or SetupFragment

        // Nav to tutorial for now
        startNavHostFragment.findNavController().navigate(R.id.action_lockedFragment_to_tutorialFragment)
    }
}