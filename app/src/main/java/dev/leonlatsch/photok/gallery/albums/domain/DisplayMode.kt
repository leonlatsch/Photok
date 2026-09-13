package dev.leonlatsch.photok.gallery.albums.domain

enum class DisplayMode(val configValue: String) {
    Grid("grid"),
    List("list");

    companion object {
        fun fromConfigValue(configValue: String?) = when (configValue) {
            List.configValue -> List
            else -> Grid
        }
    }
}