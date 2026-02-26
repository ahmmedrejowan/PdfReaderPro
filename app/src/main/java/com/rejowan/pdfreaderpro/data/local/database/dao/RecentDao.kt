package com.rejowan.pdfreaderpro.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rejowan.pdfreaderpro.data.local.database.entity.RecentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentDao {

    @Query("SELECT * FROM recent ORDER BY lastOpened DESC")
    fun getAllRecent(): Flow<List<RecentEntity>>

    @Query("SELECT * FROM recent WHERE path = :path")
    suspend fun getByPath(path: String): RecentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(recent: RecentEntity)

    @Query("UPDATE recent SET lastPage = :page, lastOpened = :timestamp WHERE path = :path")
    suspend fun updateLastPage(path: String, page: Int, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun delete(recent: RecentEntity)

    @Query("DELETE FROM recent WHERE path = :path")
    suspend fun deleteByPath(path: String)

    @Query("DELETE FROM recent")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM recent")
    suspend fun getCount(): Int
}
