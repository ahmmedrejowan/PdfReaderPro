package com.rejowan.pdfreaderpro.vms

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rejowan.pdfreaderpro.enums.PermissionStatus
import com.rejowan.pdfreaderpro.repoModels.PermissionRepository

class PermissionViewModel(private val permissionRepository: PermissionRepository) : ViewModel() {

    private val _permissionStatus = MutableLiveData<PermissionStatus>()
    val permissionStatus: LiveData<PermissionStatus> = _permissionStatus

    fun checkStoragePermission() {
        val hasPermission = permissionRepository.hasStoragePermission()
        _permissionStatus.value = if (hasPermission) PermissionStatus.GRANTED else PermissionStatus.INITIAL
    }

    fun handlePermissionResult(granted: Boolean) {
        _permissionStatus.value = if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED
    }
}

