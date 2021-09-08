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

package dev.leonlatsch.photok.news.newfeatures.ui

import androidx.recyclerview.widget.RecyclerView
import dev.leonlatsch.photok.databinding.ItemNewsBinding
import dev.leonlatsch.photok.news.newfeatures.ui.model.NewFeatureViewData

/**
 * ViewHolder for displaying news. Used by [NewFeaturesAdapter].
 *
 * @since 1.3.0
 * @author Leon Latsch
 */
class NewsViewHolder(
    private val binding: ItemNewsBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bindTo(viewData: NewFeatureViewData) {
        binding.itemNewsTitle.text = viewData.title
        binding.itemNewsSummary.text = viewData.summary
    }
}