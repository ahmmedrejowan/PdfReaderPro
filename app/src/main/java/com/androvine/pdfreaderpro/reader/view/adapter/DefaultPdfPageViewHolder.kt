package com.androvine.pdfreaderpro.reader.view.adapter

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import com.androvine.pdfreaderpro.R

class DefaultPdfPageViewHolder(
    view: View
) : PdfPageViewHolder(view) {

    private val image: ImageView = itemView.findViewById(R.id.image)

    override fun bind(page: Bitmap) {
        image.layoutParams.height =  page.height*image.width/page.width
        image.setImageBitmap(page)
    }
}