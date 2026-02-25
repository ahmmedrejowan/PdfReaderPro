package com.rejowan.pdfreaderpro.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for recently opened PDF files.
 * Maps from the legacy recent_table in SQLite.
 */
@Entity(tableName = "recent")
data class RecentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val path: String,
    val size: Long,
    val lastOpened: Long,
    val totalPages: Int,
    val lastPage: Int
)
