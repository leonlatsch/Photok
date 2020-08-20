package dev.leonlatsch.photok.model.database.entity

enum class PhotoType(val value: Int) {
    UNDEFINED(0),
    PNG(1),
    JPEG(2),
    GIF(3);

    companion object {
        fun fromValue(value: Int) = values().first { it.value == value }
    }

}