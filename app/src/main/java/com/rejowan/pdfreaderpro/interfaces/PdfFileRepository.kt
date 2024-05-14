package com.rejowan.pdfreaderpro.interfaces

import com.rejowan.pdfreaderpro.dataClasses.PdfFile

interface PdfFileRepository {
    suspend fun getAllPdfFiles(): List<PdfFile>
    suspend fun deletePdfFile(pdfFile: PdfFile): Boolean
    suspend fun renamePdfFile(pdfFile: PdfFile, newName: String): PdfFile?

}