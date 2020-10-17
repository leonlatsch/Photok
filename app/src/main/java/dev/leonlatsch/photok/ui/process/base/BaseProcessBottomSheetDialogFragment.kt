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

package dev.leonlatsch.photok.ui.process.base

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.DialogBottomSheetProcessBinding
import dev.leonlatsch.photok.ui.components.BindableBottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_bottom_sheet_process.*

/**
 * Abstract base for all process dialogs.
 * Handles state, messages and layout.
 * Holds an [BaseProcessViewModel] which also needs to be overridden.
 * [viewModel] is abstract and needs to be set in the child.
 *
 * @param processingLabelTextResource The string resource to be displayed while processing.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
abstract class BaseProcessBottomSheetDialogFragment(
    @StringRes private val processingLabelTextResource: Int
) : BindableBottomSheetDialogFragment<DialogBottomSheetProcessBinding>(
    R.layout.dialog_bottom_sheet_process
) {

    // region binding properties

    val labelText: MutableLiveData<String> = MutableLiveData()
    val closeButtonVisibility: MutableLiveData<Int> = MutableLiveData(View.GONE)
    val abortButtonVisibility: MutableLiveData<Int> = MutableLiveData(View.VISIBLE)
    val processIndicatorsVisibility: MutableLiveData<Int> = MutableLiveData(View.GONE)
    val statusDrawable: MutableLiveData<Drawable> = MutableLiveData()
    val failuresWarnMessageVisibility: MutableLiveData<Int> = MutableLiveData(View.INVISIBLE)

    // endregion

    /**
     * Abstract [BaseProcessViewModel].
     * Needs to be set in the child, handles processing.
     */
    abstract val viewModel: BaseProcessViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.processState.postValue(ProcessState.INITIALIZE)

        viewModel.processState.observe(viewLifecycleOwner, {
            val label: String = when (it) {
                ProcessState.INITIALIZE -> {
                    isCancelable = false
                    setStatusIcon(null)
                    closeButtonVisibility.postValue(View.GONE)
                    abortButtonVisibility.postValue(View.VISIBLE)
                    processIndicatorsVisibility.postValue(View.GONE)
                    failuresWarnMessageVisibility.postValue(View.INVISIBLE)
                    getString(R.string.process_initialize)
                }
                ProcessState.PROCESSING -> {
                    processIndicatorsVisibility.postValue(View.VISIBLE)
                    getString(processingLabelTextResource)
                }
                ProcessState.FINISHED -> {
                    enterFinishedOrAbortedState()
                    setStatusIcon(R.drawable.ic_check, android.R.color.holo_green_dark)
                    getString(R.string.process_finished)
                }
                ProcessState.ABORTED -> {
                    enterFinishedOrAbortedState()
                    setStatusIcon(R.drawable.ic_close, android.R.color.holo_red_dark)
                    getString(R.string.process_aborted)
                }
                else -> return@observe
            }
            labelText.postValue(label)
        })

        prepareViewModel()
        viewModel.runProcessing()
    }

    private fun enterFinishedOrAbortedState() {
        isCancelable = true
        closeButtonVisibility.postValue(View.VISIBLE)
        abortButtonVisibility.postValue(View.GONE)
        if (viewModel.failuresOccurred) {
            failuresWarnMessageVisibility.postValue(View.VISIBLE)
        }
    }

    /**
     * Called before viewModel starts processing.
     * Assign data and variables in implementations.
     */
    open fun prepareViewModel() {
    }

    private fun setStatusIcon(drawable: Int?, color: Int = 0) {
        if (drawable == null) {
            statusDrawable.postValue(null)
            return
        }

        statusDrawable.postValue(ContextCompat.getDrawable(requireContext(), drawable))
        statusImageView.setColorFilter(ContextCompat.getColor(requireContext(), color))
    }

    override fun bind(binding: DialogBottomSheetProcessBinding) {
        super.bind(binding)
        binding.context = this
        binding.viewModel = viewModel
    }
}
