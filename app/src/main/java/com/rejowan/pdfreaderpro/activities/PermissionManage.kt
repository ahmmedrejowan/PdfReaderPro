package com.rejowan.pdfreaderpro.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.rejowan.pdfreaderpro.R
import com.rejowan.pdfreaderpro.databinding.ActivityPermissionManageBinding
import com.rejowan.pdfreaderpro.enums.PermissionStatus
import com.rejowan.pdfreaderpro.repoModels.PermissionRepository
import com.rejowan.pdfreaderpro.vms.PermissionViewModel
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
    ) { _ ->

        if (permissionRepository.hasStoragePermission()) {
            viewModel.handlePermissionResult(true)
            return@registerForActivityResult
        } else {
            viewModel.handlePermissionResult(false)
            return@registerForActivityResult
        }


    }

    private var currentStatus = PermissionStatus.INITIAL


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        setupViewModel()

        setupClicks()


    }


    private fun setupViewModel() {
        viewModel.checkStoragePermission()

        viewModel.permissionStatus.observe(this) { status ->
            when (status) {
                PermissionStatus.INITIAL -> {
                    currentStatus = PermissionStatus.INITIAL
                    initialUI()
                }

                PermissionStatus.GRANTED -> {
                    currentStatus = PermissionStatus.GRANTED
                    allowedUI()
                }

                PermissionStatus.DENIED -> {
                    currentStatus = PermissionStatus.DENIED
                    deniedUI()
                }

                null -> {
                    initialUI()
                }
            }
        }

    }

    private fun setupClicks() {

        binding.allowAccessButton.setOnClickListener {
            if (currentStatus == PermissionStatus.INITIAL || currentStatus == PermissionStatus.DENIED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val manageFilesIntent = permissionRepository.getStoragePermissionIntent()
                    manageFilesLauncher.launch(manageFilesIntent)
                } else {
                    val permissions = permissionRepository.getRequiredPermissions()
                    runtimePermissionsLauncher.launch(permissions.toTypedArray())

                }
            } else if (currentStatus == PermissionStatus.GRANTED) {
                goToHome()
            }
        }

        binding.appSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }


        val htmlStringHowToAllow = "<u>How to allow access?</u>"
        binding.howToAllowAccess.text =
            Html.fromHtml(htmlStringHowToAllow, Html.FROM_HTML_MODE_COMPACT)

        binding.howToAllowAccess.setOnClickListener {
            // TODO how to allow dialog
        }

    }

    private fun initialUI() {

        Log.e("TAG", "initialUI: ")

        binding.permissionTitle.text = getString(R.string.permission_normal_title)
        binding.permissionSubTitle.text = getString(R.string.permission_normal_sub_title)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            binding.permissionDescription.text =
                getString(R.string.permission_above_11_normal_description)
            binding.howToAllowAccess.visibility = View.VISIBLE
        } else {
            binding.permissionTitle.text =
                getString(R.string.permission_below_11_normal_description)
            binding.howToAllowAccess.visibility = View.INVISIBLE
        }

        binding.permissionImage.setImageResource(R.drawable.ic_folder_permission)

        binding.allowAccessButton.text = getString(R.string.allow_access)

        binding.appSettings.visibility = View.INVISIBLE

    }


    private fun allowedUI() {

        Log.e("TAG", "allowedUI: ")

        binding.permissionTitle.text = getString(R.string.permission_allowed_title)
        binding.permissionSubTitle.text = getString(R.string.permission_allowed_sub_title)

        binding.allowAccessButton.text = getString(R.string.lets_go)

        binding.permissionImage.setImageResource(R.drawable.ic_checkmark_permission)

        binding.permissionDescription.visibility = View.INVISIBLE
        binding.appSettings.visibility = View.INVISIBLE
        binding.howToAllowAccess.visibility = View.INVISIBLE


    }


    private fun deniedUI() {


        Log.e("TAG", "deniedUI: ")

        binding.permissionTitle.text = getString(R.string.permission_error_title)
        binding.permissionSubTitle.text = getString(R.string.permission_error_sub_title)
        binding.permissionDescription.text = getString(R.string.permission_error_description)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            binding.howToAllowAccess.visibility = View.VISIBLE
        } else {
            binding.howToAllowAccess.visibility = View.INVISIBLE

        }

        binding.appSettings.visibility = View.VISIBLE
        binding.permissionImage.setImageResource(R.drawable.ic_warning_permission)
        binding.allowAccessButton.text = getString(R.string.try_again)

    }

    private fun goToHome() {
        startActivity(Intent(this, Home::class.java))
        finish()
    }


}
