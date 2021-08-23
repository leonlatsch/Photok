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

package dev.leonlatsch.photok.gallery.ui.collections

import androidx.recyclerview.widget.RecyclerView
import dev.leonlatsch.photok.databinding.ItemCollectionBinding
import dev.leonlatsch.photok.model.database.entity.Collection

class CollectionItemViewHolder(
    private val binding: ItemCollectionBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bindTo(collection: Collection?) {
        collection ?: return

        binding.collection = collection
    }
}