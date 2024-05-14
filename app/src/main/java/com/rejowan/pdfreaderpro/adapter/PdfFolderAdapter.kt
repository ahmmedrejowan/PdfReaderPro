package com.rejowan.pdfreaderpro.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.rejowan.pdfreaderpro.dataClasses.PdfFolder
import com.rejowan.pdfreaderpro.databinding.DialogInfoFolderBinding
import com.rejowan.pdfreaderpro.databinding.SingleFolderItemBinding
import com.rejowan.pdfreaderpro.databinding.SingleFolderItemGridBinding
import com.rejowan.pdfreaderpro.diffUtils.PdfFolderDiffCallback
import com.rejowan.pdfreaderpro.interfaces.OnPdfFolderClicked
import com.rejowan.pdfreaderpro.utils.FormattingUtils

@SuppressLint("SetTextI18n")
class PdfFolderAdapter(
    private val pdfFolders: MutableList<PdfFolder>,
    var isGridView: Boolean = false,
    var onPdfFolderClicked: OnPdfFolderClicked
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_GRID = 1
        private const val VIEW_TYPE_LIST = 2
    }


    inner class GridViewHolder(private val binding: SingleFolderItemGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pdfFolder: PdfFolder) {
            binding.folderName.text = resizeName(pdfFolder.name)
            binding.fileCount.text = pdfFolder.pdfFiles.size.toString() + " files"

            binding.root.setOnClickListener {
                onPdfFolderClicked.onPdfFolderClicked(pdfFolder.name)
            }

            binding.ivOption.setOnClickListener {
                showInfoDialog(binding.root.context, pdfFolder)
            }

        }

    }

    inner class ListViewHolder(private val binding: SingleFolderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pdfFolder: PdfFolder) {
            binding.folderName.text = resizeName(pdfFolder.name)
            binding.fileCount.text = pdfFolder.pdfFiles.size.toString() + " files"

            binding.root.setOnClickListener {
                onPdfFolderClicked.onPdfFolderClicked(pdfFolder.name)
            }

            binding.ivOption.setOnClickListener {
                showInfoDialog(binding.root.context, pdfFolder)
            }

        }

    }


    override fun getItemViewType(position: Int): Int {
        return if (isGridView) VIEW_TYPE_GRID else VIEW_TYPE_LIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_GRID -> {
                val binding = SingleFolderItemGridBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                GridViewHolder(binding)
            }

            VIEW_TYPE_LIST -> {
                val binding = SingleFolderItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ListViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val pdfFolder = pdfFolders[position]
        when (holder) {
            is GridViewHolder -> holder.bind(pdfFolder)
            is ListViewHolder -> holder.bind(pdfFolder)
        }
    }


    override fun getItemCount(): Int {
        return pdfFolders.size
    }


    private fun resizeName(s: String): String {
        if (s.length <= 32) {
            return s
        }
        val first = s.substring(0, 18)
        val last = s.substring(s.length - 10, s.length)
        return "$first...$last"
    }

    fun updateList(newPdfFolders: List<PdfFolder>) {
        val diffCallback = PdfFolderDiffCallback(pdfFolders, newPdfFolders)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        pdfFolders.clear()
        pdfFolders.addAll(newPdfFolders)
        diffResult.dispatchUpdatesTo(this)
    }


    private fun showInfoDialog(context: Context, pdfFolder: PdfFolder) {
        val dialog = Dialog(context)
        val dialogBinding: DialogInfoFolderBinding = DialogInfoFolderBinding.inflate(
            LayoutInflater.from(context)
        )
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window!!.setLayout(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.folderName.text = pdfFolder.name
        dialogBinding.fileSize.text = FormattingUtils.formattedFileSize(pdfFolder.pdfFiles.sumOf { it.size })
        dialogBinding.pdfCount.text = pdfFolder.pdfFiles.size.toString()
        dialogBinding.folderPath.text = pdfFolder.pdfFiles[0].path.substringBeforeLast("/")


        dialogBinding.dismiss.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


}