package com.androvine.pdfreaderpro.databaseRecent

import kotlinx.coroutines.flow.Flow

class RecentRepository(private val recentDao: RecentDao) {

    fun getAllRecent() = recentDao.getAllRecent()

    fun insertRecent(recentEntity: RecentEntity) {
        recentDao.insertRecent(recentEntity)
    }

    fun updateRecent(recentEntity: RecentEntity) {
        recentDao.updateRecent(recentEntity)
    }

    fun deleteRecent(recentEntity: RecentEntity) {
        recentDao.deleteRecent(recentEntity)
    }

    fun getRecentById(id: Long): RecentEntity {
        return recentDao.getRecentById(id)
    }


}