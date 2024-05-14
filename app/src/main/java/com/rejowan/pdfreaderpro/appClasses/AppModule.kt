package com.rejowan.pdfreaderpro.appClasses

import com.rejowan.pdfreaderpro.interfaces.PdfFileRepository
import com.rejowan.pdfreaderpro.repoModels.PdfFileRepositoryImpl
import com.rejowan.pdfreaderpro.repoModels.PermissionRepository
import com.rejowan.pdfreaderpro.vms.PdfListViewModel
import com.rejowan.pdfreaderpro.vms.PermissionViewModel
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

