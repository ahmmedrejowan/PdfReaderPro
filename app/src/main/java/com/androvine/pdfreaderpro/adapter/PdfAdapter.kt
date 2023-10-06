package com.androvine.pdfreaderpro.adapter

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.androvine.pdfreaderpro.R
import com.androvine.pdfreaderpro.dataClasses.PdfFile
import com.androvine.pdfreaderpro.databinding.BottomSheetMenuFilesBinding
import com.androvine.pdfreaderpro.databinding.DialogDeleteFilesBinding
import com.androvine.pdfreaderpro.databinding.DialogInfoFilesBinding
import com.androvine.pdfreaderpro.databinding.DialogRenameFilesBinding
import com.androvine.pdfreaderpro.databinding.SinglePdfItemFileBinding
import com.androvine.pdfreaderpro.databinding.SinglePdfItemFileGridBinding
import com.androvine.pdfreaderpro.diffUtils.PdfFileDiffCallback
import com.androvine.pdfreaderpro.utils.FormattingUtils
import com.androvine.pdfreaderpro.utils.FormattingUtils.Companion.extractParentFolders
import com.androvine.pdfreaderpro.utils.FormattingUtils.Companion.formattedDate
import com.androvine.pdfreaderpro.utils.FormattingUtils.Companion.formattedFileSize
import com.androvine.pdfreaderpro.utils.FormattingUtils.Companion.generateNormalThumbnail
import com.androvine.pdfreaderpro.utils.FormattingUtils.Companion.generateThumbnail
import com.androvine.pdfreaderpro.utils.FormattingUtils.Companion.resizeName
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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

            binding.fileLayout.setOnLongClickListener {
                showOptionsDialog(it.context, pdfFile)
                true
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

            binding.fileLayout.setOnLongClickListener {
                showOptionsDialog(it.context, pdfFile)
                true
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

        optionBinding.optionInfo.setOnClickListener {
            bottomSheetDialog.dismiss()
            showInfoDialog(context, pdfFile)
        }

        optionBinding.optionRename.setOnClickListener {
            bottomSheetDialog.dismiss()
            showRenameDialog(context, pdfFile)
        }

        bottomSheetDialog.show()

    }

    private fun showDeleteDialog(context: Context, pdfFile: PdfFile) {

        val dialog = Dialog(context)
        val dialogBinding: DialogDeleteFilesBinding = DialogDeleteFilesBinding.inflate(
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

    private fun showRenameDialog(context: Context, pdfFile: PdfFile) {

        val dialog = Dialog(context)
        val dialogBinding: DialogRenameFilesBinding = DialogRenameFilesBinding.inflate(
            LayoutInflater.from(context)
        )
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window!!.setLayout(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        )


        val oldName = pdfFile.name
        var finalNewName = ""
        var hasError = true

        oldName.let {
            dialogBinding.renameEditText.setText(it)
            dialogBinding.renameEditText.setSelection(it.length)
        }

        dialogBinding.renameEditText.addTextChangedListener {
            // show error if name is empty
            if (it.toString().isEmpty()) {
                dialogBinding.renameEditText.error = "Name cannot be empty"
                hasError = true
                return@addTextChangedListener
            }

            // show error if name contains invalid characters
            if (it.toString().contains("[\\\\/:*?\"<>|]".toRegex())) {
                dialogBinding.renameEditText.error = "Name cannot contain special characters"
                hasError = true
                return@addTextChangedListener
            }


            // show error if name already exists in the same folder with same extension
            val pdfFolderPath = pdfFile.path.substringBeforeLast("/")
            val newName = "$pdfFolderPath/${it.toString()}.pdf"
            val file = File(newName)
            if (file.exists() && newName != pdfFile.path) {
                dialogBinding.renameEditText.error = "File already exists"
                hasError = true
                return@addTextChangedListener
            }


            // if no error, set newName
            dialogBinding.renameEditText.error = null
            finalNewName = it.toString()
            hasError = false

        }

        dialogBinding.cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.rename.setOnClickListener {
            if (!hasError) {

            } else {
                Toast.makeText(context, "Please enter a valid name", Toast.LENGTH_SHORT).show()
            }
        }


        dialog.show()

    }


    fun showInfoDialog(context: Context, pdfFile: PdfFile) {

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

        dialogBinding.fileName.text = pdfFile.name
        dialogBinding.fileSize.text = formattedFileSize(pdfFile.size)
        dialogBinding.filePath.text = pdfFile.path.substringBeforeLast("/")
        dialogBinding.lastModified.text = formattedDate(pdfFile.dateModified)
        dialogBinding.pages.text = FormattingUtils.getPdfPageCount(pdfFile.path).toString()


        dialogBinding.dismiss.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }


}