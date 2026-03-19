/*
 *   Copyright 2020–2026 Leon Latsch
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dev.leonlatsch.photok.R
import dev.leonlatsch.photok.ui.theme.AppTheme

private const val LICENSE_REPORT_FILE = "open_source_licenses.html"

/**
 * Fragment for displaying open source licenses.
 *
 * @since 1.2.1
 * @author Leon Latsch
 */
class OssLicensesFragment : Fragment() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = stringResource(R.string.about_third_party)
                                    )
                                },
                                navigationIcon = {
                                    IconButton(
                                        onClick = {
                                            findNavController().navigateUp()
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_back),
                                            contentDescription = stringResource(R.string.process_close),
                                        )
                                    }
                                }
                            )
                        }
                    ) { contentPadding ->
                        AndroidView(
                            factory = { context ->
                                WebView(context).apply {
                                    loadUrl("file:///android_asset/$LICENSE_REPORT_FILE")
                                }
                            },
                            modifier = Modifier.padding(contentPadding)
                        )
                    }
                }
            }
        }
    }
}