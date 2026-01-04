package dev.leonlatsch.photok.imageviewer.ui

import android.os.Bundle
import android.view.View
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.DialogBottomSheetDetailsBinding
import dev.leonlatsch.photok.model.database.entity.Photo
import dev.leonlatsch.photok.uicomponnets.bindings.BindableBottomSheetDialogFragment

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