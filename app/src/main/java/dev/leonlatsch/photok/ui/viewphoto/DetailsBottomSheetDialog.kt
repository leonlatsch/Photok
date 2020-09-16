package dev.leonlatsch.photok.ui.viewphoto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.DialogDetailsBottomSheetBinding
import dev.leonlatsch.photok.model.database.entity.Photo

class DetailsBottomSheetDialog(
    val photo: Photo?,
    val photoSize: Int
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: DialogDetailsBottomSheetBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.dialog_details_bottom_sheet,
            container,
            false
        )
        binding.lifecycleOwner = this
        binding.context = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photo ?: dismiss()
    }
}