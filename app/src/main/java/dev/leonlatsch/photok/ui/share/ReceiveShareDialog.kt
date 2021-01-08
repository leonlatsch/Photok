/*
 *   Copyright 2020-2021 Leon Latsch
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

package dev.leonlatsch.photok.ui.share

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.DialogReceiveShareBinding
import dev.leonlatsch.photok.other.REQ_PERM_SHARED_IMPORT
import dev.leonlatsch.photok.ui.components.BindableDialogFragment
import dev.leonlatsch.photok.ui.process.ImportBottomSheetDialogFragment
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

/**
 * Dialog shown when sharing elements to photok. Asks for permission and launches import dialog
 *
 * @since 1.2.0
 * @author Leon Latsch
 */
class ReceiveShareDialog(
    private val sharedData: List<Uri>
) : BindableDialogFragment<DialogReceiveShareBinding>(R.layout.dialog_receive_share) {

    private val viewModel: ReceiveShareViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.elementsToProcess = sharedData.size
    }

    @AfterPermissionGranted(REQ_PERM_SHARED_IMPORT)
    fun startImport() {
        if (EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            ImportBottomSheetDialogFragment(sharedData).show(
                requireActivity().supportFragmentManager,
                ImportBottomSheetDialogFragment::class.qualifiedName
            )

            dismiss()
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.import_permission_rationale),
                REQ_PERM_SHARED_IMPORT,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    override fun bind(binding: DialogReceiveShareBinding) {
        super.bind(binding)
        binding.context = this
        binding.viewModel = viewModel
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward result to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}