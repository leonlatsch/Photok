/*
 *   Copyright 2020-2024 Leon Latsch
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

package dev.leonlatsch.photok.settings.domain.models

import dev.leonlatsch.photok.R

enum class LockTimeout(
    override val value: String,
    override val label: Int,
) : SettingsEnum {
    Immediately("0", R.string.settings_security_timeout_immediately),
    OneMinute("60000", R.string.settings_security_timeout_one_minute),
    TwoMinute("120000", R.string.settings_security_timeout_two_minute),
    FiveMinute("300000", R.string.settings_security_timeout_five_minute),
    TenMinute("600000", R.string.settings_security_timeout_ten_minute),
    Never("-1", R.string.settings_security_timeout_never),
}