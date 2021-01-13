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

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentCreditsBinding
import dev.leonlatsch.photok.ui.components.BindableFragment

/**
 * Fragment for displaying credits for icons and contributors.
 *
 * @since 1.2.0
 * @author Leon Latsch
 */
class CreditsFragment : BindableFragment<FragmentCreditsBinding>(R.layout.fragment_credits) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.creditsToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        requireActivity().assets.open(CONTRIBUTORS_FILE).let {
            val json = String(it.readBytes())
            val listType = object : TypeToken<ArrayList<CreditEntry?>?>() {}.type
            val entries: ArrayList<CreditEntry> = Gson().fromJson(json, listType)

            val layoutManager = LinearLayoutManager(requireContext())
            binding.creditsRecycler.layoutManager = layoutManager
            binding.creditsRecycler.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    layoutManager.orientation
                )
            )
            binding.creditsRecycler.adapter = CreditsAdapter(entries, onClick)
        }
    }

    private val onClick: (str: String?) -> Unit = {
        if (it != null) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(it)
            startActivity(intent)
        }
    }

    companion object {
        const val CONTRIBUTORS_FILE = "contributors.json"
    }
}