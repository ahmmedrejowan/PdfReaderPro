package com.rejowan.pdfreaderpro.presentation.screens.reader.components

/**
 * Represents an embedded attachment in a PDF document.
 */
data class AttachmentItem(
    val title: String,
    val id: String,
    val dest: String? = null // The download destination/link
)
