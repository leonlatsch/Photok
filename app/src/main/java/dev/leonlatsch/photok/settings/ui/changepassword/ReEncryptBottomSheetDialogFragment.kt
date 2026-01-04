package dev.leonlatsch.photok.settings.ui.changepassword

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.uicomponnets.base.processdialogs.BaseProcessBottomSheetDialogFragment

/**
 * Process fragment for re-encrypting photos.
 * Cannot be aborted.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class ReEncryptBottomSheetDialogFragment(
    private val oldPassword: String,
    private val newPassword: String,
) : BaseProcessBottomSheetDialogFragment<Photo>(
    null,
    R.string.change_password_reencrypting,
    false
) {

    override val viewModel: ReEncryptViewModel by viewModels()

    override fun prepareViewModel(items: List<Photo>?) {
        super.prepareViewModel(items)
        viewModel.oldPassword = oldPassword
        viewModel.newPassword = newPassword
    }
}