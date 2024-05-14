package com.rejowan.pdfreaderpro.database

import android.content.ContentValues
import android.content.Context
import com.rejowan.pdfreaderpro.dataClasses.RecentModel

class RecentDBHelper(context: Context) : DBHelper(context) {

    companion object {
        const val RECENT_TABLE_NAME = "recent_table"

      private  const val COLUMN_ID = "id_recent"
        private  const val COLUMN_FILE_NAME = "file_name_recent"
        private  const val COLUMN_FILE_PATH = "file_path_recent"
        private  const val COLUMN_FILE_SIZE = "file_size_recent"
        private  const val COLUMN_LAST_OPENED_DATE = "last_opened_date_recent"
        private  const val COLUMN_TOTAL_PAGE_COUNT = "total_page_count_recent"
        private  const val COLUMN_LAST_PAGE_OPENED = "last_page_opened_recent"

        private const val SQL_CREATE_RECENT_TABLE =
            "CREATE TABLE IF NOT EXISTS $RECENT_TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,$COLUMN_FILE_NAME TEXT,$COLUMN_FILE_PATH TEXT,$COLUMN_FILE_SIZE INTEGER,$COLUMN_LAST_OPENED_DATE INTEGER,$COLUMN_TOTAL_PAGE_COUNT INTEGER,$COLUMN_LAST_PAGE_OPENED INTEGER)"

        private const val SQL_DELETE_RECENT_TABLE = "DROP TABLE IF EXISTS $RECENT_TABLE_NAME"


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
            put(COLUMN_LAST_OPENED_DATE, recentModel.lastOpenedDate)
            put(COLUMN_TOTAL_PAGE_COUNT, recentModel.totalPageCount)
            put(COLUMN_LAST_PAGE_OPENED, recentModel.lastPageOpened)
        }
        val returnValue =  db.insert(RECENT_TABLE_NAME, null, values)
        db.close()
        return returnValue
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
                    getLong(getColumnIndexOrThrow(COLUMN_LAST_OPENED_DATE)),
                    getInt(getColumnIndexOrThrow(COLUMN_TOTAL_PAGE_COUNT)),
                    getInt(getColumnIndexOrThrow(COLUMN_LAST_PAGE_OPENED)),
                )
                recentList.add(recentModel)
            }
        }
        cursor.close()
        db.close()
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
                    getLong(getColumnIndexOrThrow(COLUMN_LAST_OPENED_DATE)),
                    getInt(getColumnIndexOrThrow(COLUMN_TOTAL_PAGE_COUNT)),
                    getInt(getColumnIndexOrThrow(COLUMN_LAST_PAGE_OPENED))
                )
            }
        }
        cursor.close()
        db.close()
        return recentModel
    }

    //  ------------------------------UPDATE---------------------------------

    fun updateRecent(recentModel: RecentModel): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FILE_NAME, recentModel.name)
            put(COLUMN_FILE_PATH, recentModel.path)
            put(COLUMN_FILE_SIZE, recentModel.size)
            put(COLUMN_LAST_OPENED_DATE, recentModel.lastOpenedDate)
            put(COLUMN_TOTAL_PAGE_COUNT, recentModel.totalPageCount)
            put(COLUMN_LAST_PAGE_OPENED, recentModel.lastPageOpened)
        }
        val returnValue = db.update(
            RECENT_TABLE_NAME, values, "$COLUMN_FILE_PATH = ?", arrayOf(recentModel.path)
        ).toLong()
        db.close()
        return returnValue
    }

    //  ------------------------------DELETE---------------------------------

    fun deleteRecentItem(path: String): Int {
        val db = this.writableDatabase
        val returnValue = db.delete(RECENT_TABLE_NAME, "$COLUMN_FILE_PATH = ?", arrayOf(path))
        db.close()
        return returnValue
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
                    getLong(getColumnIndexOrThrow(COLUMN_LAST_OPENED_DATE)),
                    getInt(getColumnIndexOrThrow(COLUMN_TOTAL_PAGE_COUNT)),
                    getInt(getColumnIndexOrThrow(COLUMN_LAST_PAGE_OPENED))
                )
            }
        }
        cursor.close()
        db.close()
        return recentModel != null
    }



}