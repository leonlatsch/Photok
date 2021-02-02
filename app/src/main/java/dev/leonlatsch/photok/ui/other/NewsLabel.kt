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

package dev.leonlatsch.photok.ui.other

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.LayoutNewsLabelBinding
import dev.leonlatsch.photok.ui.components.bindings.Bindable

class NewsLabel @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle), Bindable<LayoutNewsLabelBinding> {

    override lateinit var binding: LayoutNewsLabelBinding

    init {
        if (isInEditMode) {
            LayoutInflater.from(context).inflate(R.layout.layout_news_label, this, true)
        } else {
            binding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.layout_news_label,
                this,
                true
            )
            bind(binding)
            binding.root
        }

        attrs?.let {
            val styledAttrs = context.obtainStyledAttributes(it, R.styleable.NewsLabel, 0, 0)
            val title =
                resources.getText(
                    styledAttrs.getResourceId(
                        R.styleable.NewsLabel_news_label_title,
                        0 // Add default
                    )
                )
            val summary =
                resources.getText(
                    styledAttrs.getResourceId(
                        R.styleable.NewsLabel_news_label_summary,
                        0 // Add default
                    )
                )

            binding.newsLabelTitle.text = title
            binding.newsLabelSummary.text = summary

            styledAttrs.recycle()
        }
    }

    override fun bind(binding: LayoutNewsLabelBinding) {
        binding.context = this
    }

}