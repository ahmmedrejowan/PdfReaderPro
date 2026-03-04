package com.rejowan.pdfreaderpro.appClasses

import android.app.Application
import com.rejowan.pdfreaderpro.BuildConfig
import com.rejowan.pdfreaderpro.di.dataStoreModule
import com.rejowan.pdfreaderpro.di.databaseModule
import com.rejowan.pdfreaderpro.di.networkModule
import com.rejowan.pdfreaderpro.di.repositoryModule
import com.rejowan.pdfreaderpro.di.viewModelModule
import com.rejowan.pdfreaderpro.util.GlobalErrorHandler
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        initTimber()

        // Setup global error handler
        GlobalErrorHandler.setup(this)

        // Initialize Koin
        initKoin()

        Timber.d("Application initialized successfully")
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initKoin() {
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
            androidContext(this@MyApplication)
            modules(
                databaseModule,
                dataStoreModule,
                networkModule,
                repositoryModule,
                viewModelModule
            )
        }
    }
}
