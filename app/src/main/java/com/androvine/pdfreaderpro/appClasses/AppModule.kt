package com.androvine.pdfreaderpro.appClasses

import androidx.room.Room
import com.androvine.pdfreaderpro.databaseRecent.RecentDBVM
import com.androvine.pdfreaderpro.databaseRecent.RecentDatabase
import com.androvine.pdfreaderpro.databaseRecent.RecentRepository
import com.androvine.pdfreaderpro.interfaces.PdfFileRepository
import com.androvine.pdfreaderpro.repoModels.PdfFileRepositoryImpl
import com.androvine.pdfreaderpro.repoModels.PermissionRepository
import com.androvine.pdfreaderpro.vms.PdfListViewModel
import com.androvine.pdfreaderpro.vms.PermissionViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Singleton for PermissionRepository
    single { PermissionRepository(androidContext()) }

    // ViewModel for Permission
    viewModel { PermissionViewModel(get()) }

    single<PdfFileRepository> { PdfFileRepositoryImpl(androidContext()) }

    viewModel { PdfListViewModel(get()) }
}

val recentModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            RecentDatabase::class.java,
            "recent_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    // Providing Dao
    single { get<RecentDatabase>().recentDao() }

    // Providing Repository
    factory { RecentRepository(get()) }

    // Providing ViewModel
    viewModel { RecentDBVM(get()) }


}