package com.rejowan.pdfreaderpro.di

import com.rejowan.pdfreaderpro.data.repository.FavoriteRepositoryImpl
import com.rejowan.pdfreaderpro.data.repository.PdfFileRepositoryImpl
import com.rejowan.pdfreaderpro.data.repository.PreferencesRepositoryImpl
import com.rejowan.pdfreaderpro.data.repository.RecentRepositoryImpl
import com.rejowan.pdfreaderpro.domain.repository.FavoriteRepository
import com.rejowan.pdfreaderpro.domain.repository.PdfFileRepository
import com.rejowan.pdfreaderpro.domain.repository.PreferencesRepository
import com.rejowan.pdfreaderpro.domain.repository.RecentRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single<PdfFileRepository> { PdfFileRepositoryImpl(androidContext()) }
    single<RecentRepository> { RecentRepositoryImpl(get()) }
    single<FavoriteRepository> { FavoriteRepositoryImpl(get()) }
    single<PreferencesRepository> { PreferencesRepositoryImpl(get()) }
}
