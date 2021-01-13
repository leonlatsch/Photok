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

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter for managing viewHolders for credit entries.
 *
 * @since 1.2.0
 * @author Leon Latsch
 */
class CreditsAdapter(
    private val creditEntries: List<CreditEntry>,
    private val onClick: (str: String?) -> Unit
) : RecyclerView.Adapter<CreditEntryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreditEntryViewHolder =
        CreditEntryViewHolder(parent, onClick)

    override fun onBindViewHolder(holder: CreditEntryViewHolder, position: Int) {
        holder.bindTo(creditEntries[position])
    }

    override fun getItemCount(): Int = creditEntries.size
}