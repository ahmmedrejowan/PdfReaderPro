package com.androvine.pdfreaderpro.adapter

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.androvine.pdfreaderpro.R
import com.androvine.pdfreaderpro.dataClasses.PdfFile
import com.androvine.pdfreaderpro.databinding.BottomSheetMenuFilesBinding
import com.androvine.pdfreaderpro.databinding.DialogDeleteFilesBinding
import com.androvine.pdfreaderpro.databinding.SinglePdfItemFileBinding
import com.androvine.pdfreaderpro.databinding.SinglePdfItemFileGridBinding
import com.androvine.pdfreaderpro.diffUtils.PdfFileDiffCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfAdapter(
    private val pdfFiles: MutableList<PdfFile>,
    var isGridView: Boolean = false,
    private val recyclerView: RecyclerView
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_GRID = 1
        private const val VIEW_TYPE_LIST = 2
    }

    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8

    private val thumbnailCache: LruCache<String, Bitmap> = LruCache(cacheSize)


    inner class GridViewHolder(private val binding: SinglePdfItemFileGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var viewHolderJob = Job()
        private var viewHolderScope = CoroutineScope(Dispatchers.Main + viewHolderJob)

        fun bind(pdfFile: PdfFile) {
            binding.fileName.text = resizeName(pdfFile.name)
            binding.date.text = formattedDate(pdfFile.dateModified)
            binding.size.text = formattedFileSize(pdfFile.size)

            viewHolderJob = Job()
            viewHolderScope = CoroutineScope(Dispatchers.Main + viewHolderJob)

            binding.ivOption.setOnClickListener {
                showOptionsDialog(it.context, pdfFile)
            }

            loadThumbnail(pdfFile.path)
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
            //    viewHolderScope.cancel() // cancel ongoing thumbnail generation if any
            binding.fileIcon.setImageResource(R.drawable.ic_pdf_file) // set placeholder
        }
    }


    inner class ListViewHolder(private val binding: SinglePdfItemFileBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var viewHolderJob = Job()
        private var viewHolderScope = CoroutineScope(Dispatchers.Main + viewHolderJob)

        fun bind(pdfFile: PdfFile) {
            binding.fileName.text = resizeName(pdfFile.name)
            binding.date.text = formattedDate(pdfFile.dateModified)
            binding.size.text = formattedFileSize(pdfFile.size)

            viewHolderJob = Job()
            viewHolderScope = CoroutineScope(Dispatchers.Main + viewHolderJob)

            binding.ivOption.setOnClickListener {
                showOptionsDialog(it.context, pdfFile)
            }

            loadThumbnail(pdfFile.path)
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
            //    viewHolderScope.cancel() // cancel ongoing thumbnail generation if any
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
                val binding = SinglePdfItemFileGridBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                GridViewHolder(binding)
            }

            VIEW_TYPE_LIST -> {
                val binding = SinglePdfItemFileBinding.inflate(
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

    fun updatePdfFiles(newPdfFiles: List<PdfFile>) {

        thumbnailCache.evictAll() // clear thumbnail cache

        val diffCallback = PdfFileDiffCallback(pdfFiles, newPdfFiles)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        pdfFiles.clear()
        pdfFiles.addAll(newPdfFiles)

        diffResult.dispatchUpdatesTo(this)
        recyclerView.layoutManager?.scrollToPosition(0)

    }

    private fun formattedDate(lastModified: Long): String {
        val date = Date(lastModified * 1000)
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        return sdf.format(date)
    }

    private fun showOptionsDialog(context: Context, pdfFile: PdfFile) {
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

        val name = pdfFile.name
        val path = pdfFile.path

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

        optionBinding.optionDelete.setOnClickListener {
            bottomSheetDialog.dismiss()
            showDeleteDialog(context, pdfFile)
        }

        bottomSheetDialog.show()

    }

    private fun showDeleteDialog(context: Context, pdfFile: PdfFile) {

        val dialog = Dialog(context)
        val dialogBinding : DialogDeleteFilesBinding = DialogDeleteFilesBinding.inflate(
            LayoutInflater.from(context)
        )
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window!!.setLayout(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.fileName.text = pdfFile.name
        dialogBinding.fileSize.text = formattedFileSize(pdfFile.size)
        dialogBinding.filePath.text = pdfFile.path.substringBeforeLast("/")



        dialog.show()

    }

    private fun formattedFileSize(length: Long): String {
        // to kb, mb , gb
        val kb = 1024
        val mb = kb * 1024
        val gb = mb * 1024
        return if (length >= gb) {
            String.format("%.1f GB", length / gb.toFloat())
        } else if (length >= mb) {
            String.format("%.1f MB", length / mb.toFloat())
        } else if (length >= kb) {
            String.format("%.1f KB", length / kb.toFloat())
        } else {
            String.format("%d B", length)
        }
    }

    private fun resizeName(s: String): String {
        if (s.length <= 32) {
            return s
        }
        val first = s.substring(0, 18)
        val last = s.substring(s.length - 10, s.length)
        return "$first...$last"
    }

    suspend fun generateThumbnail(pdfFilePath: String): Bitmap? = withContext(Dispatchers.IO) {
        var pdfRenderer: PdfRenderer? = null
        var currentPage: PdfRenderer.Page? = null
        try {
            val file = File(pdfFilePath)
            if (!file.exists()) {
                return@withContext null
            }

            val fileDescriptor =
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(fileDescriptor)

            // Use the first page to generate the thumbnail
            currentPage = pdfRenderer.openPage(0)
            val bitmap: Bitmap = Bitmap.createBitmap(
                currentPage.width / 4, currentPage.height / 4, Bitmap.Config.ARGB_8888
            )
            currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            bitmap
        } catch (ex: OutOfMemoryError) {
            // Log.e("ThumbnailGenerator", "Memory issue generating thumbnail", ex)
            null
        } catch (ex: Exception) {
            //  Log.e("ThumbnailGenerator", "Error generating thumbnail", ex)
            null
        } finally {
            currentPage?.close()
            pdfRenderer?.close()
        }
    }

    private fun generateNormalThumbnail(pdfFilePath: String): Bitmap? {
        var pdfRenderer: PdfRenderer? = null
        var currentPage: PdfRenderer.Page? = null
        try {
            val file = File(pdfFilePath)
            if (!file.exists()) {
                return null
            }

            val fileDescriptor =
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(fileDescriptor)

            // Use the first page to generate the thumbnail
            currentPage = pdfRenderer.openPage(0)
            val bitmap: Bitmap = Bitmap.createBitmap(
                currentPage.width / 4, currentPage.height / 4, Bitmap.Config.ARGB_8888
            )
            currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            return bitmap
        } catch (ex: OutOfMemoryError) {
            // Log.e("ThumbnailGenerator", "Memory issue generating thumbnail", ex)
            return null
        } catch (ex: Exception) {
            //  Log.e("ThumbnailGenerator", "Error generating thumbnail", ex)
            return null
        } finally {
            currentPage?.close()
            pdfRenderer?.close()
        }
    }

    private fun extractParentFolders(fullPath: String): String {
        val path: String = if (fullPath.startsWith("/")) {
            fullPath.substring(1)
        } else {
            fullPath
        }
        val parts = path.split("/")
        if (parts.size < 4) return "Storage"
        val relevantParts = parts.drop(3).dropLast(1)
        return relevantParts.joinToString(" > ")
    }

}