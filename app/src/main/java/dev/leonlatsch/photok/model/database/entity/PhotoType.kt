package dev.leonlatsch.photok.model.database.entity

/**
 * Enum for [Photo.type].
 * Internal value is an [Int].
 *
 * @since 1.0.0
 */
enum class PhotoType(val value: Int) {
    UNDEFINED(0),
    PNG(1),
    JPEG(2),
    GIF(3);

    companion object {
        fun fromValue(value: Int) = values().first { it.value == value }
    }

}