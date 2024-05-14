package com.rejowan.pdfreaderpro.diffUtils

import androidx.recyclerview.widget.DiffUtil
import com.rejowan.pdfreaderpro.dataClasses.PdfFolder

class PdfFolderDiffCallback(
    private val oldList: List<PdfFolder>,
    private val newList: List<PdfFolder>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].name == newList[newItemPosition].name
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
