


package dev.leonlatsch.photok.gallery.ui.importing

import android.net.Uri
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.model.repositories.ImportSource
import dev.leonlatsch.photok.uicomponnets.base.processdialogs.BaseProcessBottomSheetDialogFragment


/**
 * Process Fragment to import photos.
 * Uses [ImportViewModel] for the process.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class ImportBottomSheetDialogFragment(
    uris: List<Uri>,
    private val albumUUID: String? = "",
    private val importSource: ImportSource,
) : BaseProcessBottomSheetDialogFragment<Uri>(
    uris,
    R.string.import_importing,
    true
) {

    override val viewModel: ImportViewModel by viewModels()

    override fun prepareViewModel(items: List<Uri>?) {
        viewModel.albumUUID = albumUUID
        viewModel.importSource = importSource
        super.prepareViewModel(items?.reversed()) // Reverse list to keep order in system gallery
    }
}

package dev.leonlatsch.photok.gallery.ui.importing

import android.net.Uri
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.model.repositories.ImportSource
import dev.leonlatsch.photok.uicomponnets.base.processdialogs.BaseProcessBottomSheetDialogFragment


/**
 * Process Fragment to import photos.
 * Uses [ImportViewModel] for the process.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class ImportBottomSheetDialogFragment(
    uris: List<Uri>,
    private val albumUUID: String? = "",
    private val importSource: ImportSource,
) : BaseProcessBottomSheetDialogFragment<Uri>(
    uris,
    R.string.import_importing,
    true
) {

    override val viewModel: ImportViewModel by viewModels()

    override fun prepareViewModel(items: List<Uri>?) {
        viewModel.albumUUID = albumUUID
        viewModel.importSource = importSource
        super.prepareViewModel(items?.reversed()) // Reverse list to keep order in system gallery
    }
}