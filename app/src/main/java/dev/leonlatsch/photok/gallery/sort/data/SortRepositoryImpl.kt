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

package dev.leonlatsch.photok.gallery.sort.data

import androidx.room.withTransaction
import dev.leonlatsch.photok.gallery.albums.domain.model.Album
import dev.leonlatsch.photok.gallery.sort.data.db.SortDao
import dev.leonlatsch.photok.gallery.sort.domain.Sort
import dev.leonlatsch.photok.gallery.sort.domain.SortRepository
import dev.leonlatsch.photok.model.database.PhotokDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SortRepositoryImpl @Inject constructor(
    private val sortDao: SortDao,
    private val database: PhotokDatabase,
) : SortRepository {

    override fun observeSortFor(albumUuid: String?): Flow<Sort> {
        return sortDao.observeSort(album = albumUuid).map { it?.toDomain() ?: Sort.Default }
    }

    override suspend fun updateSortFor(
        albumUuid: String?,
        sort: Sort
    ) {
        database.withTransaction {
            sortDao.deleteSortFor(albumUuid)

            if (sort.isModified()) {
                sortDao.updateSortFor(sort.toData(albumUuid))
            }
        }
    }
}