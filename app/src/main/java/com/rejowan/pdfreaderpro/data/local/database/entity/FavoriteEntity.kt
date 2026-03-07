package com.rejowan.pdfreaderpro.data.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity for favorite PDF files.
 * Maps from the legacy favorite table in SQLite.
 */
@Entity(
    tableName = "favorites",
    indices = [
        Index(value = ["path"], unique = true),
        Index(value = ["name"])
    ]
)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val path: String,
    val size: Long,
    val dateModified: Long,
    val parentFolder: String
)
