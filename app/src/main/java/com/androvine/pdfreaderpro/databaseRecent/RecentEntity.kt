package com.androvine.pdfreaderpro.databaseRecent

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_models")
data class RecentEntity (
    @PrimaryKey(autoGenerate = true) var id: Long = 0L,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "path") var path: String,
    @ColumnInfo(name = "size") var size: Long,
    @ColumnInfo(name = "dateModified") var dateModified: Long,
    @ColumnInfo(name = "parentFolderName") var parentFolderName: String,
    @ColumnInfo(name = "lastOpened") var lastOpened: Long,
    @ColumnInfo(name = "lastPageOpened") var lastPageOpened: Int
    )