package com.androvine.pdfreaderpro.dataClasses

data class PdfFile(
    val name: String,
    val path: String,
    val size: Long,
    val dateModified: Long,
    val parentFolderName: String
)
