package com.rejowan.pdfreaderpro.domain.model

data class PdfFolder(
    val path: String,
    val name: String,
    val pdfCount: Int,
    val subFolders: List<PdfFolder> = emptyList()
) {
    val displayPath: String
        get() {
            val parts = path.removePrefix("/storage/emulated/0/").split("/")
            return parts.joinToString(" > ")
        }
}
