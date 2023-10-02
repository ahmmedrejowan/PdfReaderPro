package com.androvine.pdfreaderpro.appClasses

import com.androvine.pdfreaderpro.repoModels.PdfFileRepository
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
