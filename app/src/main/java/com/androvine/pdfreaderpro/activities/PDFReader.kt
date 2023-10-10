@file:Suppress("DEPRECATION")

package com.androvine.pdfreaderpro.activities

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.androvine.pdfreaderpro.R
import com.androvine.pdfreaderpro.databinding.ActivityPdfreaderBinding
import com.androvine.pdfreaderpro.databinding.BottomSheetBrightnessBinding
import com.androvine.pdfreaderpro.databinding.BottomSheetJumpBinding
import com.androvine.pdfreaderpro.utils.FormattingUtils.Companion.resizeName
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File

class PDFReader : AppCompatActivity() {

    private val binding: ActivityPdfreaderBinding by lazy {
        ActivityPdfreaderBinding.inflate(layoutInflater)
    }

    private var pdfPath: String = ""
    private var pdfName: String = "null"

    private val fullScreenFlags =
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

    private val normalFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE

    var isDarkMode = false
    var currentPage = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        pdfPath = intent.getStringExtra("pdfPath").toString()
        pdfName = intent.getStringExtra("pdfName").toString()

        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt("CURRENT_PAGE", 0)
        }


        binding.ivBack.setOnClickListener {
            finish()
        }

        if (pdfName.isNotEmpty() && pdfPath.isNotEmpty()) {
            setupPdfView()
        }


    }


    private fun setupPdfView() {

        binding.title.text = resizeName(pdfName)


        val file = File(pdfPath)
        binding.customPdfView.fromFile(file).onTap {
            if (binding.toolbar.isVisible) {
                hideActionUI()
            } else {
                showActionUI()
            }
            true
        }.enableSwipe(true).swipeHorizontal(false).enableDoubletap(true).defaultPage(currentPage)
            .enableAnnotationRendering(true).password(null).scrollHandle(DefaultScrollHandle(this))
            .enableAntialiasing(true).spacing(0).load()


        binding.darkModeAction.setOnClickListener {
            isDarkMode = if (isDarkMode) {
                binding.customPdfView.setNightMode(false)
                binding.customPdfView.setBackgroundColor(resources.getColor(R.color.white))
                binding.darkModeAction.setImageResource(R.drawable.ic_dark_mode)
                false
            } else {
                binding.customPdfView.setNightMode(true)
                binding.customPdfView.setBackgroundColor(resources.getColor(R.color.dark_backgroundColor))
                binding.darkModeAction.setImageResource(R.drawable.ic_light_mode)
                true
            }

        }


        binding.rotateAction.setOnClickListener {

            requestedOrientation = if (resources.configuration.orientation == 1) {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }

        }

        binding.brightnessAction.setOnClickListener {
            showBrightnessDialog()
        }


        binding.swipeAction.setOnClickListener {
            if (binding.customPdfView.isPageFlingEnabled) {
                binding.customPdfView.setPageFling(false)
                binding.swipeAction.setImageResource(R.drawable.ic_multi_swipe)
                Toast.makeText(this, "Multi Swipe Enabled", Toast.LENGTH_SHORT).show()
            } else {
                binding.customPdfView.setPageFling(true)
                binding.swipeAction.setImageResource(R.drawable.ic_single_swipe)
                Toast.makeText(this, "Single Swipe Enabled", Toast.LENGTH_SHORT).show()
            }
        }

        binding.jumpAction.setOnClickListener {
            showJumpDialog()
        }


    }

    private fun showJumpDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val dialogBinding: BottomSheetJumpBinding =
            BottomSheetJumpBinding.inflate(LayoutInflater.from(this))

        bottomSheetDialog.setContentView(dialogBinding.root)
        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }

        val totalPage = binding.customPdfView.pageCount
        val currentPage = binding.customPdfView.currentPage + 1

        dialogBinding.pageNumbers.text = "($currentPage - $totalPage)"

        dialogBinding.cancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        dialogBinding.editText.setText(currentPage.toString())

        dialogBinding.jump.setOnClickListener {
            val pageNumber = dialogBinding.editText.text.toString().toInt()
            if (pageNumber in 1..totalPage) {
                binding.customPdfView.jumpTo(pageNumber - 1, true)
                bottomSheetDialog.dismiss()
            } else {
                Toast.makeText(this, "Invalid Page Number", Toast.LENGTH_SHORT).show()
            }
        }

        bottomSheetDialog.show()

    }

    private fun showBrightnessDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val dialogBinding: BottomSheetBrightnessBinding =
            BottomSheetBrightnessBinding.inflate(LayoutInflater.from(this))

        bottomSheetDialog.setContentView(dialogBinding.root)
        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            setDimAmount(0f)
        }

        // Get current window brightness
        val layoutParams = window.attributes
        val currentBrightness = if (layoutParams.screenBrightness < 0) { // Use system brightness
            android.provider.Settings.System.getInt(
                contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS
            ).toFloat() / 255
        } else {
            layoutParams.screenBrightness
        }
        dialogBinding.brightnessSeekBar.progress = (currentBrightness * 100).toInt()

        // Update window brightness when SeekBar value changes
        dialogBinding.brightnessSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val brightness = progress.toFloat() / 100
                layoutParams.screenBrightness = brightness
                window.attributes = layoutParams
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        bottomSheetDialog.show()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("CURRENT_PAGE", binding.customPdfView.currentPage)
    }


    fun showActionUI() {
        binding.toolbar.visibility = View.VISIBLE
        binding.bottomToolbar.visibility = View.VISIBLE
        window.decorView.systemUiVisibility = normalFlags
    }

    fun hideActionUI() {
        binding.toolbar.visibility = View.GONE
        binding.bottomToolbar.visibility = View.GONE
        window.decorView.systemUiVisibility = fullScreenFlags
    }

}