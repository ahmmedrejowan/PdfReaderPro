package com.rejowan.pdfreaderpro.data.repository

import com.rejowan.pdfreaderpro.data.local.database.dao.RecentDao
import com.rejowan.pdfreaderpro.data.local.database.entity.RecentEntity
import com.rejowan.pdfreaderpro.domain.model.RecentFile
import com.rejowan.pdfreaderpro.domain.repository.RecentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class RecentRepositoryImpl(
    private val recentDao: RecentDao
) : RecentRepository {

    override fun getRecentFiles(): Flow<List<RecentFile>> {
        return recentDao.getAllRecent().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addOrUpdateRecent(
        path: String,
        name: String,
        size: Long,
        totalPages: Int,
        currentPage: Int
    ) {
        val entity = RecentEntity(
            name = name,
            path = path,
            size = size,
            lastOpened = System.currentTimeMillis(),
            totalPages = totalPages,
            lastPage = currentPage
        )
        recentDao.upsert(entity)
    }

    override suspend fun updateLastPage(path: String, page: Int) {
        recentDao.updateLastPage(path, page)
    }

    override suspend fun getLastPage(path: String): Int? {
        return recentDao.getByPath(path)?.lastPage
    }

    override suspend fun removeRecent(path: String) {
        recentDao.deleteByPath(path)
    }

    override suspend fun clearAllRecent() {
        recentDao.clearAll()
    }

    override suspend fun getRecentCount(): Int {
        return recentDao.getCount()
    }

    override suspend fun updatePath(oldPath: String, newPath: String, newName: String) {
        recentDao.updatePath(oldPath, newPath, newName)
    }

    override suspend fun cleanupMissingFiles() {
        val recentFiles = recentDao.getAllRecentSync()
        recentFiles.forEach { entity ->
            if (!File(entity.path).exists()) {
                recentDao.deleteByPath(entity.path)
            }
        }
    }

    private fun RecentEntity.toDomain(): RecentFile {
        return RecentFile(
            id = id,
            name = name,
            path = path,
            size = size,
            lastOpened = lastOpened,
            totalPages = totalPages,
            lastPage = lastPage
        )
    }
}
