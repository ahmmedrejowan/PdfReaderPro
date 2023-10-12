package com.androvine.pdfreaderpro.interfaces

import com.androvine.pdfreaderpro.dataClasses.RecentModel

interface OnRecentClicked {
    fun onFavorite(recentModel: RecentModel)
    fun onRename(recentModel: RecentModel, newName: String)
    fun onRemoveFromRecent(recentModel: RecentModel)
    fun onDeleted(recentModel: RecentModel)
}