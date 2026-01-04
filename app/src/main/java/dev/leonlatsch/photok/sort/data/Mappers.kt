


package dev.leonlatsch.photok.sort.data

import dev.leonlatsch.photok.sort.data.db.model.SortTable
import dev.leonlatsch.photok.sort.domain.Sort

fun Sort.toData(albumUuid: String?): SortTable {
    return SortTable(
        id = 0,
        albumUuid = albumUuid,
        field = field,
        order = order,
    )
}

fun SortTable.toDomain(): Sort {
    return Sort(
        field = field,
        order = order,
    )
}

package dev.leonlatsch.photok.sort.data

import dev.leonlatsch.photok.sort.data.db.model.SortTable
import dev.leonlatsch.photok.sort.domain.Sort

fun Sort.toData(albumUuid: String?): SortTable {
    return SortTable(
        id = 0,
        albumUuid = albumUuid,
        field = field,
        order = order,
    )
}

fun SortTable.toDomain(): Sort {
    return Sort(
        field = field,
        order = order,
    )
}