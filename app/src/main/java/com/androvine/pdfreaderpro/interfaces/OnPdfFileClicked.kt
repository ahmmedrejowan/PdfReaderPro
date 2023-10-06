package com.androvine.pdfreaderpro.interfaces

import com.androvine.pdfreaderpro.dataClasses.PdfFile

interface OnPdfFileClicked {
    fun onPdfFileRenamed(pdfFile: PdfFile, newName: String)
    fun onPdfFileDeleted(pdfFile: PdfFile)
}