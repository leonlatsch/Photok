package dev.leonlatsch.photok.model.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity describing a Password.
 * Always uses 0 for [id] to only have one instance at a time.
 *
 * @since 1.0.0
 */
@Entity(tableName = "password")
data class Password(
    val password: String,
    @PrimaryKey(autoGenerate = false) val id: Int = 0 // Set hard 0 to replace if a dupe happens
)