package com.androvine.pdfreaderpro.database

import android.content.ContentValues
import android.content.Context
import com.androvine.pdfreaderpro.dataClasses.RecentModel

class RecentDBHelper(context: Context) : DBHelper(context) {

    companion object {
        const val RECENT_TABLE_NAME = "recent_table"

        const val COLUMN_ID = "id_recent"
        const val COLUMN_FILE_NAME = "file_name_recent"
        const val COLUMN_FILE_PATH = "file_path_recent"
        const val COLUMN_FILE_SIZE = "file_size_recent"
        const val COLUMN_DATE_MODIFIED = "date_modified_recent"
        const val COLUMN_PARENT_FOLDER_NAME = "parent_folder_name_recent"
        const val COLUMN_LAST_OPENED_DATE = "last_opened_date_recent"
        const val COLUMN_TOTAL_PAGE_COUNT = "total_page_count_recent"
        const val COLUMN_LAST_PAGE_OPENED = "last_page_opened_recent"

        const val SQL_CREATE_RECENT_TABLE =
            "CREATE TABLE IF NOT EXISTS $RECENT_TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,$COLUMN_FILE_NAME TEXT,$COLUMN_FILE_PATH TEXT,$COLUMN_FILE_SIZE INTEGER,$COLUMN_DATE_MODIFIED INTEGER,$COLUMN_PARENT_FOLDER_NAME TEXT,$COLUMN_LAST_OPENED_DATE INTEGER,$COLUMN_TOTAL_PAGE_COUNT INTEGER,$COLUMN_LAST_PAGE_OPENED INTEGER)"

        const val SQL_DELETE_RECENT_TABLE = "DROP TABLE IF EXISTS $RECENT_TABLE_NAME"


    }

    //  ------------------------------INIT------------------------------------

    init {
        val db = this.writableDatabase
        db.execSQL(SQL_CREATE_RECENT_TABLE)
    }

    fun dropRecentTable() {
        val db = this.writableDatabase
        db.execSQL(SQL_DELETE_RECENT_TABLE)
    }

    //  ------------------------------CREATE---------------------------------

    fun addRecentItem(recentModel: RecentModel): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FILE_NAME, recentModel.name)
            put(COLUMN_FILE_PATH, recentModel.path)
            put(COLUMN_FILE_SIZE, recentModel.size)
            put(COLUMN_DATE_MODIFIED, recentModel.dateModified)
            put(COLUMN_PARENT_FOLDER_NAME, recentModel.parentFolderName)
            put(COLUMN_LAST_OPENED_DATE, recentModel.lastOpenedDate)
            put(COLUMN_TOTAL_PAGE_COUNT, recentModel.totalPageCount)
            put(COLUMN_LAST_PAGE_OPENED, recentModel.lastPageOpened)
        }
        return db.insert(RECENT_TABLE_NAME, null, values)
    }


    //  ------------------------------READ-----------------------------------

    fun getAllRecentItem(): List<RecentModel> {
        val db = this.readableDatabase
        val cursor = db.query(
            RECENT_TABLE_NAME, arrayOf(
                COLUMN_ID,
                COLUMN_FILE_NAME,
                COLUMN_FILE_PATH,
                COLUMN_FILE_SIZE,
                COLUMN_DATE_MODIFIED,
                COLUMN_PARENT_FOLDER_NAME,
                COLUMN_LAST_OPENED_DATE,
                COLUMN_TOTAL_PAGE_COUNT,
                COLUMN_LAST_PAGE_OPENED
            ), null, null, null, null, "$COLUMN_LAST_OPENED_DATE DESC"
        )
        val recentList = mutableListOf<RecentModel>()
        with(cursor) {
            while (moveToNext()) {
                val recentModel = RecentModel(
                    getInt(getColumnIndexOrThrow(COLUMN_ID)),
                    getString(getColumnIndexOrThrow(COLUMN_FILE_NAME)),
                    getString(getColumnIndexOrThrow(COLUMN_FILE_PATH)),
                    getLong(getColumnIndexOrThrow(COLUMN_FILE_SIZE)),
                    getLong(getColumnIndexOrThrow(COLUMN_DATE_MODIFIED)),
                    getString(getColumnIndexOrThrow(COLUMN_PARENT_FOLDER_NAME)),
                    getLong(getColumnIndexOrThrow(COLUMN_LAST_OPENED_DATE)),
                    getInt(getColumnIndexOrThrow(COLUMN_TOTAL_PAGE_COUNT)),
                    getInt(getColumnIndexOrThrow(COLUMN_LAST_PAGE_OPENED))
                )
                recentList.add(recentModel)
            }
        }
        return recentList
    }

    fun getGetRecentByPath(path: String): RecentModel? {
        val db = this.readableDatabase
        val cursor = db.query(
            RECENT_TABLE_NAME, arrayOf(
                COLUMN_ID,
                COLUMN_FILE_NAME,
                COLUMN_FILE_PATH,
                COLUMN_FILE_SIZE,
                COLUMN_DATE_MODIFIED,
                COLUMN_PARENT_FOLDER_NAME,
                COLUMN_LAST_OPENED_DATE,
                COLUMN_TOTAL_PAGE_COUNT,
                COLUMN_LAST_PAGE_OPENED
            ), "$COLUMN_FILE_PATH = ?", arrayOf(path), null, null, null
        )
        var recentModel: RecentModel? = null
        with(cursor) {
            while (moveToNext()) {
                recentModel = RecentModel(
                    getInt(getColumnIndexOrThrow(COLUMN_ID)),
                    getString(getColumnIndexOrThrow(COLUMN_FILE_NAME)),
                    getString(getColumnIndexOrThrow(COLUMN_FILE_PATH)),
                    getLong(getColumnIndexOrThrow(COLUMN_FILE_SIZE)),
                    getLong(getColumnIndexOrThrow(COLUMN_DATE_MODIFIED)),
                    getString(getColumnIndexOrThrow(COLUMN_PARENT_FOLDER_NAME)),
                    getLong(getColumnIndexOrThrow(COLUMN_LAST_OPENED_DATE)),
                    getInt(getColumnIndexOrThrow(COLUMN_TOTAL_PAGE_COUNT)),
                    getInt(getColumnIndexOrThrow(COLUMN_LAST_PAGE_OPENED))
                )
            }
        }
        return recentModel
    }

    //  ------------------------------UPDATE---------------------------------

    fun updateRecent(recentModel: RecentModel): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FILE_NAME, recentModel.name)
            put(COLUMN_FILE_PATH, recentModel.path)
            put(COLUMN_FILE_SIZE, recentModel.size)
            put(COLUMN_DATE_MODIFIED, recentModel.dateModified)
            put(COLUMN_PARENT_FOLDER_NAME, recentModel.parentFolderName)
            put(COLUMN_LAST_OPENED_DATE, recentModel.lastOpenedDate)
            put(COLUMN_TOTAL_PAGE_COUNT, recentModel.totalPageCount)
            put(COLUMN_LAST_PAGE_OPENED, recentModel.lastPageOpened)
        }
        return db.update(
            RECENT_TABLE_NAME, values, "$COLUMN_FILE_PATH = ?", arrayOf(recentModel.path)
        ).toLong()
    }

    //  ------------------------------DELETE---------------------------------

    fun deleteRecentItem(path: String): Int {
        val db = this.writableDatabase
        return db.delete(RECENT_TABLE_NAME, "$COLUMN_FILE_PATH = ?", arrayOf(path))
    }

    fun deleteAllRecentItem(): Int {
        val db = this.writableDatabase
        return db.delete(RECENT_TABLE_NAME, null, null)
    }


    // ------------------------------OTHERS---------------------------------

    fun checkIfExists(path: String): Boolean {

        val db = this.readableDatabase
        val cursor = db.query(
            RECENT_TABLE_NAME, arrayOf(
                COLUMN_ID,
                COLUMN_FILE_NAME,
                COLUMN_FILE_PATH,
                COLUMN_FILE_SIZE,
                COLUMN_DATE_MODIFIED,
                COLUMN_PARENT_FOLDER_NAME,
                COLUMN_LAST_OPENED_DATE,
                COLUMN_TOTAL_PAGE_COUNT,
                COLUMN_LAST_PAGE_OPENED
            ), "$COLUMN_FILE_PATH = ?", arrayOf(path), null, null, null
        )
        var recentModel: RecentModel? = null
        with(cursor) {
            while (moveToNext()) {
                recentModel = RecentModel(
                    getInt(getColumnIndexOrThrow(COLUMN_ID)),
                    getString(getColumnIndexOrThrow(COLUMN_FILE_NAME)),
                    getString(getColumnIndexOrThrow(COLUMN_FILE_PATH)),
                    getLong(getColumnIndexOrThrow(COLUMN_FILE_SIZE)),
                    getLong(getColumnIndexOrThrow(COLUMN_DATE_MODIFIED)),
                    getString(getColumnIndexOrThrow(COLUMN_PARENT_FOLDER_NAME)),
                    getLong(getColumnIndexOrThrow(COLUMN_LAST_OPENED_DATE)),
                    getInt(getColumnIndexOrThrow(COLUMN_TOTAL_PAGE_COUNT)),
                    getInt(getColumnIndexOrThrow(COLUMN_LAST_PAGE_OPENED))
                )
            }
        }
        return recentModel != null
    }



}