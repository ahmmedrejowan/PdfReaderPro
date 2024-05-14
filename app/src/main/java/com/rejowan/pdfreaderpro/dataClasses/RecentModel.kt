package com.rejowan.pdfreaderpro.dataClasses

data class RecentModel(
    var id: Int = 0,
    var name: String,
    var path: String,
    var size: Long,
    var lastOpenedDate: Long,
    var totalPageCount: Int,
    var lastPageOpened: Int,
)