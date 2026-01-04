


package dev.leonlatsch.photok.backup.ui

import android.net.Uri
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.backup.domain.BackupStrategy
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.uicomponnets.base.processdialogs.BaseProcessBottomSheetDialogFragment


/**
 * Process fragment for backing up photos.
 * See [BackupViewModel]
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class BackupBottomSheetDialogFragment(
    private val uri: Uri,
    private val strategy: BackupStrategy.Name
) : BaseProcessBottomSheetDialogFragment<Photo>(
    itemSource = null,
    processingLabelTextResource = strategy.title,
    canAbort = true,
) {
    override val viewModel: BackupViewModel by viewModels()

    override fun prepareViewModel(items: List<Photo>?) {
        super.prepareViewModel(items)
        viewModel.uri = uri
        viewModel.strategyName = strategy
    }
}

package dev.leonlatsch.photok.backup.ui

import android.net.Uri
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.backup.domain.BackupStrategy
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.uicomponnets.base.processdialogs.BaseProcessBottomSheetDialogFragment


/**
 * Process fragment for backing up photos.
 * See [BackupViewModel]
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class BackupBottomSheetDialogFragment(
    private val uri: Uri,
    private val strategy: BackupStrategy.Name
) : BaseProcessBottomSheetDialogFragment<Photo>(
    itemSource = null,
    processingLabelTextResource = strategy.title,
    canAbort = true,
) {
    override val viewModel: BackupViewModel by viewModels()

    override fun prepareViewModel(items: List<Photo>?) {
        super.prepareViewModel(items)
        viewModel.uri = uri
        viewModel.strategyName = strategy
    }
}