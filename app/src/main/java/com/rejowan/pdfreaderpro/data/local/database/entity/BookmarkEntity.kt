package com.rejowan.pdfreaderpro.data.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity for user bookmarks within PDF files.
 * New in v2 - allows users to save specific pages.
 */
@Entity(
    tableName = "bookmarks",
    indices = [
        Index(value = ["pdfPath"]),
        Index(value = ["pdfPath", "pageNumber"], unique = true)
    ]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pdfPath: String,
    val pageNumber: Int,
    val title: String?,
    val createdAt: Long = System.currentTimeMillis()
)
