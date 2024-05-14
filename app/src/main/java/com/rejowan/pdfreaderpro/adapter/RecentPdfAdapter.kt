package com.rejowan.pdfreaderpro.adapter

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.rejowan.pdfreaderpro.R
import com.rejowan.pdfreaderpro.activities.PDFReader
import com.rejowan.pdfreaderpro.dataClasses.RecentModel
import com.rejowan.pdfreaderpro.databinding.BottomSheetMenuFilesBinding
import com.rejowan.pdfreaderpro.databinding.DialogInfoFilesBinding
import com.rejowan.pdfreaderpro.databinding.DialogRecentRemoveFilesBinding
import com.rejowan.pdfreaderpro.databinding.SinglePdfItemFileRecentBinding
import com.rejowan.pdfreaderpro.databinding.SinglePdfItemFileRecentGridBinding
import com.rejowan.pdfreaderpro.diffUtils.RecentPdfFileDiffCallback
import com.rejowan.pdfreaderpro.interfaces.OnRecentClicked
import com.rejowan.pdfreaderpro.utils.DialogUtils.Companion.sharePDF
import com.rejowan.pdfreaderpro.utils.FormattingUtils
import com.rejowan.pdfreaderpro.utils.FormattingUtils.Companion.extractParentFolders
import com.rejowan.pdfreaderpro.utils.FormattingUtils.Companion.formattedFileSize
import com.rejowan.pdfreaderpro.utils.FormattingUtils.Companion.generateNormalThumbnail
import com.rejowan.pdfreaderpro.utils.FormattingUtils.Companion.generateThumbnail
import com.rejowan.pdfreaderpro.utils.FormattingUtils.Companion.resizeName
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecentPdfAdapter(
    private val pdfFiles: MutableList<RecentModel>,
    var isGridView: Boolean = false,
    private val recyclerView: RecyclerView,
    private val onRecentClicked: OnRecentClicked
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_GRID = 1
        private const val VIEW_TYPE_LIST = 2
    }

    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8
    private val thumbnailCache: LruCache<String, Bitmap> = LruCache(cacheSize)


    inner class GridViewHolder(private val binding: SinglePdfItemFileRecentGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var viewHolderJob = Job()
        private var viewHolderScope = CoroutineScope(Dispatchers.Main + viewHolderJob)

        fun bind(recentModel: RecentModel) {
            binding.fileName.text = resizeName(recentModel.name)
            binding.size.text = formattedFileSize(recentModel.size)
            binding.pages.text = recentModel.totalPageCount.toString() + " pages"

            viewHolderJob = Job()
            viewHolderScope = CoroutineScope(Dispatchers.Main + viewHolderJob)

            binding.ivOption.setOnClickListener {
                showOptionsDialog(it.context, recentModel)
            }

            binding.fileLayout.setOnLongClickListener {
                showOptionsDialog(it.context, recentModel)
                true
            }

            binding.fileLayout.setOnClickListener {
                openPDF(it.context, recentModel)
            }

            val totalPage = recentModel.totalPageCount
            val currentPage = recentModel.lastPageOpened

            binding.progressBar.max = totalPage
            binding.progressBar.progress = currentPage + 1



            loadThumbnail(recentModel.path)
        }

        private fun loadThumbnail(pdfFilePath: String) {
            viewHolderScope.launch {
                try {
                    var thumbnail = thumbnailCache.get(pdfFilePath)
                    if (thumbnail == null) {
                        thumbnail = withContext(Dispatchers.IO) {
                            generateThumbnail(pdfFilePath)
                        }
                        if (thumbnail != null) {
                            thumbnailCache.put(pdfFilePath, thumbnail)
                        }
                    }

                    if (thumbnail != null) {
                        binding.fileIcon.setImageBitmap(thumbnail)
                    } else {
                        binding.fileIcon.setImageResource(R.drawable.ic_pdf_file)
                    }


                } catch (e: Exception) {
                    binding.fileIcon.setImageResource(R.drawable.ic_pdf_file)
                }
            }
        }


        fun clear() {
            viewHolderJob.cancel()
            binding.fileIcon.setImageResource(R.drawable.ic_pdf_file) // set placeholder
        }
    }


    inner class ListViewHolder(private val binding: SinglePdfItemFileRecentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var viewHolderJob = Job()
        private var viewHolderScope = CoroutineScope(Dispatchers.Main + viewHolderJob)

        fun bind(recentModel: RecentModel) {
            binding.fileName.text = resizeName(recentModel.name)
            binding.size.text = formattedFileSize(recentModel.size)
            binding.pages.text = recentModel.totalPageCount.toString() + " pages"

            viewHolderJob = Job()
            viewHolderScope = CoroutineScope(Dispatchers.Main + viewHolderJob)

            binding.ivOption.setOnClickListener {
                showOptionsDialog(it.context, recentModel)
            }

            binding.fileLayout.setOnLongClickListener {
                showOptionsDialog(it.context, recentModel)
                true
            }

            binding.fileLayout.setOnClickListener {
                openPDF(it.context, recentModel)
            }

            val totalPage = recentModel.totalPageCount
            val currentPage = recentModel.lastPageOpened

            binding.progressBar.max = totalPage
            binding.progressBar.progress = currentPage + 1



            loadThumbnail(recentModel.path)
        }

        private fun loadThumbnail(pdfFilePath: String) {
            viewHolderScope.launch {
                try {
                    var thumbnail = thumbnailCache.get(pdfFilePath)
                    if (thumbnail == null) {
                        thumbnail = withContext(Dispatchers.IO) {
                            generateThumbnail(pdfFilePath)
                        }
                        if (thumbnail != null) {
                            thumbnailCache.put(pdfFilePath, thumbnail)
                        }
                    }
                    if (thumbnail != null) {
                        binding.fileIcon.setImageBitmap(thumbnail)
                    } else {
                        binding.fileIcon.setImageResource(R.drawable.ic_pdf_file)
                    }
                } catch (e: Exception) {
                    binding.fileIcon.setImageResource(R.drawable.ic_pdf_file)
                }
            }
        }


        fun clear() {
            viewHolderJob.cancel()
            binding.fileIcon.setImageResource(R.drawable.ic_pdf_file) // set placeholder
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is GridViewHolder -> holder.clear()
            is ListViewHolder -> holder.clear()
        }
    }


    override fun getItemViewType(position: Int): Int {
        return if (isGridView) VIEW_TYPE_GRID else VIEW_TYPE_LIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_GRID -> {
                val binding = SinglePdfItemFileRecentGridBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                GridViewHolder(binding)
            }

            VIEW_TYPE_LIST -> {
                val binding = SinglePdfItemFileRecentBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ListViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val pdfFile = pdfFiles[position]
        when (holder) {
            is GridViewHolder -> holder.bind(pdfFile)
            is ListViewHolder -> holder.bind(pdfFile)
        }
    }


    override fun getItemCount(): Int {
        return pdfFiles.size
    }

    private fun openPDF(context: Context, pdfFile: RecentModel) {


        context.startActivity(Intent(context, PDFReader::class.java).apply {
            putExtra("pdfName", pdfFile.name)
            putExtra("pdfPath", pdfFile.path)
        })

    }


    fun updatePdfFiles(newRecentFiles: List<RecentModel>) {

        thumbnailCache.evictAll()

        val diffCallback = RecentPdfFileDiffCallback(pdfFiles, newRecentFiles)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        pdfFiles.clear()
        pdfFiles.addAll(newRecentFiles)

        diffResult.dispatchUpdatesTo(this)
        recyclerView.layoutManager?.scrollToPosition(0)

    }


    private fun showOptionsDialog(context: Context, recentModel: RecentModel) {
        val bottomSheetDialog = BottomSheetDialog(context)
        val optionBinding: BottomSheetMenuFilesBinding = BottomSheetMenuFilesBinding.inflate(
            LayoutInflater.from(context)
        )
        bottomSheetDialog.setContentView(optionBinding.root)
        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        bottomSheetDialog.window!!.setLayout(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        )

        val name = recentModel.name
        val path = recentModel.path

        val customFolder = extractParentFolders(path)

        optionBinding.fileName.text = name
        optionBinding.filePath.text = customFolder

        try {
            var thumbnail = thumbnailCache.get(path)
            if (thumbnail == null) {
                thumbnail = generateNormalThumbnail(path)
            }
            if (thumbnail != null) {
                optionBinding.fileIcon.setImageBitmap(thumbnail)
            } else {
                optionBinding.fileIcon.setImageResource(R.drawable.ic_pdf_file)
            }
        } catch (e: Exception) {
            optionBinding.fileIcon.setImageResource(R.drawable.ic_pdf_file)
        }

        optionBinding.optionRename.visibility = View.GONE
        optionBinding.optionDelete.visibility = View.GONE
        optionBinding.optionFavorite.visibility = View.GONE



        optionBinding.optionInfo.setOnClickListener {
            bottomSheetDialog.dismiss()
            showInfoDialogRecent(context, recentModel)
        }

        optionBinding.optionShare.setOnClickListener {
            bottomSheetDialog.dismiss()
            sharePDF(context, recentModel)
        }

        optionBinding.optionRecent.setOnClickListener {
            bottomSheetDialog.dismiss()
            showRecentDialog(context, recentModel)
        }


        bottomSheetDialog.show()

    }

   private fun showInfoDialogRecent(context: Context, recentModel: RecentModel) {
        val dialog = Dialog(context)
        val dialogBinding: DialogInfoFilesBinding = DialogInfoFilesBinding.inflate(
            LayoutInflater.from(context)
        )
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window!!.setLayout(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        )

       dialogBinding.lastModified.visibility = View.GONE

        dialogBinding.fileName.text = recentModel.name
        dialogBinding.fileSize.text = formattedFileSize(recentModel.size)
        dialogBinding.filePath.text = recentModel.path.substringBeforeLast("/")
        dialogBinding.pages.text = FormattingUtils.getPdfPageCount(recentModel.path).toString()


        dialogBinding.dismiss.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showRecentDialog(context: Context, recentModel: RecentModel) {
        val dialog = Dialog(context)
        val dialogBinding: DialogRecentRemoveFilesBinding =
            DialogRecentRemoveFilesBinding.inflate(
                LayoutInflater.from(context)
            )
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window!!.setLayout(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.fileName.text = recentModel.name
        dialogBinding.fileSize.text = formattedFileSize(recentModel.size)
        dialogBinding.filePath.text = recentModel.path.substringBeforeLast("/")

        dialogBinding.cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.remove.setOnClickListener {
            dialog.dismiss()
            onRecentClicked.onRemoveFromRecent(recentModel)
        }


        dialog.show()
    }


}