package com.rejowan.pdfreaderpro.activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.rejowan.pdfreaderpro.R
import com.rejowan.pdfreaderpro.dataClasses.PdfFile
import com.rejowan.pdfreaderpro.dataClasses.RecentModel
import com.rejowan.pdfreaderpro.database.FavoriteDBHelper
import com.rejowan.pdfreaderpro.database.RecentDBHelper
import com.rejowan.pdfreaderpro.databinding.ActivityPdfreaderBinding
import com.rejowan.pdfreaderpro.databinding.BottomSheetBrightnessBinding
import com.rejowan.pdfreaderpro.databinding.BottomSheetJumpBinding
import com.rejowan.pdfreaderpro.databinding.DialogFavoriteRemoveFilesBinding
import com.rejowan.pdfreaderpro.databinding.PopupMenuReaderBinding
import com.rejowan.pdfreaderpro.interfaces.OnPdfFileClicked
import com.rejowan.pdfreaderpro.utils.DialogUtils
import com.rejowan.pdfreaderpro.utils.FormattingUtils
import com.rejowan.pdfreaderpro.utils.FormattingUtils.Companion.resizeName
import com.rejowan.pdfreaderpro.vms.PdfListViewModel
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

@Suppress("DEPRECATION")
class PDFReader : AppCompatActivity() {

    private val binding: ActivityPdfreaderBinding by lazy {
        ActivityPdfreaderBinding.inflate(layoutInflater)
    }

    private var pdfPath: String? = null
    private var pdfName: String? = null
    private var pdfUri: Uri? = null
    private var isOutside = false


    private val fullScreenFlags =
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

    private val normalFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE

    var isDarkMode = false
    var currentPage = 0

    private val pdfListViewModel: PdfListViewModel by viewModel()
    var pdfFile: PdfFile? = null

    private lateinit var recentDBHelper: RecentDBHelper
    var recentModel: RecentModel? = null

    private lateinit var favoriteDBHelper: FavoriteDBHelper

    var isPDFDarkEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        pdfPath = intent.getStringExtra("pdfPath")
        pdfName = intent.getStringExtra("pdfName")

        pdfUri = intent.parcelable("pdfUri")
        isOutside = intent.getBooleanExtra("isOutside", false)

        Log.e("Intent", "pdf path: $pdfPath")
        Log.e("Intent", "pdf uri: $pdfUri")

        recentDBHelper = RecentDBHelper(this)
        favoriteDBHelper = FavoriteDBHelper(this)

        isPDFDarkEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_dark_pdf", false)
        isDarkMode = isPDFDarkEnabled
        if (isPDFDarkEnabled){
            binding.customPdfView.setBackgroundColor(
                ContextCompat.getColor(
                    this, R.color.dark_backgroundColor
                )
            )
            binding.darkModeAction.setImageResource(R.drawable.ic_light_mode)
        }


        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt("CURRENT_PAGE", 0)
        }

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }


        if (pdfName != null && pdfPath != null) {
            pdfListViewModel.pdfFiles.observe(this) { pdfFiles ->
                pdfFile = pdfFiles.find { it.path == pdfPath }
            }
            setupPdfViewWithFile()
        } else if (pdfUri != null) {
            setupPdfViewWithUri()
        } else {
            Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show()
            finish()
        }


    }

    private fun setupPdfViewWithUri() {

        binding.customPdfView
            .fromUri(pdfUri)
            .onTap {
                if (binding.toolbar.isVisible) {
                    hideActionUI()
                } else {
                    showActionUI()
                }
                true
            }
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .defaultPage(currentPage)
            .enableAnnotationRendering(true)
            .password(null)
            .scrollHandle(DefaultScrollHandle(this))
            .enableAntialiasing(true)
            .nightMode(isPDFDarkEnabled)
            .spacing(0)
            .load()

        val contentResolver = contentResolver
        val projection = {
            arrayOf(
                android.provider.MediaStore.Files.FileColumns._ID,
                android.provider.MediaStore.Files.FileColumns.DISPLAY_NAME,
                android.provider.MediaStore.Files.FileColumns.SIZE,
                android.provider.MediaStore.Files.FileColumns.DATE_ADDED
            )
        }
        val cursor = contentResolver.query(
            pdfUri!!,
            projection(),
            null,
            null,
            null
        )
        var title: String? = null

        if (cursor != null) {
            cursor.moveToFirst()

            title =
                cursor.getString(cursor.getColumnIndexOrThrow(android.provider.MediaStore.Files.FileColumns.DISPLAY_NAME))

            cursor.close()
        }

        binding.title.text = resizeName(title!!)


        binding.ivOption.visibility = View.GONE

        setUpViewActions()

    }

    private fun setUpViewActions() {
        binding.darkModeAction.setOnClickListener {
            isDarkMode = if (isDarkMode) {
                binding.customPdfView.setNightMode(false)
                binding.customPdfView.setBackgroundColor(
                    ContextCompat.getColor(
                        this, R.color.light_backgroundColor
                    )
                )
                binding.darkModeAction.setImageResource(R.drawable.ic_dark_mode)
                false
            } else {
                binding.customPdfView.setNightMode(true)
                binding.customPdfView.setBackgroundColor(
                    ContextCompat.getColor(
                        this, R.color.dark_backgroundColor
                    )
                )
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


    private fun setupPdfViewWithFile() {

        val file = File(pdfPath!!)

        if (recentDBHelper.checkIfExists(pdfPath!!)) {
            recentModel = recentDBHelper.getGetRecentByPath(pdfPath!!)!!
        } else {
            recentModel = RecentModel(
                name = file.name.replace(".pdf", ""),
                path = file.path,
                size = file.length(),
                lastOpenedDate = System.currentTimeMillis(),
                totalPageCount = binding.customPdfView.pageCount,
                lastPageOpened = 0
            )
            recentDBHelper.addRecentItem(recentModel!!)
        }


        binding.title.text = resizeName(pdfName!!)

        if (recentModel!!.lastPageOpened != 0) {
            currentPage = recentModel!!.lastPageOpened
        }

        binding.customPdfView
            .fromFile(file)
            .onTap {
                if (binding.toolbar.isVisible) {
                    hideActionUI()
                } else {
                    showActionUI()
                }
                true
            }
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .defaultPage(currentPage)
            .nightMode(isPDFDarkEnabled)
            .enableAnnotationRendering(true)
            .password(null)
            .scrollHandle(DefaultScrollHandle(this))
            .enableAntialiasing(true)
            .spacing(0)
            .load()



        binding.ivOption.setOnClickListener {
            showOptionPopup()
        }

        setUpViewActions()


    }

    private fun showOptionPopup() {


        val popupBinding = PopupMenuReaderBinding.inflate(
            LayoutInflater.from(this)
        )
        val popupWindow = PopupWindow(
            popupBinding.root,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        val isFavorite = favoriteDBHelper.checkIfExists(pdfFile!!.path)
        if (isFavorite) {
            popupBinding.optionFavorite.text = "Remove from favorites"
            popupBinding.optionFavorite.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_no_favorite,
                0,
                0,
                0
            )
        } else {
            popupBinding.optionFavorite.text = "Add to favorites"
            popupBinding.optionFavorite.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_favorite,
                0,
                0,
                0
            )
        }

        popupBinding.optionInfo.setOnClickListener {

            if (pdfFile != null) {
                DialogUtils.showInfoDialog(this, pdfFile!!)
            } else {
                Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show()
            }


            popupWindow.dismiss()
        }

        popupBinding.optionShare.setOnClickListener {
            if (pdfFile != null) {
                DialogUtils.sharePDF(this, pdfFile!!)
            } else {
                Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show()
            }
            popupWindow.dismiss()
        }

        popupBinding.optionDelete.setOnClickListener {

            if (pdfFile != null) {
                DialogUtils.showDeleteDialog(this, pdfFile!!, object : OnPdfFileClicked {
                    override fun onPdfFileRenamed(pdfFile: PdfFile, newName: String) {
                        // Do nothing
                    }

                    override fun onPdfFileDeleted(pdfFile: PdfFile) {
                        pdfListViewModel.deletePdfFile(pdfFile)

                        if (recentDBHelper.checkIfExists(pdfFile.path)) {
                            recentDBHelper.deleteRecentItem(pdfFile.path)
                        }

                        if (favoriteDBHelper.checkIfExists(pdfFile.path)) {
                            favoriteDBHelper.deleteFavorite(pdfFile.path)
                        }

                        finish()
                    }

                })
            } else {
                Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show()
            }
            popupWindow.dismiss()
        }

        popupBinding.optionFavorite.setOnClickListener {

            if (isFavorite) {
                showFavoriteDialog(this, favoriteDBHelper, pdfFile!!)
            } else {
                favoriteDBHelper.addFavoriteItem(pdfFile!!)
                Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
            }

            popupWindow.dismiss()
        }



        popupWindow.isOutsideTouchable = true
        popupWindow.showAsDropDown(binding.ivOption)

    }


    private fun showFavoriteDialog(
        context: Context,
        favoriteDBHelper: FavoriteDBHelper,
        pdfFile: PdfFile
    ) {

        val dialog = Dialog(context)
        val dialogBinding: DialogFavoriteRemoveFilesBinding =
            DialogFavoriteRemoveFilesBinding.inflate(
                LayoutInflater.from(context)
            )
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window!!.setLayout(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        )


        dialogBinding.fileName.text = pdfFile.name
        dialogBinding.fileSize.text = FormattingUtils.formattedFileSize(pdfFile.size)
        dialogBinding.filePath.text = pdfFile.path.substringBeforeLast("/")

        dialogBinding.cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.remove.setOnClickListener {
            dialog.dismiss()
            favoriteDBHelper.deleteFavorite(pdfFile.path)
        }

        dialog.show()

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

        dialogBinding.editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val pageNumber = dialogBinding.editText.text.toString().toInt()
                if (pageNumber in 1..totalPage) {
                    binding.customPdfView.jumpTo(pageNumber - 1, true)
                    bottomSheetDialog.dismiss()
                } else {
                    Toast.makeText(this@PDFReader, "Invalid Page Number", Toast.LENGTH_SHORT).show()
                }
            }
            false
        }

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

    override fun onDestroy() {


        if (recentModel != null) {
            recentModel!!.totalPageCount = binding.customPdfView.pageCount
            recentModel!!.lastPageOpened = binding.customPdfView.currentPage
            recentModel!!.lastOpenedDate = System.currentTimeMillis()
            recentDBHelper.updateRecent(recentModel!!)
        }



        super.onDestroy()

    }

    override fun onBackPressed() {
        if (isOutside) {
            startActivity(Intent(this, Home::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
            finish()
        } else {
            super.onBackPressed()
        }
    }

}