package dev.leonlatsch.photok.sort.data

import androidx.room.withTransaction
import dev.leonlatsch.photok.model.database.PhotokDatabase
import dev.leonlatsch.photok.sort.data.db.SortDao
import dev.leonlatsch.photok.sort.domain.Sort
import dev.leonlatsch.photok.sort.domain.SortRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SortRepositoryImpl @Inject constructor(
    private val sortDao: SortDao,
    private val database: PhotokDatabase,
) : SortRepository {

    override fun observeSortFor(albumUuid: String?, default: Sort): Flow<Sort> {
        return sortDao.observeSort(album = albumUuid).map { it?.toDomain() ?: default }
    }

    override suspend fun getSortForAlbum(albumUuid: String?): Sort? = withContext(IO) {
        sortDao.getSortForAlbum(albumUuid)?.toDomain()
    }

    override fun observeSortsForAlbums(): Flow<Map<String, Sort>> {
        return sortDao.observeSortsForAlbums().map { sorts ->
            buildMap {
                for (sort in sorts) {
                    sort.albumUuid ?: continue
                    put(sort.albumUuid, sort.toDomain())
                }
            }
        }
    }

    override suspend fun updateSortFor(
        albumUuid: String?,
        sort: Sort
    ) = withContext(IO) {
        database.withTransaction {
            sortDao.deleteSortFor(albumUuid)
            sortDao.updateSortFor(sort.toData(albumUuid))
        }
    }

}