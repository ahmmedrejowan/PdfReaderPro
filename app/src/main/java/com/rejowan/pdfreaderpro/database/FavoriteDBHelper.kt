package com.rejowan.pdfreaderpro.database

import android.content.ContentValues
import android.content.Context
import com.rejowan.pdfreaderpro.dataClasses.PdfFile


class FavoriteDBHelper(context: Context) : DBHelper(context) {

    companion object {
        const val FAVORITE_TABLE_NAME = "favorite"

        private const val COLUMN_ID = "id_favorite"
        private const val COLUMN_FILE_NAME = "file_name_favorite"
        private const val COLUMN_FILE_PATH = "file_path_favorite"
        private const val COLUMN_FILE_SIZE = "file_size_favorite"
        private const val COLUMN_DATE_MODIFIED = "date_modified_favorite"
        private const val COLUMN_PARENT_FOLDER_NAME = "parent_folder_name_favorite"


        private const val SQL_CREATE_FAVORITE_TABLE =
            "CREATE TABLE IF NOT EXISTS $FAVORITE_TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,$COLUMN_FILE_NAME TEXT,$COLUMN_FILE_PATH TEXT,$COLUMN_FILE_SIZE INTEGER,$COLUMN_DATE_MODIFIED INTEGER,$COLUMN_PARENT_FOLDER_NAME TEXT)"


        private const val SQL_DELETE_FAVORITE_TABLE = "DROP TABLE IF EXISTS $FAVORITE_TABLE_NAME"


    }

    //  ------------------------------INIT------------------------------------

    init {
        val db = this.writableDatabase
        db.execSQL(SQL_CREATE_FAVORITE_TABLE)

    }

    fun dropFavoriteTable() {
        val db = this.writableDatabase
        db.execSQL(SQL_DELETE_FAVORITE_TABLE)
    }

    //  ------------------------------CREATE---------------------------------

    fun addFavoriteItem(pdfFile: PdfFile): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FILE_NAME, pdfFile.name)
            put(COLUMN_FILE_PATH, pdfFile.path)
            put(COLUMN_FILE_SIZE, pdfFile.size)
            put(COLUMN_DATE_MODIFIED, pdfFile.dateModified)
            put(COLUMN_PARENT_FOLDER_NAME, pdfFile.parentFolderName)
        }
        val mLong = db.insert(FAVORITE_TABLE_NAME, null, values)
        db.close()
        return mLong

    }


    //  ------------------------------READ-----------------------------------


    fun getAllFavoriteItem(): List<PdfFile> {
        val db = this.readableDatabase
        val cursor = db.query(
            FAVORITE_TABLE_NAME, arrayOf(
                COLUMN_ID,
                COLUMN_FILE_NAME,
                COLUMN_FILE_PATH,
                COLUMN_FILE_SIZE,
                COLUMN_DATE_MODIFIED,
                COLUMN_PARENT_FOLDER_NAME
            ), null, null, null, null, "$COLUMN_DATE_MODIFIED DESC"
        )
        val recentList = mutableListOf<PdfFile>()
        with(cursor) {
            while (moveToNext()) {
                val recentModel = PdfFile(
                    getString(getColumnIndexOrThrow(COLUMN_FILE_NAME)),
                    getString(getColumnIndexOrThrow(COLUMN_FILE_PATH)),
                    getLong(getColumnIndexOrThrow(COLUMN_FILE_SIZE)),
                    getLong(getColumnIndexOrThrow(COLUMN_DATE_MODIFIED)),
                    getString(getColumnIndexOrThrow(COLUMN_PARENT_FOLDER_NAME))
                )
                recentList.add(recentModel)
            }
        }
        cursor.close()
        db.close()
        return recentList
    }


    fun getFavoriteByPath(path: String): PdfFile? {
        val db = this.readableDatabase
        val cursor = db.query(
            FAVORITE_TABLE_NAME, arrayOf(
                COLUMN_ID,
                COLUMN_FILE_NAME,
                COLUMN_FILE_PATH,
                COLUMN_FILE_SIZE,
                COLUMN_DATE_MODIFIED,
                COLUMN_PARENT_FOLDER_NAME
            ), "$COLUMN_FILE_PATH = ?", arrayOf(path), null, null, null
        )
        var recentModel: PdfFile? = null
        with(cursor) {
            while (moveToNext()) {
                recentModel = PdfFile(
                    getString(getColumnIndexOrThrow(COLUMN_FILE_NAME)),
                    getString(getColumnIndexOrThrow(COLUMN_FILE_PATH)),
                    getLong(getColumnIndexOrThrow(COLUMN_FILE_SIZE)),
                    getLong(getColumnIndexOrThrow(COLUMN_DATE_MODIFIED)),
                    getString(getColumnIndexOrThrow(COLUMN_PARENT_FOLDER_NAME))
                )
            }
        }
        cursor.close()
        db.close()
        return recentModel
    }


    //  ------------------------------UPDATE---------------------------------

    fun updateFavorite(pdfFile: PdfFile): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FILE_NAME, pdfFile.name)
            put(COLUMN_FILE_PATH, pdfFile.path)
            put(COLUMN_FILE_SIZE, pdfFile.size)
            put(COLUMN_DATE_MODIFIED, pdfFile.dateModified)
            put(COLUMN_PARENT_FOLDER_NAME, pdfFile.parentFolderName)
        }
        val mLong = db.update(
            FAVORITE_TABLE_NAME, values, "$COLUMN_FILE_PATH = ?", arrayOf(pdfFile.path)
        ).toLong()

        db.close()
        return mLong
    }


    //  ------------------------------DELETE---------------------------------

    fun deleteFavorite(path: String): Int {
        val db = this.writableDatabase
        val mInt = db.delete(FAVORITE_TABLE_NAME, "$COLUMN_FILE_PATH = ?", arrayOf(path))
        db.close()
        return mInt
    }

    fun deleteAllFavorite(): Int {
        val db = this.writableDatabase
        return db.delete(FAVORITE_TABLE_NAME, null, null)
    }


    // ------------------------------OTHERS---------------------------------


    fun checkIfExists(path: String): Boolean {

        val db = this.readableDatabase
        val cursor = db.query(
            FAVORITE_TABLE_NAME, arrayOf(
                COLUMN_ID,
                COLUMN_FILE_NAME,
                COLUMN_FILE_PATH,
                COLUMN_FILE_SIZE,
                COLUMN_DATE_MODIFIED,
                COLUMN_PARENT_FOLDER_NAME
            ), "$COLUMN_FILE_PATH = ?", arrayOf(path), null, null, null
        )
        var recentModel: PdfFile? = null
        with(cursor) {
            while (moveToNext()) {
                recentModel = PdfFile(
                    getString(getColumnIndexOrThrow(COLUMN_FILE_NAME)),
                    getString(getColumnIndexOrThrow(COLUMN_FILE_PATH)),
                    getLong(getColumnIndexOrThrow(COLUMN_FILE_SIZE)),
                    getLong(getColumnIndexOrThrow(COLUMN_DATE_MODIFIED)),
                    getString(getColumnIndexOrThrow(COLUMN_PARENT_FOLDER_NAME))
                )
            }
        }
        cursor.close()
        db.close()
        return recentModel != null
    }


}