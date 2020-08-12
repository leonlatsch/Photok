package dev.leonlatsch.photok.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import dev.leonlatsch.photok.R

class LockedFragment : Fragment(R.layout.fragment_locked) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Determine if it's the first start and navigate to tutorial.
        // TODO: Determine if a password is set and decide if navigate to LockedFragment or SetupFragment
    }
}