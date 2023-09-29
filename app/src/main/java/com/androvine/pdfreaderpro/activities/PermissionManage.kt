package com.androvine.pdfreaderpro.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.androvine.pdfreaderpro.databinding.ActivityPermissionManageBinding
import com.androvine.pdfreaderpro.enums.PermissionStatus
import com.androvine.pdfreaderpro.repoModels.PermissionRepository
import com.androvine.pdfreaderpro.vms.PermissionViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class PermissionManage : AppCompatActivity() {

    private val binding: ActivityPermissionManageBinding by lazy {
        ActivityPermissionManageBinding.inflate(layoutInflater)
    }

    private val viewModel: PermissionViewModel by viewModel()

    private val permissionRepository: PermissionRepository by inject()

    // For below Android 11
    private val runtimePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val allGranted = permissionsMap.entries.all { it.value }
        viewModel.handlePermissionResult(allGranted)
    }

    // For Android 11 and above
    private val manageFilesLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handlePermissionResult(true)
        } else {
            viewModel.handlePermissionResult(false)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        viewModel.permissionStatus.observe(this) { status ->
            when (status) {
                PermissionStatus.INITIAL -> {
                    // Initial UI state (e.g. show permission request button)
                }

                PermissionStatus.GRANTED -> {
                    // Update UI for granted permission (e.g. show next page button)
                }

                PermissionStatus.DENIED -> {
                    // Update UI to show permission denied message or explanation
                }

                null -> {
                    // No-op
                }
            }
        }


        binding.allowAccessButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val manageFilesIntent =
                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:${packageName}")
                    }
                manageFilesLauncher.launch(manageFilesIntent)
            } else {
                val permissions = permissionRepository.getRequiredPermissions()
                runtimePermissionsLauncher.launch(permissions.toTypedArray())

            }
        }
    }


}
