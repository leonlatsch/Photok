package dev.leonlatsch.photok.settings.domain.models

import androidx.annotation.StringRes
import dev.leonlatsch.photok.R

enum class SystemDesignEnum(override val value: String, @param:StringRes override val label: Int) : SettingsEnum {
    System("system", R.string.settings_app_design_system_default),
    Dark("dark", R.string.settings_app_design_system_dark),
    Light("light", R.string.settings_app_design_system_light);

    companion object {
        fun fromValue(value: String?): SystemDesignEnum {
            return SystemDesignEnum.entries.find { it.value == value } ?: System
        }
    }
}