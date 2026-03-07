package com.rejowan.pdfreaderpro.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rejowan.pdfreaderpro.data.local.database.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Query("SELECT * FROM bookmarks WHERE pdfPath = :pdfPath ORDER BY pageNumber ASC")
    fun getBookmarksForPdf(pdfPath: String): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE id = :id")
    suspend fun getById(id: Long): BookmarkEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE pdfPath = :pdfPath AND pageNumber = :pageNumber)")
    suspend fun isPageBookmarked(pdfPath: String, pageNumber: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BookmarkEntity): Long

    @Update
    suspend fun update(bookmark: BookmarkEntity)

    @Delete
    suspend fun delete(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE pdfPath = :pdfPath AND pageNumber = :pageNumber")
    suspend fun deleteByPage(pdfPath: String, pageNumber: Int)

    @Query("DELETE FROM bookmarks WHERE pdfPath = :pdfPath")
    suspend fun deleteAllForPdf(pdfPath: String)

    @Query("DELETE FROM bookmarks")
    suspend fun clearAll()
}
