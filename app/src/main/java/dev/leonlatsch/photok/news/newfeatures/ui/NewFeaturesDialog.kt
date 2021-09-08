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

import android.os.Bundle
import android.view.View
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.DialogNewsBinding
import dev.leonlatsch.photok.news.newfeatures.ui.model.NewFeatureViewData
import dev.leonlatsch.photok.other.openUrl
import dev.leonlatsch.photok.uicomponnets.FixLinearLayoutManager
import dev.leonlatsch.photok.uicomponnets.bindings.BindableDialogFragment

/**
 * Dialog for displaying new features.
 *
 * @since 1.3.0
 * @author Leon Latsch
 */
class NewFeaturesDialog : BindableDialogFragment<DialogNewsBinding>(R.layout.dialog_news) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.newsRecycler.layoutManager = FixLinearLayoutManager(requireContext())
        binding.newsRecycler.adapter = NewFeaturesAdapter(getNewFeaturesViewData())

        binding.newsVersion.text = BuildConfig.VERSION_NAME
    }

    /**
     * Open the github release with the current version name.
     */
    fun openChangelog() {
        val url = getString(R.string.news_changelog_url)
        openUrl(requireContext(), url)
    }

    private fun getNewFeaturesViewData(): List<NewFeatureViewData> {
        val titles = resources.getStringArray(R.array.newsTitles)
        val summaries = resources.getStringArray(R.array.newsSummaries)

        return if (titles.size == summaries.size) {
            val viewDataList = mutableListOf<NewFeatureViewData>()
            for (i in 0..titles.lastIndex) {
                viewDataList.add(NewFeatureViewData(titles[i], summaries[i]))
            }
            viewDataList
        } else {
            listOf()
        }
    }

    override fun bind(binding: DialogNewsBinding) {
        super.bind(binding)
        binding.context = this
    }
}