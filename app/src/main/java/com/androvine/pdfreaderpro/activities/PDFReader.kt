package com.androvine.pdfreaderpro.activities

import android.app.Dialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
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
import com.androvine.pdfreaderpro.R
import com.androvine.pdfreaderpro.dataClasses.PdfFile
import com.androvine.pdfreaderpro.dataClasses.RecentModel
import com.androvine.pdfreaderpro.database.FavoriteDBHelper
import com.androvine.pdfreaderpro.database.RecentDBHelper
import com.androvine.pdfreaderpro.databinding.ActivityPdfreaderBinding
import com.androvine.pdfreaderpro.databinding.BottomSheetBrightnessBinding
import com.androvine.pdfreaderpro.databinding.BottomSheetJumpBinding
import com.androvine.pdfreaderpro.databinding.DialogFavoriteRemoveFilesBinding
import com.androvine.pdfreaderpro.databinding.PopupMenuReaderBinding
import com.androvine.pdfreaderpro.interfaces.OnPdfFileClicked
import com.androvine.pdfreaderpro.utils.DialogUtils
import com.androvine.pdfreaderpro.utils.FormattingUtils
import com.androvine.pdfreaderpro.utils.FormattingUtils.Companion.resizeName
import com.androvine.pdfreaderpro.vms.PdfListViewModel
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
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

    private val pdfListViewModel: PdfListViewModel by viewModel()
    var pdfFile: PdfFile? = null

    private lateinit var recentDBHelper: RecentDBHelper
    lateinit var recentModel: RecentModel

    private lateinit var favoriteDBHelper: FavoriteDBHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        pdfPath = intent.getStringExtra("pdfPath").toString()
        pdfName = intent.getStringExtra("pdfName").toString()

        recentDBHelper = RecentDBHelper(this)
        favoriteDBHelper = FavoriteDBHelper(this)


        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt("CURRENT_PAGE", 0)
        }


        binding.ivBack.setOnClickListener {
            finish()
        }

        if (pdfName.isNotEmpty() && pdfPath.isNotEmpty()) {
            pdfListViewModel.pdfFiles.observe(this) { pdfFiles ->
                pdfFile = pdfFiles.find { it.path == pdfPath }
            }
            setupPdfView()
        }


    }


    private fun setupPdfView() {

        val file = File(pdfPath)

        if (recentDBHelper.checkIfExists(pdfPath)) {
            recentModel = recentDBHelper.getGetRecentByPath(pdfPath)!!
        } else {
            recentModel = RecentModel(
                name = file.name.replace(".pdf", ""),
                path = file.path,
                size = file.length(),
                dateModified = file.lastModified(),
                parentFolderName = getFolderNameFromPath(file.path),
                lastOpenedDate = System.currentTimeMillis(),
                totalPageCount = binding.customPdfView.pageCount,
                lastPageOpened = 0
            )
            recentDBHelper.addRecentItem(recentModel)
        }


        binding.title.text = resizeName(pdfName)

        if (recentModel.lastPageOpened != 0) {
            currentPage = recentModel.lastPageOpened
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
            .enableAnnotationRendering(true)
            .password(null)
            .scrollHandle(DefaultScrollHandle(this))
            .enableAntialiasing(true)
            .spacing(0)
            .load()


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

        binding.ivOption.setOnClickListener {
            showOptionPopup()
        }


    }

    private fun getFolderNameFromPath(path: String): String {
        val folders = path.split("/")
        return folders[folders.size - 2]
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

        dialogBinding.editText.setOnEditorActionListener { v, actionId, event ->
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

        recentModel.totalPageCount = binding.customPdfView.pageCount
        recentModel.lastPageOpened = binding.customPdfView.currentPage
        recentModel.lastOpenedDate = System.currentTimeMillis()
        recentDBHelper.updateRecent(recentModel)

        super.onDestroy()

    }

}