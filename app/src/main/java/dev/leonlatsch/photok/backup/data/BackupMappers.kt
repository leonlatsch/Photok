package dev.leonlatsch.photok.backup.data

import dev.leonlatsch.photok.gallery.albums.domain.model.Album
import dev.leonlatsch.photok.gallery.albums.domain.model.AlbumPhotoRef
import dev.leonlatsch.photok.model.database.entity.Photo

fun Photo.toBackup(): PhotoBackup =
    PhotoBackup(
        fileName = fileName,
        importedAt = importedAt,
        lastModified = lastModified,
        type = type,
        size = size,
        uuid = uuid,
    )

fun PhotoBackup.toDomain(): Photo =
    Photo(
        fileName = fileName,
        importedAt = importedAt,
        lastModified = lastModified,
        type = type,
        size = size,
        uuid = uuid,
    )

fun Album.toBackup(): AlbumBackup =
    AlbumBackup(
        uuid = uuid,
        modifiedAt = modifiedAt,
        name = name,
    )

fun AlbumBackup.toDomain(): Album =
    Album(
        uuid = uuid,
        name = name,
        modifiedAt = modifiedAt ?: System.currentTimeMillis(),
        files = emptyList(),
    )

fun AlbumPhotoRef.toBackup(): AlbumPhotoRefBackup =
    AlbumPhotoRefBackup(
        albumUUID = albumUUID,
        photoUUID = photoUUID,
        linkedAt = linkedAt,
    )

fun AlbumPhotoRefBackup.toDomain(): AlbumPhotoRef =
    AlbumPhotoRef(
        albumUUID = albumUUID,
        photoUUID = photoUUID,
        linkedAt = linkedAt,
    )