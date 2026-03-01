package com.rejowan.pdfreaderpro.di

import androidx.room.Room
import com.rejowan.pdfreaderpro.data.local.database.PdfDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    // Room Database
    single {
        Room.databaseBuilder(
            androidContext(),
            PdfDatabase::class.java,
            PdfDatabase.DATABASE_NAME
        )
            .addMigrations(*PdfDatabase.migrations)
            .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
            .build()
    }

    // DAOs
    single { get<PdfDatabase>().recentDao() }
    single { get<PdfDatabase>().favoriteDao() }
    single { get<PdfDatabase>().bookmarkDao() }
    single { get<PdfDatabase>().annotationDao() }
}
