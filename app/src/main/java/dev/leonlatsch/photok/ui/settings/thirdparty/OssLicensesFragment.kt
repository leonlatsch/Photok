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

package dev.leonlatsch.photok.ui.settings.thirdparty

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentOssLicensesBinding
import dev.leonlatsch.photok.ui.components.bindings.BindableFragment

class OssLicensesFragment :
    BindableFragment<FragmentOssLicensesBinding>(R.layout.fragment_oss_licenses) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ossToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        requireActivity().assets.open("licenseReleaseReport.json").let {
            val json = String(it.readBytes())
            val listType = object : TypeToken<ArrayList<OssEntry?>?>() {}.type
            val licenses: ArrayList<OssEntry> = Gson().fromJson(json, listType)

            val layoutManager = LinearLayoutManager(requireContext())
            binding.ossRecycler.layoutManager = layoutManager
            binding.ossRecycler.adapter = OssAdapter(licenses)
        }
    }
}