package com.androvine.pdfreaderpro.repoModels

import com.androvine.pdfreaderpro.dataClasses.PdfFile

interface PdfFileRepository {
    suspend fun getAllPdfFiles(): List<PdfFile>
    suspend fun deletePdfFile(pdfFile: PdfFile): Boolean
}