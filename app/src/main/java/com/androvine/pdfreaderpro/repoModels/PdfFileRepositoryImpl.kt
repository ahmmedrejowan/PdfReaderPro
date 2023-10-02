package com.androvine.pdfreaderpro.repoModels

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.androvine.pdfreaderpro.dataClasses.PdfFile

class PdfFileRepositoryImpl(private val context: Context) : PdfFileRepository {

    override suspend fun getAllPdfFiles(): List<PdfFile> {
        val pdfFiles = mutableListOf<PdfFile>()

        val cursor = context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.PARENT
            ),
            MediaStore.Files.FileColumns.MIME_TYPE + "=?",
            arrayOf("application/pdf"),
            null
        )

        cursor?.use {
            while (cursor.moveToNext()) {

                val indexName = cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE)
                val indexData = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                val indexSize = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                val indexDateModified =
                    cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val indexParent = cursor.getColumnIndex(MediaStore.Files.FileColumns.PARENT)

                Log.d("PdfFileRepositoryImpl", "getAllPdfFiles: indexName: $indexName")

//                if (indexName == -1 || indexData == -1 || indexSize == -1 || indexDateModified == -1 || indexParent == -1) {
//                    continue
//                }

                val name = cursor.getString(indexName)
                val path = cursor.getString(indexData)
                val size = cursor.getLong(indexSize)
                val dateModified = cursor.getLong(indexDateModified)
                val parent = cursor.getString(indexParent)

                pdfFiles.add(PdfFile(name, path, size, dateModified, parent))
            }
        }

        return pdfFiles
    }


}