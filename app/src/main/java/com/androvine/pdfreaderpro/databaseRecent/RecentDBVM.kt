package com.androvine.pdfreaderpro.databaseRecent

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RecentDBVM(private val recentRepository: RecentRepository) : ViewModel() {

    private val _allRecent = MutableLiveData<List<RecentEntity>>()
    val allRecent : MutableLiveData<List<RecentEntity>>
        get() = _allRecent


    fun getAllRecent() = viewModelScope.launch {
        recentRepository.getAllRecent().collect {
            _allRecent.value = it
        }
    }

    fun insertRecent(recentEntity: RecentEntity) = viewModelScope.launch {
        recentRepository.insertRecent(recentEntity)
    }

    fun updateRecent(recentEntity: RecentEntity) = viewModelScope.launch {
        recentRepository.updateRecent(recentEntity)
    }

    fun deleteRecent(recentEntity: RecentEntity) = viewModelScope.launch {
        recentRepository.deleteRecent(recentEntity)
    }

    fun getRecentById(id: Long): RecentEntity {
        return recentRepository.getRecentById(id)
    }



}