package com.rejowan.pdfreaderpro.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.domain.model.PdfFolder
import com.rejowan.pdfreaderpro.domain.repository.PdfFileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class PdfFileRepositoryImpl(
    private val context: Context
) : PdfFileRepository {

    private val _pdfFiles = MutableStateFlow<List<PdfFile>>(emptyList())

    override fun getAllPdfFiles(): Flow<List<PdfFile>> = _pdfFiles

    override fun getPdfsByFolder(folderPath: String): Flow<List<PdfFile>> {
        return _pdfFiles.map { files ->
            files.filter { it.parentFolder == folderPath }
        }
    }

    override fun searchPdfs(query: String): Flow<List<PdfFile>> {
        return _pdfFiles.map { files ->
            if (query.isBlank()) {
                emptyList()
            } else {
                files.filter {
                    it.name.contains(query, ignoreCase = true)
                }
            }
        }
    }

    override fun getPdfFolders(): Flow<List<PdfFolder>> {
        return _pdfFiles.map { files ->
            files.groupBy { it.parentFolder }
                .map { (path, pdfs) ->
                    PdfFolder(
                        path = path,
                        name = path.substringAfterLast("/").ifEmpty { "Storage" },
                        pdfCount = pdfs.size
                    )
                }
                .sortedBy { it.name.lowercase() }
        }
    }

    override suspend fun getPdfFile(path: String): PdfFile? {
        return _pdfFiles.value.find { it.path == path }
    }

    override suspend fun renamePdf(pdfFile: PdfFile, newName: String): Result<PdfFile> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(pdfFile.path)
                val newFile = File(file.parent, "$newName.pdf")

                if (file.renameTo(newFile)) {
                    refreshPdfs()
                    val updatedFile = pdfFile.copy(
                        name = "$newName.pdf",
                        path = newFile.absolutePath
                    )
                    Result.success(updatedFile)
                } else {
                    Result.failure(Exception("Failed to rename file"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Error renaming PDF")
                Result.failure(e)
            }
        }
    }

    override suspend fun deletePdf(pdfFile: PdfFile): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val deleted = context.contentResolver.delete(pdfFile.uri, null, null) > 0
                if (deleted) {
                    refreshPdfs()
                    Result.success(Unit)
                } else {
                    // Try file system delete as fallback
                    val file = File(pdfFile.path)
                    if (file.delete()) {
                        refreshPdfs()
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception("Failed to delete file"))
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error deleting PDF")
                Result.failure(e)
            }
        }
    }

    override suspend fun refreshPdfs() {
        withContext(Dispatchers.IO) {
            val pdfs = queryPdfFiles()
            _pdfFiles.value = pdfs
            Timber.d("Refreshed PDFs: ${pdfs.size} files found")
        }
    }

    private fun queryPdfFiles(): List<PdfFile> {
        val pdfList = mutableListOf<PdfFile>()

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.DATE_ADDED
        )

        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ?"
        val selectionArgs = arrayOf("application/pdf")
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn) ?: "Unknown.pdf"
                    val path = cursor.getString(pathColumn) ?: continue
                    val size = cursor.getLong(sizeColumn)
                    val dateModified = cursor.getLong(dateModifiedColumn) * 1000
                    val dateAdded = cursor.getLong(dateAddedColumn) * 1000

                    // Skip if file doesn't exist
                    if (!File(path).exists()) continue

                    val uri = ContentUris.withAppendedId(collection, id)
                    val parentFolder = File(path).parent ?: ""

                    pdfList.add(
                        PdfFile(
                            id = id,
                            name = name,
                            path = path,
                            uri = uri,
                            size = size,
                            dateModified = dateModified,
                            dateAdded = dateAdded,
                            parentFolder = parentFolder
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error querying PDF files")
        }

        return pdfList
    }
}
