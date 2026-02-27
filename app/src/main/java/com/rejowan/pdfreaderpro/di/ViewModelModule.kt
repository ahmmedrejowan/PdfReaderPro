package com.rejowan.pdfreaderpro.di

import com.rejowan.pdfreaderpro.presentation.screens.folder.FolderDetailViewModel
import com.rejowan.pdfreaderpro.presentation.screens.home.HomeViewModel
import com.rejowan.pdfreaderpro.presentation.screens.onboarding.OnboardingViewModel
import com.rejowan.pdfreaderpro.presentation.screens.reader.ReaderViewModel
import com.rejowan.pdfreaderpro.presentation.screens.search.SearchViewModel
import com.rejowan.pdfreaderpro.presentation.screens.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::FolderDetailViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModel { params -> ReaderViewModel(get(), get(), get(), androidContext(), params.get()) }
}
