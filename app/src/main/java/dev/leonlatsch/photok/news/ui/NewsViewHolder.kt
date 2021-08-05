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

package dev.leonlatsch.photok.news.ui

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.leonlatsch.photok.R

/**
 * ViewHolder for displaying news. Used by [NewsAdapter].
 *
 * @since 1.3.0
 * @author Leon Latsch
 */
class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val titleTextView = itemView.findViewById<TextView>(R.id.itemNewsTitle)
    private val summaryTextView = itemView.findViewById<TextView>(R.id.itemNewsSummary)

    /**
     * Bind [title] and [summary] to the view.
     */
    fun bindTo(title: String, summary: String) {
        titleTextView.text = title
        summaryTextView.text = summary
    }
}