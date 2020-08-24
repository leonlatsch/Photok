package dev.leonlatsch.photok.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.other.FIRST_START
import dev.leonlatsch.photok.other.FIRST_START_DEFAULT
import dev.leonlatsch.photok.other.PrefManager
import kotlinx.android.synthetic.main.activity_start.*
import javax.inject.Inject

@AndroidEntryPoint
class StartActivity : AppCompatActivity() {

    @Inject
    lateinit var prefManager: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        // TEST !!!
        startNavHostFragment.findNavController().navigate(R.id.action_lockedFragment_to_setupFragment)
        return

        // Nav to intro, locked or setup
        if (prefManager.getBoolean(FIRST_START, FIRST_START_DEFAULT)) {
            startNavHostFragment.findNavController()
                .navigate(R.id.action_lockedFragment_to_OnBoardingFragment)
        } else {
            TODO("Determine if a password is set and decide if navigate to LockedFragment or SetupFragment ")
        }
    }
}