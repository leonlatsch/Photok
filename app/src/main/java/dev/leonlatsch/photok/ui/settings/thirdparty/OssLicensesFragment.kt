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
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentOssLicensesBinding
import dev.leonlatsch.photok.ui.components.bindings.BindableFragment

/**
 * Fragment for displaying open source licenses.
 *
 * @since 1.2.1
 * @author Leon Latsch
 */
class OssLicensesFragment :
    BindableFragment<FragmentOssLicensesBinding>(R.layout.fragment_oss_licenses) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ossToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.ossRecycler.initialize(LICENSE_REPORT_FILE)
    }

    companion object {
        const val LICENSE_REPORT_FILE = "licenseReleaseReport.json"
    }
}