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

/**
 * Representing data for an entry in the contributors page.
 *
 * @since 1.2.0
 * @author Leon Latsch
 */
data class CreditEntry(
    val contribution: String,
    val name: String? = null,
    val contact: String? = null,
    val website: String? = null
) {
    val isHeader: Boolean
        get() = contribution == LAYOUT_HEADER

    val isFooter: Boolean
        get() = contribution == LAYOUT_FOOTER

    companion object {
        private const val LAYOUT_HEADER = "layout_header"
        private const val LAYOUT_FOOTER = "layout_footer"

        /**
         * Creates an entry wich will be interpreted as a header.
         */
        fun createHeader() = CreditEntry(LAYOUT_HEADER)

        /**
         * Creates an entry wich will be interpreted as a footer.
         */
        fun createFooter() = CreditEntry(LAYOUT_FOOTER)
    }
}
