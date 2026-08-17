package com.rejowan.pdfreaderpro.presentation.screens.reader

import kotlinx.serialization.Serializable

/**
 * One placed signature as the viewer reports it.
 *
 * [rect] is [left, bottom, right, top] in PDF user space, which is the form the
 * viewer both gives and accepts, so it is stored unconverted.
 */
@Serializable
data class PlacedSignatureJson(
    val key: String,
    val pageIndex: Int,
    val rect: List<Float>
) {
    val left: Float get() = rect.getOrElse(0) { 0f }
    val bottom: Float get() = rect.getOrElse(1) { 0f }
    val right: Float get() = rect.getOrElse(2) { 0f }
    val top: Float get() = rect.getOrElse(3) { 0f }
}
