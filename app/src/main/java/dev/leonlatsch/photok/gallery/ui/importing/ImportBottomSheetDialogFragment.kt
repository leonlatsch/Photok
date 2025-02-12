/*
 *   Copyright 2020-2024 Leon Latsch
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package dev.leonlatsch.photok.gallery.ui.importing

import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.model.repositories.ImportSource
import dev.leonlatsch.photok.settings.data.Config
import dev.leonlatsch.photok.uicomponnets.base.processdialogs.BaseProcessBottomSheetDialogFragment
import timber.log.Timber
import javax.inject.Inject

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

    private val deleteRequestLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
        Timber.d("Delete Request result: ${it.resultCode}")
    }

    @Inject
    lateinit var config: Config

    override val viewModel: ImportViewModel by viewModels()

    override fun prepareViewModel(items: List<Uri>?) {
        viewModel.albumUUID = albumUUID
        viewModel.importSource = importSource
        super.prepareViewModel(items?.reversed()) // Reverse list to keep order in system gallery
    }

    override fun onProcessingDone() {
        if (config.deleteImportedFiles && viewModel.importingFromPhotoPicker && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestDelete(viewModel.items)
        }
        super.onProcessingDone()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestDelete(uris: List<Uri>) {
        try {
            // Create delete request
            val deleteRequest = MediaStore.createDeleteRequest(
                requireContext().contentResolver,
                uris,
            )

            deleteRequestLauncher.launch(
                IntentSenderRequest.Builder(deleteRequest.intentSender).build()
            )
        } catch (e: Exception) {
            Timber.e("Error requesting delete: $e")
        }
    }
}