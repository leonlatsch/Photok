package dev.leonlatsch.photok.sort.domain

enum class SortConfig(
    val fields: List<Sort.Field>,
    val default: Sort,
) {
    Gallery(
        fields = listOf(Sort.Field.ImportDate, Sort.Field.LastModified, Sort.Field.FileName, Sort.Field.Size),
        default = Sort(field = Sort.Field.ImportDate, Sort.Order.Desc),
    ),
    Album(
        fields = listOf(Sort.Field.LinkedAt, Sort.Field.LastModified, Sort.Field.FileName, Sort.Field.Size),
        default = Sort(field = Sort.Field.LinkedAt, Sort.Order.Desc),
    ),
}