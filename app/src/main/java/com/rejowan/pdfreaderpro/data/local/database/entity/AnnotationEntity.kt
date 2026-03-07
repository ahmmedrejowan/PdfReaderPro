package com.rejowan.pdfreaderpro.data.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity for PDF annotations (highlights, notes, etc.)
 * New in v2 - allows users to add annotations to PDFs.
 */
@Entity(
    tableName = "annotations",
    indices = [
        Index(value = ["pdfPath"]),
        Index(value = ["pdfPath", "pageNumber"])
    ]
)
data class AnnotationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pdfPath: String,
    val pageNumber: Int,
    val type: String, // "highlight", "underline", "note"
    val content: String?, // For notes, the text content
    val color: Int?, // Highlight/underline color as ARGB int
    val startX: Float?, // Coordinates for highlight rectangle
    val startY: Float?,
    val endX: Float?,
    val endY: Float?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
