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

package dev.leonlatsch.photok.ui.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Base for all BottomSheetDialogs that use data binding.
 *
 * @param BindingType The type of the generated binding
 * @param layout The layout resource id
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
abstract class BindableBottomSheetDialogFragment<BindingType : ViewDataBinding>(
    @LayoutRes private val layout: Int
) : BottomSheetDialogFragment(), Bindable<BindingType> {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: BindingType = DataBindingUtil.inflate(inflater, layout, container, false)
        bind(binding)
        return binding.root
    }

    override fun bind(binding: BindingType) {
        binding.lifecycleOwner = this
    }
}