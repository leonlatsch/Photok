/*
 *   Copyright 2020 Leon Latsch
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

package dev.leonlatsch.photok.ui.viewphoto

import android.os.Bundle
import android.view.View
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.DialogBottomSheetDetailsBinding
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.ui.components.BindableBottomSheetDialogFragment

/**
 * Bottom Sheet Dialog for the photo details.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
class DetailsBottomSheetDialog(
    val photo: Photo?
) : BindableBottomSheetDialogFragment<DialogBottomSheetDetailsBinding>(R.layout.dialog_bottom_sheet_details) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photo ?: dismiss()
    }

    override fun bind(binding: DialogBottomSheetDetailsBinding) {
        super.bind(binding)
        binding.context = this
    }
}