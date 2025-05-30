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

package dev.leonlatsch.photok.settings.ui.thirdparty

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.databinding.FragmentOssLicensesBinding
import dev.leonlatsch.photok.other.systemBarsPadding
import dev.leonlatsch.photok.uicomponnets.bindings.BindableFragment

private const val LICENSE_REPORT_FILE = "open_source_licenses.html"

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
        binding.root.systemBarsPadding()

        binding.ossToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.licenseWebView.loadUrl("file:///android_asset/$LICENSE_REPORT_FILE")
    }
}