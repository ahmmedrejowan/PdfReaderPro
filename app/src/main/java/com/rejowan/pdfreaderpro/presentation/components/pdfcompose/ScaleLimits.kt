package com.rejowan.pdfreaderpro.presentation.components.pdfcompose

import androidx.annotation.FloatRange
import com.rejowan.pdfreaderpro.presentation.components.pdf.PdfViewer.Zoom

/**
 * Defines the scale limits for the PDF viewer.
 *
 * These values are used to configure the zoom behavior of the viewer.
 *
 * @property minPageScale The minimum page scale.
 * @property maxPageScale The maximum page scale.
 * @property defaultPageScale The default page scale.
 * @see com.rejowan.pdfreaderpro.presentation.components.pdf.PdfViewer
 * @see com.rejowan.pdfreaderpro.presentation.components.pdf.PdfViewerView.Zoom
 */
data class ScaleLimit(
    @param:FloatRange(-4.0, 10.0) val minPageScale: Float = 0.1f,
    @param:FloatRange(-4.0, 10.0) val maxPageScale: Float = 10f,
    @param:FloatRange(-4.0, 10.0) val defaultPageScale: Float = Zoom.AUTOMATIC.floatValue,
)

/**
 * Represents the actual, computed scale limits used by the PDF viewer.
 *
 * @property minPageScale The computed minimum page scale.
 * @property maxPageScale The computed maximum page scale.
 * @property defaultPageScale The computed default page scale.
 * @see ScaleLimit
 * @see com.rejowan.pdfreaderpro.presentation.components.pdf.PdfViewer
 */
data class ActualScaleLimit(
    @param:FloatRange(0.0, 10.0) val minPageScale: Float = 0.1f,
    @param:FloatRange(0.0, 10.0) val maxPageScale: Float = 10f,
    @param:FloatRange(0.0, 10.0) val defaultPageScale: Float = 0f,
)
