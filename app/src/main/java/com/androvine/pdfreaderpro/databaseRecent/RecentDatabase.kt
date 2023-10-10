package com.androvine.pdfreaderpro.databaseRecent

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(
    entities = [RecentEntity::class],
    version = 1,
    exportSchema = false
)


abstract class RecentDatabase : RoomDatabase() {
    abstract fun recentDao(): RecentDao

}