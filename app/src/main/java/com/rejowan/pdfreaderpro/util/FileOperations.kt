package com.rejowan.pdfreaderpro.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.File

object FileOperations {

    fun sharePdf(context: Context, filePath: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Timber.e("File not found: $filePath")
                return
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(
                Intent.createChooser(shareIntent, "Share PDF")
            )
        } catch (e: Exception) {
            Timber.e(e, "Error sharing file")
        }
    }

    fun renameFile(filePath: String, newName: String): Boolean {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                Timber.e("File not found: $filePath")
                return false
            }

            val newFileName = if (newName.endsWith(".pdf", ignoreCase = true)) {
                newName
            } else {
                "$newName.pdf"
            }

            val newFile = File(file.parentFile, newFileName)

            if (newFile.exists()) {
                Timber.e("File with name $newFileName already exists")
                return false
            }

            file.renameTo(newFile)
        } catch (e: Exception) {
            Timber.e(e, "Error renaming file")
            false
        }
    }

    fun deleteFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                Timber.e("File not found: $filePath")
                return false
            }
            file.delete()
        } catch (e: Exception) {
            Timber.e(e, "Error deleting file")
            false
        }
    }

    fun getFileSize(filePath: String): Long {
        return try {
            File(filePath).length()
        } catch (e: Exception) {
            0L
        }
    }

    fun fileExists(filePath: String): Boolean {
        return try {
            File(filePath).exists()
        } catch (e: Exception) {
            false
        }
    }
}
