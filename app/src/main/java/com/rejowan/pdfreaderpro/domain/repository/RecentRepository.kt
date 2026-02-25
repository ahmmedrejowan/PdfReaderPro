package com.rejowan.pdfreaderpro.domain.repository

import com.rejowan.pdfreaderpro.domain.model.RecentFile
import kotlinx.coroutines.flow.Flow

interface RecentRepository {
    fun getRecentFiles(): Flow<List<RecentFile>>
    suspend fun addOrUpdateRecent(path: String, name: String, size: Long, totalPages: Int, currentPage: Int)
    suspend fun updateLastPage(path: String, page: Int)
    suspend fun removeRecent(path: String)
    suspend fun clearAllRecent()
    suspend fun getRecentCount(): Int
}
