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

package dev.leonlatsch.photok.ui.news

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import dev.leonlatsch.photok.BuildConfig
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.DialogNewsBinding
import dev.leonlatsch.photok.other.openUrl
import dev.leonlatsch.photok.ui.components.bindings.BindableDialogFragment

class NewsDialog : BindableDialogFragment<DialogNewsBinding>(R.layout.dialog_news) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titles = resources.getStringArray(R.array.newsTitles)
        val summaries = resources.getStringArray(R.array.newsSummaries)

        val fixLayoutManager = object : LinearLayoutManager(requireContext()) {
            override fun canScrollVertically() = false
        }

        binding.newsRecycler.layoutManager = fixLayoutManager
        binding.newsRecycler.adapter = NewsAdapter(titles, summaries)

        binding.newsVersion.text = BuildConfig.VERSION_NAME
    }

    fun openChangelog() {
        val url = getString(R.string.news_changelog_url, BuildConfig.VERSION_NAME)
        openUrl(requireContext(), url)
    }

    override fun bind(binding: DialogNewsBinding) {
        super.bind(binding)
        binding.context = this
    }
}