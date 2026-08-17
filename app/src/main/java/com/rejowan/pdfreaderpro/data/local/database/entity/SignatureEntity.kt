package com.rejowan.pdfreaderpro.data.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A signature the user has placed on a document but not yet written into the PDF.
 *
 * Held here for the same reason highlights are: placing one should not force an
 * immediate save-or-lose decision, and coming back to the document later should
 * find the work still there.
 *
 * Only pending placements live in this table. Once they are written into the file
 * they belong to the PDF, the viewer draws them from the document itself, and the
 * app can no longer move or remove them, exactly as with baked highlights.
 */
@Entity(
    tableName = "signatures",
    indices = [
        Index(value = ["pdfPath"]),
        Index(value = ["pdfPath", "pageIndex"])
    ]
)
data class SignatureEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val pdfPath: String,

    /** 0-based, matching the viewer. */
    val pageIndex: Int,

    /**
     * Where it sits, in PDF user space, as the viewer reports it:
     * left, bottom, right, top. Stored rather than normalised because this is the
     * form the viewer both gives and takes, so nothing is converted twice.
     */
    val rectLeft: Float,
    val rectBottom: Float,
    val rectRight: Float,
    val rectTop: Float,

    /**
     * The saved signature this was placed from, as its id in the signature store.
     * Null if it was placed from a one-off capture the user chose not to keep, in
     * which case [imagePath] carries it instead.
     */
    val savedSignatureId: String? = null,

    /** A private copy of the image, so a placement survives deleting the original. */
    val imagePath: String,

    val createdAt: Long = System.currentTimeMillis()
)
