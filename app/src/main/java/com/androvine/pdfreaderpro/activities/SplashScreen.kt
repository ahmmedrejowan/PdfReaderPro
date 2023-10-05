package com.androvine.pdfreaderpro.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.androvine.pdfreaderpro.R
import com.androvine.pdfreaderpro.repoModels.IntroRepository
import com.androvine.pdfreaderpro.repoModels.PermissionRepository
import org.koin.android.ext.android.inject

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {

    private val permissionRepository: PermissionRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION") window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }


        Handler(Looper.getMainLooper()).postDelayed({
            val introUtils = IntroRepository(this@SplashScreen)
            if (introUtils.isFirstTimeLaunch()) {
                startActivity(Intent(this@SplashScreen, Intro::class.java))
                finish()
            } else {

                if (permissionRepository.hasStoragePermission()) {
                    startActivity(Intent(this@SplashScreen, Home::class.java))
                    finish()
                } else {
                    startActivity(Intent(this@SplashScreen, PermissionManage::class.java))
                    finish()
                }

            }
        }, 2000)


    }


}