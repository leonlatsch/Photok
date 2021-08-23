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

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.DialogCollectionAddBinding
import dev.leonlatsch.photok.uicomponnets.bindings.BindableBottomSheetDialogFragment
import javax.inject.Inject

/**
 * Dialog for adding a new collection.
 *
 * @since 2.0.0
 * @author Leon Latsch
 */
@AndroidEntryPoint
class AddCollectionBottomSheetDialogFragment : BindableBottomSheetDialogFragment<DialogCollectionAddBinding>(R.layout.dialog_collection_add) {

    private val viewModel: AddCollectionViewModel by viewModels()

    @Inject
    lateinit var navigator: AddCollectionNavigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigationEvent.observe(viewLifecycleOwner) {
            navigator.navigate(this, it)
        }
    }

    override fun bind(binding: DialogCollectionAddBinding) {
        super.bind(binding)

        binding.viewModel = viewModel
    }
}