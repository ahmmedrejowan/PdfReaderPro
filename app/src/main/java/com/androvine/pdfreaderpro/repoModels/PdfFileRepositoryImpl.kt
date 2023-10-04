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
                val parent = getFolderNameFromParentId(cursor.getString(indexParent)) ?: "Unknown"

                pdfFiles.add(PdfFile(name, path, size, dateModified, parent))
            }
        }

        return pdfFiles
    }


    private fun getFolderNameFromParentId(parentId: String): String? {
        val projection = arrayOf(MediaStore.Files.FileColumns.TITLE)
        val selection = MediaStore.Files.FileColumns._ID + "=?"
        val selectionArgs = arrayOf(parentId)

        val cursor = context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            null
        )

        var folderName: String? = null
        cursor?.use {
            if (it.moveToFirst()) {
                val indexName = it.getColumnIndex(MediaStore.Files.FileColumns.TITLE)
                if (indexName == -1) {
                    return null
                }
                folderName = it.getString(indexName)
            }
        }

        return folderName
    }


}