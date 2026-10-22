package com.rejowan.pdfreaderpro.di

import com.rejowan.pdfreaderpro.data.repository.FavoriteRepositoryImpl
import com.rejowan.pdfreaderpro.data.repository.PdfFileRepositoryImpl
import com.rejowan.pdfreaderpro.data.repository.PdfToolsRepositoryImpl
import com.rejowan.pdfreaderpro.data.repository.PreferencesRepositoryImpl
import com.rejowan.pdfreaderpro.data.repository.RecentRepositoryImpl
import com.rejowan.pdfreaderpro.data.repository.UpdateRepositoryImpl
import com.rejowan.pdfreaderpro.domain.repository.FavoriteRepository
import com.rejowan.pdfreaderpro.domain.repository.PdfFileRepository
import com.rejowan.pdfreaderpro.domain.repository.PdfToolsRepository
import com.rejowan.pdfreaderpro.domain.repository.PreferencesRepository
import com.rejowan.pdfreaderpro.domain.repository.RecentRepository
import com.rejowan.pdfreaderpro.domain.repository.UpdateRepository
import com.rejowan.pdfreaderpro.util.ApkDownloadManager
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single<PdfFileRepository> {
        PdfFileRepositoryImpl(
            androidContext(),
            get<PreferencesRepository>().preferences.map { it.hideHiddenFiles }.distinctUntilChanged()
        )
    }
    single<RecentRepository> { RecentRepositoryImpl(get()) }
    single<FavoriteRepository> { FavoriteRepositoryImpl(get()) }
    single<PreferencesRepository> { PreferencesRepositoryImpl(get()) }
    single<PdfToolsRepository> { PdfToolsRepositoryImpl(androidContext()) }
    single<UpdateRepository> { UpdateRepositoryImpl(get(), get()) }
    single { ApkDownloadManager(androidContext()) }
}
