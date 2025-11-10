/*
 *   Copyright 2020-2024 Leon Latsch
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

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