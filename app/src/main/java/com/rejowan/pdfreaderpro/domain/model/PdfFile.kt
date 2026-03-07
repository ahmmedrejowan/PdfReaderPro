package com.rejowan.pdfreaderpro.domain.model

import android.net.Uri

data class PdfFile(
    val id: Long,
    val name: String,
    val path: String,
    val uri: Uri,
    val size: Long,
    val dateModified: Long,
    val dateAdded: Long,
    val parentFolder: String,
    val pageCount: Int = 0,
    val isFavorite: Boolean = false
) {
    val displayName: String
        get() = name.removeSuffix(".pdf").removeSuffix(".PDF")

    val folderName: String
        get() = parentFolder.substringAfterLast("/").ifEmpty { "Storage" }
}
