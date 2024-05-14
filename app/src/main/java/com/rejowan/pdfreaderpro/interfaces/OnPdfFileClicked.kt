package com.rejowan.pdfreaderpro.interfaces

import com.rejowan.pdfreaderpro.dataClasses.PdfFile

interface OnPdfFileClicked {
    fun onPdfFileRenamed(pdfFile: PdfFile, newName: String)
    fun onPdfFileDeleted(pdfFile: PdfFile)
}