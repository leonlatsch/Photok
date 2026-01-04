


package dev.leonlatsch.photok.gallery.ui.menu

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.uicomponnets.base.processdialogs.BaseProcessBottomSheetDialogFragment

/**
 * Process fragment to delete photos.
 * Uses [DeleteViewModel] for the process.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class DeleteBottomSheetDialogFragment(
    photos: List<Photo>
) : BaseProcessBottomSheetDialogFragment<Photo>(
    photos,
    R.string.delete_deleting,
    true
) {

    override val viewModel: DeleteViewModel by viewModels()
}

package dev.leonlatsch.photok.gallery.ui.menu

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.uicomponnets.base.processdialogs.BaseProcessBottomSheetDialogFragment

/**
 * Process fragment to delete photos.
 * Uses [DeleteViewModel] for the process.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class DeleteBottomSheetDialogFragment(
    photos: List<Photo>
) : BaseProcessBottomSheetDialogFragment<Photo>(
    photos,
    R.string.delete_deleting,
    true
) {

    override val viewModel: DeleteViewModel by viewModels()
}