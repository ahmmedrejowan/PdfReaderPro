package com.rejowan.pdfreaderpro.domain.repository

import com.rejowan.pdfreaderpro.domain.model.PdfFile
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getFavorites(): Flow<List<PdfFile>>
    fun isFavoriteFlow(path: String): Flow<Boolean>
    suspend fun addFavorite(pdfFile: PdfFile)
    suspend fun removeFavorite(path: String)
    suspend fun isFavorite(path: String): Boolean
    suspend fun toggleFavorite(pdfFile: PdfFile)
    suspend fun clearAllFavorites()
    suspend fun getFavoriteCount(): Int
}
