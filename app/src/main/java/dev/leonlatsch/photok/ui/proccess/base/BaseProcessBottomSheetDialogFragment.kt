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

package dev.leonlatsch.photok.ui.proccess.base

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.BottomSheetDialogProcessBinding
import dev.leonlatsch.photok.ui.components.BindableBottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_dialog_process.*

abstract class BaseProcessBottomSheetDialogFragment(
    @StringRes private val processingLabelTextResource: Int
) : BindableBottomSheetDialogFragment<BottomSheetDialogProcessBinding>(
    R.layout.bottom_sheet_dialog_process
) {

    // region binding properties

    val labelText: MutableLiveData<String> = MutableLiveData()
    val closeButtonVisibility: MutableLiveData<Int> = MutableLiveData(View.GONE)
    val abortButtonVisibility: MutableLiveData<Int> = MutableLiveData(View.VISIBLE)
    val processIndicatorsVisibility: MutableLiveData<Int> = MutableLiveData(View.GONE)

    // endregion

    abstract val viewModel: BaseProcessViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.processState.postValue(ProcessState.INITIALIZE)

        viewModel.processState.observe(viewLifecycleOwner, {
            val label: String = when (it) {
                ProcessState.INITIALIZE -> {
                    isCancelable = false
                    setCompoundDrawable(null)
                    closeButtonVisibility.postValue(View.GONE)
                    processIndicatorsVisibility.postValue(View.GONE)
                    abortButtonVisibility.postValue(View.VISIBLE)
                    getString(R.string.process_initialize)
                }
                ProcessState.PROCESSING -> {
                    processIndicatorsVisibility.postValue(View.VISIBLE)
                    getString(processingLabelTextResource)
                }
                ProcessState.FINISHED -> {
                    isCancelable = true
                    setCompoundDrawable(R.drawable.check, android.R.color.holo_green_dark)
                    closeButtonVisibility.postValue(View.VISIBLE)
                    abortButtonVisibility.postValue(View.GONE)
                    getString(R.string.process_finished)
                }
                ProcessState.ABORTED -> {
                    isCancelable = true
                    setCompoundDrawable(R.drawable.close, android.R.color.holo_red_dark)
                    closeButtonVisibility.postValue(View.VISIBLE)
                    abortButtonVisibility.postValue(View.GONE)
                    getString(R.string.process_aborted)
                }
                else -> return@observe
            }
            labelText.postValue(label)
        })

        viewModel.process()
    }

    private fun setCompoundDrawable(drawable: Int?, color: Int = 0) {
        if (drawable == null) {
            processingLabelTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
            return
        }

        processingLabelTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, drawable, 0)
        processingLabelTextView.compoundDrawables[2]?.setTint(color)
    }

    override fun bind(binding: BottomSheetDialogProcessBinding) {
        super.bind(binding)
        binding.context = this
        binding.viewModel = viewModel
    }


}
