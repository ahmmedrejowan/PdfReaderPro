package com.androvine.pdfreaderpro.dataClasses

data class RecentModel(
    var id: Long = 0L,
    var name: String,
    var path: String,
    var size: Long,
    var dateModified: Long,
    var parentFolderName: String,
    var lastOpened: Long,
    var totalPageCount: Int,
    var lastPageOpened: Int
)