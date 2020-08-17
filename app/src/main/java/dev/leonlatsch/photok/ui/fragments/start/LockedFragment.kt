package dev.leonlatsch.photok.ui.fragments.start

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.viewmodels.LockedViewModel

@AndroidEntryPoint
class LockedFragment : Fragment(R.layout.fragment_locked) {

    private val viewModel: LockedViewModel by viewModels()
}