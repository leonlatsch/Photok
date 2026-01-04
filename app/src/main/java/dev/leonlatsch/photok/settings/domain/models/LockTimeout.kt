


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