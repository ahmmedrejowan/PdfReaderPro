package com.rejowan.pdfreaderpro.di

import com.rejowan.pdfreaderpro.presentation.screens.folder.FolderDetailViewModel
import com.rejowan.pdfreaderpro.presentation.screens.home.HomeViewModel
import com.rejowan.pdfreaderpro.presentation.screens.onboarding.OnboardingViewModel
import com.rejowan.pdfreaderpro.presentation.screens.reader.ReaderViewModel
import com.rejowan.pdfreaderpro.presentation.screens.search.SearchViewModel
import com.rejowan.pdfreaderpro.presentation.screens.settings.SettingsViewModel
import com.rejowan.pdfreaderpro.presentation.screens.tools.compress.CompressViewModel
import com.rejowan.pdfreaderpro.presentation.screens.tools.merge.MergeViewModel
import com.rejowan.pdfreaderpro.presentation.screens.tools.lock.LockViewModel
import com.rejowan.pdfreaderpro.presentation.screens.tools.reorder.ReorderViewModel
import com.rejowan.pdfreaderpro.presentation.screens.tools.unlock.UnlockViewModel
import com.rejowan.pdfreaderpro.presentation.screens.tools.removepages.RemovePagesViewModel
import com.rejowan.pdfreaderpro.presentation.screens.tools.rotate.RotateViewModel
import com.rejowan.pdfreaderpro.presentation.screens.tools.watermark.WatermarkViewModel
import com.rejowan.pdfreaderpro.presentation.screens.tools.pagenumbers.PageNumbersViewModel
import com.rejowan.pdfreaderpro.presentation.screens.tools.imagetopdf.ImageToPdfViewModel
import com.rejowan.pdfreaderpro.presentation.screens.tools.pdftoimage.PdfToImageViewModel
import com.rejowan.pdfreaderpro.presentation.screens.tools.split.SplitViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::FolderDetailViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModel { params -> ReaderViewModel(get(), get(), get(), get(), get(), get(), get(), get(), androidApplication(), params.get()) }
    viewModel { MergeViewModel(get(), androidApplication()) }
    viewModel { SplitViewModel(get(), androidApplication()) }
    viewModel { CompressViewModel(get(), androidApplication()) }
    viewModel { RotateViewModel(get(), androidApplication()) }
    viewModel { ReorderViewModel(get(), androidApplication()) }
    viewModel { LockViewModel(get(), androidApplication()) }
    viewModel { UnlockViewModel(get(), androidApplication()) }
    viewModel { RemovePagesViewModel(get(), androidApplication()) }
    viewModel { WatermarkViewModel(get(), androidApplication()) }
    viewModel { PageNumbersViewModel(get(), androidApplication()) }
    viewModel { ImageToPdfViewModel(get(), androidApplication()) }
    viewModel { PdfToImageViewModel(get(), androidApplication()) }
}
