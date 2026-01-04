package dev.leonlatsch.photok.gallery.ui.menu

import android.net.Uri
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.uicomponnets.base.processdialogs.BaseProcessBottomSheetDialogFragment

/**
 * Process fragment to export photos.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class ExportBottomSheetDialogFragment(
    photos: List<Photo>,
    private val target: Uri,
) : BaseProcessBottomSheetDialogFragment<Photo>(
    photos,
    R.string.export_exporting,
    true
) {

    override val viewModel: ExportViewModel by viewModels()

    override fun prepareViewModel(items: List<Photo>?) {
        super.prepareViewModel(items)
        viewModel.target = target
    }
}