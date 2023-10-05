package com.androvine.pdfreaderpro.diffUtils

import androidx.recyclerview.widget.DiffUtil
import com.androvine.pdfreaderpro.dataClasses.PdfFile

class PdfFileDiffCallback(
    private val oldList: List<PdfFile>,
    private val newList: List<PdfFile>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].path == newList[newItemPosition].path
    }

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

}