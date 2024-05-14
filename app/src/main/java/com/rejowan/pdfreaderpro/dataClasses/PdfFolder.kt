package com.rejowan.pdfreaderpro.dataClasses

data class PdfFolder(
    val name: String,
    val pdfFiles: List<PdfFile>
) {
    companion object {
        val SortByName = Comparator<PdfFolder> { o1, o2 -> o1.name.compareTo(o2.name, true) }
        val SortBySize = Comparator<PdfFolder> { o1, o2 -> o2.pdfFiles.size.compareTo(o1.pdfFiles.size) }

    }
}
