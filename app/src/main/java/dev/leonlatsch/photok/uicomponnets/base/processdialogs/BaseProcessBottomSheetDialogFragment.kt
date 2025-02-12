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

package dev.leonlatsch.photok.uicomponnets.base.processdialogs

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import dev.leonlatsch.photok.BR
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.DialogBottomSheetProcessBinding
import dev.leonlatsch.photok.other.extensions.hide
import dev.leonlatsch.photok.other.extensions.show
import dev.leonlatsch.photok.other.extensions.vanish
import dev.leonlatsch.photok.uicomponnets.bindings.BindableBottomSheetDialogFragment

/**
 * Abstract base for all process dialogs.
 * Handles state, messages and layout.
 * Holds an [BaseProcessViewModel] which also needs to be overridden.
 * [viewModel] is abstract and needs to be set in the child.
 *
 * @param processingLabelTextResource The string resource to be displayed while processing.
 * @param T Type of elements to be processed
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
abstract class BaseProcessBottomSheetDialogFragment<T>(
    private val itemSource: List<T>?,
    @StringRes private val processingLabelTextResource: Int,
    val canAbort: Boolean
) : BindableBottomSheetDialogFragment<DialogBottomSheetProcessBinding>(
    R.layout.dialog_bottom_sheet_process
) {

    /**
     * Abstract [BaseProcessViewModel].
     * Needs to be set in the child, handles processing.
     */
    abstract val viewModel: BaseProcessViewModel<T>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.processState = ProcessState.INITIALIZE

        viewModel.addOnPropertyChange<ProcessState>(BR.processState) {
            val label: String = when (it) {
                ProcessState.INITIALIZE -> {
                    isCancelable = false
                    setStatusIcon(null)
                    binding.processCloseButton.hide()
                    binding.processAbortButton.show()
                    binding.processItemsProgressIndicatorLayout.hide()
                    binding.processPercentLayout.hide()
                    binding.processFailuresWarnMessage.vanish()
                    getString(R.string.process_initialize)
                }
                ProcessState.PROCESSING -> {
                    isCancelable = false
                    binding.processItemsProgressIndicatorLayout.show()
                    binding.processPercentLayout.show()
                    binding.processProcessingIndicator.show()
                    getString(processingLabelTextResource)
                }
                ProcessState.FINISHED -> {
                    enterFinishedOrAbortedState()
                    setStatusIcon(R.drawable.ic_check, android.R.color.holo_green_dark)
                    // auto dismiss
                    dismiss()
                    getString(R.string.process_finished)
                }
                ProcessState.ABORTED -> {
                    enterFinishedOrAbortedState()
                    setStatusIcon(R.drawable.ic_close, android.R.color.holo_red_dark)
                    getString(R.string.process_aborted)
                }
            }
            binding.processLabel.text = label
        }

        prepareViewModel(itemSource)
        viewModel.runProcessing()
    }

    private fun enterFinishedOrAbortedState() {
        isCancelable = true
        binding.processCloseButton.show()
        binding.processAbortButton.hide()
        binding.processProcessingIndicator.hide()
        if (viewModel.failuresOccurred) {
            binding.processFailuresWarnMessage.show()
        }
    }

    /**
     * Called before viewModel starts processing.
     * Assign data and variables in implementations.
     */
    open fun prepareViewModel(items: List<T>?) {
        if (items != null) {
            viewModel.items = items
            viewModel.elementsToProcess = items.size
        }
    }

    private fun setStatusIcon(drawable: Int?, color: Int = 0) {
        if (drawable == null) {
            binding.processStatusImageView.setImageDrawable(null)
            return
        }

        binding.processStatusImageView.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                drawable
            )
        )
        binding.processStatusImageView.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                color
            )
        )
    }

    override fun bind(binding: DialogBottomSheetProcessBinding) {
        super.bind(binding)
        binding.context = this
        binding.viewModel = viewModel
    }
}
