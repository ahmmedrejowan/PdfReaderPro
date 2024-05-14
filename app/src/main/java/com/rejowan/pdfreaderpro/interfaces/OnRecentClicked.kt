package com.rejowan.pdfreaderpro.interfaces

import com.rejowan.pdfreaderpro.dataClasses.RecentModel

interface OnRecentClicked {
    fun onFavorite(recentModel: RecentModel)
    fun onRename(recentModel: RecentModel, newName: String)
    fun onRemoveFromRecent(recentModel: RecentModel)
    fun onDeleted(recentModel: RecentModel)
}