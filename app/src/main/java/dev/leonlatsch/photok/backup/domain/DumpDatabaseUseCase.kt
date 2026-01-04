package dev.leonlatsch.photok.backup.domain

import dev.leonlatsch.photok.backup.data.BackupMetaData
import dev.leonlatsch.photok.backup.data.toBackup
import dev.leonlatsch.photok.gallery.albums.domain.AlbumRepository
import dev.leonlatsch.photok.model.repositories.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Creates a [BackupMetaData] from the current database
 */
class DumpDatabaseUseCase @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val albumRepository: AlbumRepository,
) {
    suspend operator fun invoke(password: String, version: Int): BackupMetaData = withContext(Dispatchers.IO) {
        val photos = photoRepository.findAllPhotosByImportDateDesc().map { it.toBackup() }
        val albums = albumRepository.getAlbums().map { it.toBackup() }
        val albumPhotoLinks = albumRepository.getAllAlbumPhotoLinks().map { it.toBackup() }

        BackupMetaData(
            password = password,
            photos = photos,
            albums = albums,
            albumPhotoRefs = albumPhotoLinks,
            backupVersion = version,
        )
    }
}