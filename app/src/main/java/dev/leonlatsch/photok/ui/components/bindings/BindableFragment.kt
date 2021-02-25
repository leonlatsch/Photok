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

package dev.leonlatsch.photok.ui.components.bindings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import dev.leonlatsch.photok.ui.components.base.BaseFragment

/**
 * Base for all fragments that use data binding.
 *
 * @param BindingType the binding type generated when adding <data> tag to a layout.
 * @param layout the layout id with the data binding.
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
abstract class BindableFragment<BindingType : ViewDataBinding>(
    @LayoutRes private val layout: Int
) : BaseFragment(), Bindable<BindingType> {

    final override lateinit var binding: BindingType

    /**
     * Creates layout and binding.
     * **ALWAYS** call super.onCreateView() when overwriting.
     */
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
     * Inserts the Bindings. Always call super.insertBindings() to set lifecycle owner.
     */
    override fun bind(binding: BindingType) {
        binding.lifecycleOwner = this
    }
}