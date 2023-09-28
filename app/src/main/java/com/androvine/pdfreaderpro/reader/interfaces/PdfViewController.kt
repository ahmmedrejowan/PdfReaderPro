package com.androvine.pdfreaderpro.reader.interfaces

import android.view.View
import com.androvine.pdfreaderpro.reader.utils.PdfPageQuality
import java.io.File

interface PdfViewController {

    fun getView(): View

    fun setup(file: File)

    fun setZoomEnabled(isZoomEnabled: Boolean)

    fun setMaxZoom(maxZoom: Float)

    fun setQuality(quality: PdfPageQuality)

    fun setOnPageChangedListener(onPageChangedListener: OnPageChangedListener?)

    fun goToPosition(position: Int)
}