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

package dev.leonlatsch.photok.uicomponnets.bindings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import dev.leonlatsch.photok.uicomponnets.base.BaseDialogFragment

/**
 * Base for all Dialogs that use Bindings.
 *
 * @param BindingType The type of the generated binding
 * @param layout The layout resource id
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
abstract class BindableDialogFragment<BindingType : ViewDataBinding>(
    @LayoutRes private val layout: Int
) : BaseDialogFragment(), Bindable<BindingType> {

    final override lateinit var binding: BindingType

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layout, container, false)
        bind(binding)
        return binding.root
    }

    /**
     * When called, this fragment will call setup() on its viewModel
     */
    fun useViewModel(viewModel: ObservableViewModel) {
        viewModel.setup()
    }

    override fun bind(binding: BindingType) {
        binding.lifecycleOwner = this
    }
}