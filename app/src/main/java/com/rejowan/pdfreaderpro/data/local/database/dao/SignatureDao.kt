package com.rejowan.pdfreaderpro.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.rejowan.pdfreaderpro.data.local.database.entity.SignatureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SignatureDao {

    /** Pending signatures for a document, in the order they were placed. */
    @Query("SELECT * FROM signatures WHERE pdfPath = :pdfPath ORDER BY pageIndex ASC, createdAt ASC")
    fun observe(pdfPath: String): Flow<List<SignatureEntity>>

    @Query("SELECT * FROM signatures WHERE pdfPath = :pdfPath ORDER BY pageIndex ASC, createdAt ASC")
    suspend fun get(pdfPath: String): List<SignatureEntity>

    @Insert
    suspend fun insert(signature: SignatureEntity): Long

    @Update
    suspend fun update(signature: SignatureEntity)

    @Delete
    suspend fun delete(signature: SignatureEntity)

    @Query("DELETE FROM signatures WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * Clears the pending placements for a document.
     *
     * Used after they have been written into the PDF, where the file now carries
     * them, and when the user discards them.
     */
    @Query("DELETE FROM signatures WHERE pdfPath = :pdfPath")
    suspend fun deleteAllFor(pdfPath: String)

    @Query("SELECT COUNT(*) FROM signatures WHERE pdfPath = :pdfPath")
    suspend fun countFor(pdfPath: String): Int
}
