package com.androvine.pdfreaderpro.databaseRecent

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecent(recentEntity: RecentEntity) : Long

    @Update
    fun updateRecent(recentEntity: RecentEntity) : Int

    @Query("SELECT * FROM recent_models ORDER BY lastOpened DESC")
    fun getAllRecent(): Flow<MutableList<RecentEntity>>

    @Query("SELECT * FROM recent_models WHERE id = :id")
    fun getRecentById(id: Long): RecentEntity

    @Query("SELECT * FROM recent_models WHERE path = :path")
    fun getRecentByPath(path: String): RecentEntity

    @Delete
    fun deleteRecent(recentEntity: RecentEntity)


}