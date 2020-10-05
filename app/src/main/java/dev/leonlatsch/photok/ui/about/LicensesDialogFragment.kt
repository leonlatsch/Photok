/*
 *   Copyright 2020 Leon Latsch
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

package dev.leonlatsch.photok.ui.about

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.webkit.WebView
import androidx.fragment.app.DialogFragment
import dev.leonlatsch.photok.R

/**
 * Dialog for showing open_source_licenses.html
 *
 * @author Leon Latsch
 * @since 1.0.0
 */
class LicensesDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val webView = WebView(requireContext())
        webView.loadUrl(LICENSE_REPORT_URL)

        return AlertDialog.Builder(requireActivity())
            .setTitle(getString(R.string.about_thrid_party))
            .setView(webView)
            .setNeutralButton(getString(R.string.common_ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    companion object {
        const val LICENSE_REPORT_URL = "file:///android_asset/open_source_licenses.html"
    }
}