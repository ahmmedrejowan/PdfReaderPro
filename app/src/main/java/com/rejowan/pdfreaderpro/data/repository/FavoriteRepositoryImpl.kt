package com.rejowan.pdfreaderpro.data.repository

import android.net.Uri
import com.rejowan.pdfreaderpro.data.local.database.dao.FavoriteDao
import com.rejowan.pdfreaderpro.data.local.database.entity.FavoriteEntity
import com.rejowan.pdfreaderpro.domain.model.PdfFile
import com.rejowan.pdfreaderpro.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class FavoriteRepositoryImpl(
    private val favoriteDao: FavoriteDao
) : FavoriteRepository {

    override fun getFavorites(): Flow<List<PdfFile>> {
        return favoriteDao.getAllFavorites().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun isFavoriteFlow(path: String): Flow<Boolean> {
        return favoriteDao.isFavoriteFlow(path)
    }

    override suspend fun addFavorite(pdfFile: PdfFile) {
        val entity = FavoriteEntity(
            name = pdfFile.name,
            path = pdfFile.path,
            size = pdfFile.size,
            dateModified = pdfFile.dateModified,
            parentFolder = pdfFile.parentFolder
        )
        favoriteDao.insert(entity)
    }

    override suspend fun removeFavorite(path: String) {
        favoriteDao.deleteByPath(path)
    }

    override suspend fun isFavorite(path: String): Boolean {
        return favoriteDao.isFavorite(path)
    }

    override suspend fun toggleFavorite(pdfFile: PdfFile) {
        if (isFavorite(pdfFile.path)) {
            removeFavorite(pdfFile.path)
        } else {
            addFavorite(pdfFile)
        }
    }

    override suspend fun clearAllFavorites() {
        favoriteDao.clearAll()
    }

    override suspend fun getFavoriteCount(): Int {
        return favoriteDao.getCount()
    }

    override suspend fun updatePath(oldPath: String, newPath: String, newName: String) {
        favoriteDao.updatePath(oldPath, newPath, newName)
    }

    override suspend fun cleanupMissingFiles() {
        val favorites = favoriteDao.getAllFavoritesSync()
        favorites.forEach { entity ->
            if (!File(entity.path).exists()) {
                favoriteDao.deleteByPath(entity.path)
            }
        }
    }

    private fun FavoriteEntity.toDomain(): PdfFile {
        return PdfFile(
            id = id,
            name = name,
            path = path,
            uri = Uri.parse("file://$path"),
            size = size,
            dateModified = dateModified,
            dateAdded = dateModified,
            parentFolder = parentFolder,
            isFavorite = true
        )
    }
}
