


package dev.leonlatsch.photok.settings.domain.models

import dev.leonlatsch.photok.R

enum class StartPage(override val value: String, override val label: Int) : SettingsEnum {
    AllFiles("all_files", R.string.gallery_all_photos_label),
    Albums("albums", R.string.gallery_albums_label);

    companion object {
        fun fromValue(value: String?): StartPage {
            return entries.find { it.value == value } ?: AllFiles
        }
    }
}

package dev.leonlatsch.photok.settings.domain.models

import dev.leonlatsch.photok.R

enum class StartPage(override val value: String, override val label: Int) : SettingsEnum {
    AllFiles("all_files", R.string.gallery_all_photos_label),
    Albums("albums", R.string.gallery_albums_label);

    companion object {
        fun fromValue(value: String?): StartPage {
            return entries.find { it.value == value } ?: AllFiles
        }
    }
}