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
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

/**
 * Abstract class for Activities with binding context.
 * Inflates layout file and sets binding of type [BindingType].
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
abstract class BindableActivity<BindingType : ViewDataBinding>(
    @LayoutRes private val layout: Int
) : BaseActivity(), Bindable<BindingType> {

    final override lateinit var binding: BindingType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, layout)
        bind(binding)
    }

    override fun bind(binding: BindingType) {
        binding.lifecycleOwner = this
    }
}