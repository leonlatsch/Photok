


package dev.leonlatsch.photok.model.database.entity

import dev.leonlatsch.photok.other.extensions.empty

/**
 * Enum for [Photo.type].
 * Internal value is an [Int].
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
enum class PhotoType(
    val value: Int,
    val mimeType: String)
{
    UNDEFINED(0, String.empty),
    PNG(1, "image/png"),
    JPEG(2, "image/jpeg"),
    GIF(3, "image/gif"),
    MP4(4, "video/mp4"),
    MPEG(5, "video/mpeg"),
    WEBM(6, "video/webm"),
    MOV(7, "video/quicktime");

    val isVideo: Boolean
        get() = when (value) {
            4, 5, 6, 7 -> true
            else -> false
        }

    val isGif: Boolean
        get() = value == 3

    companion object {
        /**
         * Create a [PhotoType] from its Int value.
         * Used in converters.
         */
        fun fromValue(value: Int) = values().first { it.value == value }

        fun fromMimeType(mimeType: String?): PhotoType = when (mimeType) {
            PNG.mimeType -> PNG
            JPEG.mimeType -> JPEG
            GIF.mimeType -> GIF
            MP4.mimeType -> MP4
            MPEG.mimeType -> MPEG
            WEBM.mimeType -> WEBM
            MOV.mimeType -> MOV
            else -> UNDEFINED
        }
    }

}

package dev.leonlatsch.photok.model.database.entity

import dev.leonlatsch.photok.other.extensions.empty

/**
 * Enum for [Photo.type].
 * Internal value is an [Int].
 *
 * @since 1.0.0
 * @author Leon Latsch
 */
enum class PhotoType(
    val value: Int,
    val mimeType: String)
{
    UNDEFINED(0, String.empty),
    PNG(1, "image/png"),
    JPEG(2, "image/jpeg"),
    GIF(3, "image/gif"),
    MP4(4, "video/mp4"),
    MPEG(5, "video/mpeg"),
    WEBM(6, "video/webm"),
    MOV(7, "video/quicktime");

    val isVideo: Boolean
        get() = when (value) {
            4, 5, 6, 7 -> true
            else -> false
        }

    val isGif: Boolean
        get() = value == 3

    companion object {
        /**
         * Create a [PhotoType] from its Int value.
         * Used in converters.
         */
        fun fromValue(value: Int) = values().first { it.value == value }

        fun fromMimeType(mimeType: String?): PhotoType = when (mimeType) {
            PNG.mimeType -> PNG
            JPEG.mimeType -> JPEG
            GIF.mimeType -> GIF
            MP4.mimeType -> MP4
            MPEG.mimeType -> MPEG
            WEBM.mimeType -> WEBM
            MOV.mimeType -> MOV
            else -> UNDEFINED
        }
    }

}