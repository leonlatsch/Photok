


package dev.leonlatsch.photok.backup.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.DialogRestoreBackupBinding
import dev.leonlatsch.photok.other.extensions.hide
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.uicomponnets.bindings.BindableDialogFragment

/**
 * Dialog for loading and validating a backup file.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class RestoreBackupDialogFragment(
    private val uri: Uri
) : BindableDialogFragment<DialogRestoreBackupBinding>(R.layout.dialog_restore_backup) {

    private val viewModel: RestoreBackupViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.addOnPropertyChange<RestoreState>(BR.restoreState) {
            when (it) {
                RestoreState.INITIALIZE -> {
                    binding.restoreDetails.hide()
                    binding.validateBackupFilename.hide()
                    binding.restoreButton.hide()
                    binding.restoreProgressIndicator.show()
                }
                RestoreState.FILE_VALID -> {
                    binding.restoreDetails.show()
                    binding.validateBackupFilename.show()
                    binding.restoreButton.show()
                    binding.restoreProgressIndicator.hide()
                }
                RestoreState.FILE_INVALID -> {
                    binding.restoreInvalidWarning.show()
                    binding.restoreCloseButton.show()
                    binding.restoreProgressIndicator.hide()
                }
                RestoreState.RESTORING -> {
                    viewModel.zipFileName = getString(R.string.backup_restore_processing)
                    binding.restoreProgressIndicator.show()
                    binding.restoreDetails.hide()
                    binding.restoreButton.hide()
                }
                RestoreState.FINISHED -> {
                    viewModel.zipFileName = getString(R.string.process_finished)
                    binding.restoreProgressIndicator.hide()
                    binding.restoreCloseButton.show()
                }

                RestoreState.FINISHED_WITH_ERRORS -> {
                    viewModel.zipFileName = getString(R.string.process_finished)
                    binding.restoreProgressIndicator.hide()
                    binding.restoreFailuresWarning.show()
                    binding.restoreCloseButton.show()
                }
            }
        }

        viewModel.zipFileName = getString(R.string.backup_restore_validating)
        viewModel.loadAndValidateBackup(uri)
    }

    /**
     * Starts the [UnlockBackupDialogFragment].
     * Called by ui.
     */
    fun onRestoreAndUnlock() {
        val unlockDialog =
            UnlockBackupDialogFragment(viewModel.metaData!!.password) { origPassword ->
                viewModel.restoreBackup(origPassword)
            }
        unlockDialog.show(requireActivity().supportFragmentManager)
    }

    override fun bind(binding: DialogRestoreBackupBinding) {
        super.bind(binding)
        binding.context = this
        binding.viewModel = viewModel
    }
}

package dev.leonlatsch.photok.backup.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.DialogRestoreBackupBinding
import dev.leonlatsch.photok.other.extensions.hide
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.uicomponnets.bindings.BindableDialogFragment

/**
 * Dialog for loading and validating a backup file.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class RestoreBackupDialogFragment(
    private val uri: Uri
) : BindableDialogFragment<DialogRestoreBackupBinding>(R.layout.dialog_restore_backup) {

    private val viewModel: RestoreBackupViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.addOnPropertyChange<RestoreState>(BR.restoreState) {
            when (it) {
                RestoreState.INITIALIZE -> {
                    binding.restoreDetails.hide()
                    binding.validateBackupFilename.hide()
                    binding.restoreButton.hide()
                    binding.restoreProgressIndicator.show()
                }
                RestoreState.FILE_VALID -> {
                    binding.restoreDetails.show()
                    binding.validateBackupFilename.show()
                    binding.restoreButton.show()
                    binding.restoreProgressIndicator.hide()
                }
                RestoreState.FILE_INVALID -> {
                    binding.restoreInvalidWarning.show()
                    binding.restoreCloseButton.show()
                    binding.restoreProgressIndicator.hide()
                }
                RestoreState.RESTORING -> {
                    viewModel.zipFileName = getString(R.string.backup_restore_processing)
                    binding.restoreProgressIndicator.show()
                    binding.restoreDetails.hide()
                    binding.restoreButton.hide()
                }
                RestoreState.FINISHED -> {
                    viewModel.zipFileName = getString(R.string.process_finished)
                    binding.restoreProgressIndicator.hide()
                    binding.restoreCloseButton.show()
                }

                RestoreState.FINISHED_WITH_ERRORS -> {
                    viewModel.zipFileName = getString(R.string.process_finished)
                    binding.restoreProgressIndicator.hide()
                    binding.restoreFailuresWarning.show()
                    binding.restoreCloseButton.show()
                }
            }
        }

        viewModel.zipFileName = getString(R.string.backup_restore_validating)
        viewModel.loadAndValidateBackup(uri)
    }

    /**
     * Starts the [UnlockBackupDialogFragment].
     * Called by ui.
     */
    fun onRestoreAndUnlock() {
        val unlockDialog =
            UnlockBackupDialogFragment(viewModel.metaData!!.password) { origPassword ->
                viewModel.restoreBackup(origPassword)
            }
        unlockDialog.show(requireActivity().supportFragmentManager)
    }

    override fun bind(binding: DialogRestoreBackupBinding) {
        super.bind(binding)
        binding.context = this
        binding.viewModel = viewModel
    }
}