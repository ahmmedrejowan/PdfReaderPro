package com.rejowan.pdfreaderpro.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rejowan.pdfreaderpro.data.local.database.dao.AnnotationDao
import com.rejowan.pdfreaderpro.data.local.database.dao.BookmarkDao
import com.rejowan.pdfreaderpro.data.local.database.dao.FavoriteDao
import com.rejowan.pdfreaderpro.data.local.database.dao.RecentDao
import com.rejowan.pdfreaderpro.data.local.database.entity.AnnotationEntity
import com.rejowan.pdfreaderpro.data.local.database.entity.BookmarkEntity
import com.rejowan.pdfreaderpro.data.local.database.entity.FavoriteEntity
import com.rejowan.pdfreaderpro.data.local.database.entity.RecentEntity

@Database(
    entities = [
        RecentEntity::class,
        FavoriteEntity::class,
        BookmarkEntity::class,
        AnnotationEntity::class
    ],
    version = 7,
    exportSchema = true
)
abstract class PdfDatabase : RoomDatabase() {

    abstract fun recentDao(): RecentDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun annotationDao(): AnnotationDao

    companion object {
        const val DATABASE_NAME = "pdf_reader_db"

        /**
         * Migration from v6 to v7.
         * Adds performance indexes on frequently queried columns:
         * - favorites: path (unique), name
         * - bookmarks: pdfPath, pdfPath+pageNumber (unique)
         * - annotations: pdfPath, pdfPath+pageNumber
         */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add indexes to favorites table
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_favorites_path ON favorites (path)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_favorites_name ON favorites (name)")

                // Add indexes to bookmarks table
                db.execSQL("CREATE INDEX IF NOT EXISTS index_bookmarks_pdfPath ON bookmarks (pdfPath)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_bookmarks_pdfPath_pageNumber ON bookmarks (pdfPath, pageNumber)")

                // Add indexes to annotations table
                db.execSQL("CREATE INDEX IF NOT EXISTS index_annotations_pdfPath ON annotations (pdfPath)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_annotations_pdfPath_pageNumber ON annotations (pdfPath, pageNumber)")
            }
        }

        /**
         * Migration from v5 to v6.
         * Adds unique index on path column in recent table.
         * First removes duplicate entries keeping the most recently opened one.
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Remove duplicates, keeping the entry with the highest lastOpened for each path
                db.execSQL("""
                    DELETE FROM recent
                    WHERE id NOT IN (
                        SELECT MAX(id)
                        FROM recent
                        GROUP BY path
                    )
                """)

                // Create unique index on path
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_recent_path ON recent (path)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create new recent table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS recent_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        path TEXT NOT NULL,
                        size INTEGER NOT NULL,
                        lastOpened INTEGER NOT NULL,
                        totalPages INTEGER NOT NULL,
                        lastPage INTEGER NOT NULL
                    )
                """)

                // Copy data from old recent_table to new recent
                db.execSQL("""
                    INSERT INTO recent_new (id, name, path, size, lastOpened, totalPages, lastPage)
                    SELECT
                        id_recent,
                        file_name_recent,
                        file_path_recent,
                        CAST(file_size_recent AS INTEGER),
                        CAST(strftime('%s', last_opened_date_recent) * 1000 AS INTEGER),
                        total_page_count_recent,
                        last_page_opened_recent
                    FROM recent_table
                """)

                // Drop old table and rename new
                db.execSQL("DROP TABLE IF EXISTS recent_table")
                db.execSQL("ALTER TABLE recent_new RENAME TO recent")

                // Create new favorites table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS favorites_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        path TEXT NOT NULL,
                        size INTEGER NOT NULL,
                        dateModified INTEGER NOT NULL,
                        parentFolder TEXT NOT NULL
                    )
                """)

                // Copy data from old favorite table to new favorites
                db.execSQL("""
                    INSERT INTO favorites_new (id, name, path, size, dateModified, parentFolder)
                    SELECT
                        id_favorite,
                        file_name_favorite,
                        file_path_favorite,
                        CAST(file_size_favorite AS INTEGER),
                        CAST(strftime('%s', date_modified_favorite) * 1000 AS INTEGER),
                        COALESCE(parent_folder_name_favorite, '')
                    FROM favorite
                """)

                // Drop old table and rename new
                db.execSQL("DROP TABLE IF EXISTS favorite")
                db.execSQL("ALTER TABLE favorites_new RENAME TO favorites")

                // Create new bookmarks table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS bookmarks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        pdfPath TEXT NOT NULL,
                        pageNumber INTEGER NOT NULL,
                        title TEXT,
                        createdAt INTEGER NOT NULL
                    )
                """)

                // Create new annotations table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS annotations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        pdfPath TEXT NOT NULL,
                        pageNumber INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        content TEXT,
                        color INTEGER,
                        startX REAL,
                        startY REAL,
                        endX REAL,
                        endY REAL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)
            }
        }

        val migrations = arrayOf(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
    }
}
