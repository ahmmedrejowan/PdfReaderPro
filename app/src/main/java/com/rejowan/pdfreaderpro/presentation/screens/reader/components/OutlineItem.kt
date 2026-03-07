package com.rejowan.pdfreaderpro.presentation.screens.reader.components

/**
 * Represents a table of contents item from PDF outline.
 */
data class OutlineItem(
    val title: String,
    val page: Int,
    val level: Int = 0,
    val id: String = "",
    val dest: String? = null
)
