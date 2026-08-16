package com.rejowan.pdfreaderpro.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.UUID

/**
 * The signatures the user has drawn, typed or picked, kept so placing one a
 * second time is a single tap.
 *
 * Stored as PNGs in the app's private files directory rather than in the
 * database: they are images, they are only ever read whole, and keeping them out
 * of Room avoids loading blobs the reader does not need.
 */
class SignatureStore(private val context: Context) {

    private val directory: File
        get() = File(context.filesDir, DIRECTORY).apply { if (!exists()) mkdirs() }

    /** A saved signature, newest first when listed. */
    data class SavedSignature(
        val id: String,
        val file: File,
        val savedAt: Long
    )

    suspend fun list(): List<SavedSignature> = withContext(Dispatchers.IO) {
        directory.listFiles { f -> f.isFile && f.extension == "png" }
            ?.map { SavedSignature(it.nameWithoutExtension, it, it.lastModified()) }
            ?.sortedByDescending { it.savedAt }
            ?: emptyList()
    }

    suspend fun save(bitmap: Bitmap): SavedSignature? = withContext(Dispatchers.IO) {
        try {
            val id = UUID.randomUUID().toString()
            val file = File(directory, "$id.png")
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            SavedSignature(id, file, file.lastModified())
        } catch (e: Exception) {
            Timber.e(e, "Could not save the signature")
            null
        }
    }

    suspend fun delete(id: String): Boolean = withContext(Dispatchers.IO) {
        File(directory, "$id.png").let { if (it.exists()) it.delete() else false }
    }

    suspend fun load(id: String): Bitmap? = withContext(Dispatchers.IO) {
        val file = File(directory, "$id.png")
        if (!file.exists()) null else BitmapFactory.decodeFile(file.absolutePath)
    }

    private companion object {
        const val DIRECTORY = "signatures"
    }
}
