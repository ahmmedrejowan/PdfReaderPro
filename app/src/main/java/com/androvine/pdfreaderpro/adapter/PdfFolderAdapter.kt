package com.androvine.pdfreaderpro.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.androvine.pdfreaderpro.dataClasses.PdfFolder
import com.androvine.pdfreaderpro.databinding.SingleFolderItemBinding
import com.androvine.pdfreaderpro.databinding.SingleFolderItemGridBinding
import com.androvine.pdfreaderpro.interfaces.OnPdfFolderClicked

class PdfFolderAdapter(
    private val pdfFolders: MutableList<PdfFolder>, var isGridView: Boolean = false, var onPdfFolderClicked: OnPdfFolderClicked
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
        Log.e("PdfFolderAdapter", "updateList: new list" + newPdfFolders.size)
        Log.e("PdfFolderAdapter", "updateList: old list" + pdfFolders.size)

        pdfFolders.clear()
        pdfFolders.addAll(newPdfFolders)
        Log.e("PdfFolderAdapter", "updateList after update list: " + pdfFolders.size)

        notifyDataSetChanged()

//        val diffCallback = PdfFolderDiffCallback(pdfFolders, newPdfFolders)
//        val diffResult = DiffUtil.calculateDiff(diffCallback)
//
//        pdfFolders.clear()
//        pdfFolders.addAll(newPdfFolders)
//
//        diffResult.dispatchUpdatesTo(this)
    }


}