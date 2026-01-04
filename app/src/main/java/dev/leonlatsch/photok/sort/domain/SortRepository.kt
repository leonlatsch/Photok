


package dev.leonlatsch.photok.sort.domain

import kotlinx.coroutines.flow.Flow

interface SortRepository {
    fun observeSortFor(albumUuid: String? = null, default: Sort): Flow<Sort>
    fun observeSortsForAlbums(): Flow<Map<String, Sort>>
    suspend fun updateSortFor(albumUuid: String? = null, sort: Sort)
    suspend fun getSortForAlbum(albumUuid: String? = null): Sort?
}

package dev.leonlatsch.photok.sort.domain

import kotlinx.coroutines.flow.Flow

interface SortRepository {
    fun observeSortFor(albumUuid: String? = null, default: Sort): Flow<Sort>
    fun observeSortsForAlbums(): Flow<Map<String, Sort>>
    suspend fun updateSortFor(albumUuid: String? = null, sort: Sort)
    suspend fun getSortForAlbum(albumUuid: String? = null): Sort?
}