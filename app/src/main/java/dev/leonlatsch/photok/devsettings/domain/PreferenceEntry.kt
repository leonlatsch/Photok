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

package dev.leonlatsch.photok.devsettings.domain

sealed interface PreferenceEntry {
    val key: String

    data class StringEntry(override val key: String, val value: String) : PreferenceEntry

    data class IntEntry(override val key: String, val value: Int) : PreferenceEntry

    data class LongEntry(override val key: String, val value: Long) : PreferenceEntry

    data class FloatEntry(override val key: String, val value: Float) : PreferenceEntry

    data class BooleanEntry(override val key: String, val value: Boolean) : PreferenceEntry

    data class StringSetEntry(override val key: String, val value: Set<String>) : PreferenceEntry
}
