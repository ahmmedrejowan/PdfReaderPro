package com.androvine.pdfreaderpro.databaseRecent

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecentDBVM(private val recentRepository: RecentRepository) : ViewModel() {

    private val _allRecent = MutableLiveData<List<RecentEntity>>()
    val allRecent : MutableLiveData<List<RecentEntity>>
        get() = _allRecent

    init {
        getAllRecent()
    }

   private fun getAllRecent() = viewModelScope.launch{
        recentRepository.getAllRecent().collect {
            _allRecent.value = it
        }
    }


    fun insertRecent(recentEntity: RecentEntity) = viewModelScope.launch (Dispatchers.IO) {
        recentRepository.insertRecent(recentEntity)
        Log.d("RecentDBVM", "insertRecent: $recentEntity")
    }

    fun updateRecent(recentEntity: RecentEntity) = viewModelScope.launch(Dispatchers.IO) {
        recentRepository.updateRecent(recentEntity)
    }

    fun deleteRecent(recentEntity: RecentEntity) = viewModelScope.launch (Dispatchers.IO){
        recentRepository.deleteRecent(recentEntity)
    }


}