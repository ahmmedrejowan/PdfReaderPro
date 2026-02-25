package com.rejowan.pdfreaderpro.domain.repository

import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.domain.model.PdfFolder
import kotlinx.coroutines.flow.Flow

interface PdfFileRepository {
    fun getAllPdfFiles(): Flow<List<PdfFile>>
    fun getPdfsByFolder(folderPath: String): Flow<List<PdfFile>>
    fun searchPdfs(query: String): Flow<List<PdfFile>>
    fun getPdfFolders(): Flow<List<PdfFolder>>
    suspend fun getPdfFile(path: String): PdfFile?
    suspend fun renamePdf(pdfFile: PdfFile, newName: String): Result<PdfFile>
    suspend fun deletePdf(pdfFile: PdfFile): Result<Unit>
    suspend fun refreshPdfs()
}
