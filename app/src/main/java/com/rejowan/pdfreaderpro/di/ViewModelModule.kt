package com.rejowan.pdfreaderpro.di

import com.rejowan.pdfreaderpro.presentation.screens.home.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::HomeViewModel)
}
