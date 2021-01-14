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

package dev.leonlatsch.photok.ui.settings.credits

import android.content.Context
import android.text.Spanned
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.other.empty

/**
 * ViewHolder for showing a [CreditEntry] in a Recycler View.
 *
 * @since 1.2.0
 * @author Leon Latsch
 */
class CreditEntryViewHolder(
    view: View,
    private val onClick: (str: String?) -> Unit,
    private val context: Context
) : RecyclerView.ViewHolder(view) {

    private val nameTextView: TextView? = itemView.findViewById(R.id.creditName)
    private val contributionTextView: TextView? = itemView.findViewById(R.id.creditContribution)
    private val contactTextView: TextView? = itemView.findViewById(R.id.creditContact)
    private val websiteTextView: TextView? = itemView.findViewById(R.id.creditWebsite)

    private val headerHtmlTextView: TextView? = itemView.findViewById(R.id.creditIconHtmlTextView)

    private lateinit var rawWebsite: String

    /**
     * Bind [creditEntry] to the ui.
     */
    fun bindTo(creditEntry: CreditEntry) {
        if (creditEntry.isFooter) {
            headerHtmlTextView?.text = loadIconCredits()
        } else if (!creditEntry.isHeader) {
            rawWebsite = creditEntry.website!!

            itemView.setOnClickListener {
                onClick(normalizeSensitive(rawWebsite))
            }

            nameTextView?.text = creditEntry.name
            contributionTextView?.text = creditEntry.contribution
            contactTextView?.text = normalizeSensitive(creditEntry.contact!!)
            websiteTextView?.text = prettifyWebsite(normalizeSensitive(creditEntry.website))
        }
    }

    private fun loadIconCredits(): Spanned {
        val bytes = context.assets.open(ICON_CREDITS_FILE).readBytes()
        val rawText = String(bytes)
        return HtmlCompat.fromHtml(rawText, HtmlCompat.FROM_HTML_MODE_COMPACT)
    }

    private fun normalizeSensitive(str: String) = str
        .replace("[.]", ".")
        .replace("[at]", "@")

    private fun prettifyWebsite(website: String) = when {
        website.contains("http://") -> website.replace("http://", String.empty)
        website.contains("https://") -> website.replace("https://", String.empty)
        else -> website
    }

    companion object {
        private const val ICON_CREDITS_FILE = "icon_credits.html"
    }
}