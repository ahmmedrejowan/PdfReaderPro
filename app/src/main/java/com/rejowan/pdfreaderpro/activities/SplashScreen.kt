package com.rejowan.pdfreaderpro.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.util.Log
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.rejowan.pdfreaderpro.R
import com.rejowan.pdfreaderpro.repoModels.IntroRepository
import com.rejowan.pdfreaderpro.repoModels.PermissionRepository
import org.koin.android.ext.android.inject

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {

    private val permissionRepository: PermissionRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)


        if (SDK_INT >= Build.VERSION_CODES.R) {
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

//        val recentDBHelper = RecentDBHelper(this)
//        val favoriteDBHelper = FavoriteDBHelper(this)
//        recentDBHelper.dropRecentTable()
//        favoriteDBHelper.dropFavoriteTable()


        Handler(Looper.getMainLooper()).postDelayed({
            val introUtils = IntroRepository(this@SplashScreen)
            if (introUtils.isFirstTimeLaunch()) {
                startActivity(Intent(this@SplashScreen, Intro::class.java))
                finish()
            } else {

                if (permissionRepository.hasStoragePermission()) {
                    handleIntent(intent)
                } else {
                    startActivity(Intent(this@SplashScreen, PermissionManage::class.java))
                    finish()
                }

            }
        }, 2000)


    }

    private fun handleIntent(intent: Intent) {
        val intentAction = intent.action
        Log.e("TAG", "intentAction: $intentAction intent type ${intent.type}")

        if (Intent.ACTION_SEND == intentAction && "application/pdf" == intent.type) {
            val pdfUri = intent.parcelable<Uri>(Intent.EXTRA_STREAM)
            startActivity(Intent(this, PDFReader::class.java).apply {
                putExtra("pdfUri", pdfUri)
                putExtra("isFromShare", true)
                putExtra("isFromView", false)
                putExtra("isOutside", true)
            })
            finish()
        } else if (Intent.ACTION_VIEW == intentAction) {
            val pdfUri = intent.data
            startActivity(Intent(this, PDFReader::class.java).apply {
                putExtra("pdfUri", pdfUri)
                putExtra("isFromShare", false)
                putExtra("isFromView", true)
                putExtra("isOutside", true)
            })
            finish()
        } else {
            startActivity(Intent(this, Home::class.java))
            finish()
        }


    }


}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}