package com.androvine.pdfreaderpro.databaseRecent

import android.util.Log


class RecentRepository(private val recentDao: RecentDao) {

    fun getAllRecent() = recentDao.getAllRecent()

    fun getRecentByPath(path: String): RecentEntity {
        return recentDao.getRecentByPath(path)
    }

    fun insertRecent(recentEntity: RecentEntity) {
        val result = recentDao.insertRecent(recentEntity)
        Log.e("Recent", "Recent Insert: $result")
    }

    fun updateRecent(recentEntity: RecentEntity) {
       val result =  recentDao.updateRecent(recentEntity)
        Log.e("Recent", "Recent Update: $result")
    }

    fun deleteRecent(recentEntity: RecentEntity) {
        recentDao.deleteRecent(recentEntity)
    }

    fun getRecentById(id: Long): RecentEntity {
        return recentDao.getRecentById(id)
    }


}