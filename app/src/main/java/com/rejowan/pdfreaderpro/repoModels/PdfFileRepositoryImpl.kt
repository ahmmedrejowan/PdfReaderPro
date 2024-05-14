package com.rejowan.pdfreaderpro.repoModels

import android.content.Context
import android.media.MediaScannerConnection
import android.provider.MediaStore
import com.rejowan.pdfreaderpro.dataClasses.PdfFile
import com.rejowan.pdfreaderpro.interfaces.PdfFileRepository
import java.io.File

class PdfFileRepositoryImpl(private val context: Context) : PdfFileRepository {

    override suspend fun getAllPdfFiles(): List<PdfFile> {
        val pdfFiles = mutableListOf<PdfFile>()

        val cursor = context.contentResolver.query(
            MediaStore.Files.getContentUri("external"), arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
            ), MediaStore.Files.FileColumns.MIME_TYPE + "=?", arrayOf("application/pdf"), null
        )

        cursor?.use {
            while (cursor.moveToNext()) {

                val indexName = cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE)
                val indexData = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                val indexSize = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                val indexDateModified =
                    cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)

                if (indexName == -1 || indexData == -1 || indexSize == -1 || indexDateModified == -1) {
                    continue
                }

                val name = cursor.getString(indexName)
                val path = cursor.getString(indexData)
                val size = cursor.getLong(indexSize)
                val dateModified = cursor.getLong(indexDateModified)
                val folderName = getFolderNameFromPath(path)

                val file = File(path)
                if (file.exists()) {
                    pdfFiles.add(PdfFile(name, path, size, dateModified, folderName))
                }
            }
        }

        return pdfFiles
    }

    override suspend fun deletePdfFile(pdfFile: PdfFile): Boolean {
        val file = File(pdfFile.path)
        return file.delete()
    }


    private fun getFolderNameFromPath(path: String): String {
        val folders = path.split("/")
        return folders[folders.size - 2]
    }

    override suspend fun renamePdfFile(pdfFile: PdfFile, newName: String): PdfFile? {
        val oldFile = File(pdfFile.path)
        val newFile = File(oldFile.parent, "$newName.pdf")

        // Check if the destination file name already exists
        if (newFile.exists()) return null

        if (oldFile.renameTo(newFile)) {
            // Update MediaStore entries after renaming
            MediaScannerConnection.scanFile(
                context,
                arrayOf(oldFile.absolutePath, newFile.absolutePath),
                null,
                null
            )

            return PdfFile(
                name = newName,
                path = newFile.path,
                size = newFile.length(),
                dateModified = newFile.lastModified(),
                parentFolderName = getFolderNameFromPath(newFile.path)
            )
        } else {
            return null
        }
    }
}