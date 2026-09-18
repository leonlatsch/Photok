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

package dev.leonlatsch.photok.settings.ui.credits

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val CONTRIBUTORS_FILE = "contributors.json"
private const val ICON_CREDITS_FILE = "icon_credits.html"

/**
 * ViewModel for the credits screen. Loads contributors and icon credits from the assets.
 *
 * @since 1.2.0
 * @author Leon Latsch
 */
@HiltViewModel
class CreditsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreditsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val state = withContext(Dispatchers.IO) {
                CreditsUiState(
                    contributors = loadContributors(),
                    iconCreditsHtml = loadIconCreditsHtml(),
                )
            }

            _uiState.value = state
        }
    }

    private fun loadContributors(): List<Contributor> {
        val json = context.assets.open(CONTRIBUTORS_FILE).use { String(it.readBytes()) }
        val listType = object : TypeToken<List<CreditEntry>>() {}.type
        val entries: List<CreditEntry> = gson.fromJson(json, listType)

        return entries.map { entry ->
            val website = normalizeSensitive(entry.website)

            Contributor(
                name = entry.name,
                contribution = entry.contribution,
                contact = normalizeSensitive(entry.contact),
                website = prettifyWebsite(website),
                websiteUrl = website,
            )
        }
    }

    private fun loadIconCreditsHtml() =
        context.assets.open(ICON_CREDITS_FILE).use { String(it.readBytes()) }

    private fun normalizeSensitive(str: String) = str
        .replace("[.]", ".")
        .replace("[at]", "@")

    private fun prettifyWebsite(website: String) = website
        .removePrefix("https://")
        .removePrefix("http://")
}
