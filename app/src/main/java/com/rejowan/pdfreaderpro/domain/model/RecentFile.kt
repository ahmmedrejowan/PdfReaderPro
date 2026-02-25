package com.rejowan.pdfreaderpro.domain.model

data class RecentFile(
    val id: Long,
    val name: String,
    val path: String,
    val size: Long,
    val lastOpened: Long,
    val totalPages: Int,
    val lastPage: Int
) {
    val progress: Float
        get() = if (totalPages > 0) lastPage.toFloat() / totalPages else 0f

    val progressPercent: Int
        get() = (progress * 100).toInt()
}
