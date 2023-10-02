package com.androvine.pdfreaderpro.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.androvine.pdfreaderpro.dataClasses.PdfFile
import com.androvine.pdfreaderpro.databinding.SinglePdfItemFileBinding
import com.androvine.pdfreaderpro.databinding.SinglePdfItemFileGridBinding
import com.androvine.pdfreaderpro.utils.PdfFileDiffCallback
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfAdapter(
    private val pdfFiles: MutableList<PdfFile>, var isGridView: Boolean = false
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_GRID = 1
        private const val VIEW_TYPE_LIST = 2
    }

    inner class GridViewHolder(private val binding: SinglePdfItemFileGridBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pdfFile: PdfFile) {
            binding.fileName.text = resizeName(pdfFile.name)
            binding.date.text = formattedDate(pdfFile.dateModified)
            binding.size.text = formattedFileSize(pdfFile.size)
        }
    }

    inner class ListViewHolder(private val binding: SinglePdfItemFileBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pdfFile: PdfFile) {
            binding.fileName.text = resizeName(pdfFile.name)
            binding.date.text = formattedDate(pdfFile.dateModified)
            binding.size.text = formattedFileSize(pdfFile.size)

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
        val diffCallback = PdfFileDiffCallback(pdfFiles, newPdfFiles)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        pdfFiles.clear()
        pdfFiles.addAll(newPdfFiles)

        diffResult.dispatchUpdatesTo(this)
    }

    private fun formattedDate(lastModified: Long): String {
        val date = Date(lastModified * 1000)
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        return sdf.format(date)
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


}