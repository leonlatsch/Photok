package dev.leonlatsch.photok.ui.importing

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.importing.ImportViewModel

@AndroidEntryPoint
class ImportFragment : Fragment(R.layout.fragment_import) {

    private val viewModel: ImportViewModel by viewModels()
}