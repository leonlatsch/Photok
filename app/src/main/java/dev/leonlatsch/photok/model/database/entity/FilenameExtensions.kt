package dev.leonlatsch.photok.model.database.entity

const val PHOTOK_FILE_EXTENSION = "crypt"
const val LEGACY_PHOTOK_FILE_EXTENSION = "photok"

/**
 * Get FileName for internal files and backup files.
 * Sample: 923ae2b7-f056-453d-a3dc-264a08e58a07.crypt
 */
fun internalFileName(uuid: String) = "${uuid}.$PHOTOK_FILE_EXTENSION"

/**
 * Get FileName for internal thumbnails.
 * Sample: 923ae2b7-f056-453d-a3dc-264a08e58a07.crypt.tn
 */
fun internalThumbnailFileName(uuid: String) = "${uuid}.$PHOTOK_FILE_EXTENSION.tn"

/**
 * Get FileName for video previews.
 * Sample: 923ae2b7-f056-453d-a3dc-264a08e58a07.crypt.vp
 */
fun internalVideoPreviewFileName(uuid: String) = "${uuid}.$PHOTOK_FILE_EXTENSION.vp"