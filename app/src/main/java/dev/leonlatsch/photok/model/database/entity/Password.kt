package dev.leonlatsch.photok.model.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "password")
data class Password(
    val password: String,
    @PrimaryKey(autoGenerate = false) val id: Int = 0 // Set hard 0 to replace if a dupe happens
)