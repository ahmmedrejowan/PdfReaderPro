package com.androvine.pdfreaderpro.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.androvine.pdfreaderpro.dataClasses.PdfFile
import com.androvine.pdfreaderpro.databinding.SinglePdfItemFileBinding
import com.androvine.pdfreaderpro.utils.PdfFileDiffCallback

class PdfAdapter(private val pdfFiles: MutableList<PdfFile>) :
    RecyclerView.Adapter<PdfAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            SinglePdfItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val pdfFile = pdfFiles[position]

        holder.binding.apply {
            fileName.text = resizeName(pdfFile.name)
            date.text = formattedDate(pdfFile.dateModified)
            size.text = formattedFileSize(pdfFile.size)
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

    @SuppressLint("SimpleDateFormat")
    private fun formattedDate(lastModified: Long): String {
        // to day/month/year
        val date = java.util.Date(lastModified)
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy")
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

    class ViewHolder(val binding: SinglePdfItemFileBinding) : RecyclerView.ViewHolder(binding.root)

}