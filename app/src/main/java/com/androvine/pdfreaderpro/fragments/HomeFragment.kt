package com.androvine.pdfreaderpro.fragments

import android.os.Bundle
import android.text.format.Formatter.formatFileSize
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.androvine.pdfreaderpro.databinding.FragmentHomeBinding
import com.androvine.pdfreaderpro.vms.PdfListViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class HomeFragment : Fragment() {

    private val binding: FragmentHomeBinding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }

    private val pdfListViewModel: PdfListViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Recent"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Favorite"))

        setUpInitialView()

        pdfListViewModel.pdfFiles.observe(viewLifecycleOwner) { pdfFiles ->

            binding.loadingFileCount.visibility = View.GONE
            binding.storageLayout.visibility = View.VISIBLE
            binding.totalFiles.visibility = View.VISIBLE
            binding.totalFilesTitle.visibility = View.VISIBLE

            binding.totalFiles.text = pdfFiles.size.toString()

            val totalFileSize = formatFileSize(requireContext(), pdfFiles.sumOf { it.size })
            binding.pdfSize.text = totalFileSize.substring(0, totalFileSize.length - 2)
            binding.pdfSizeUnit.text = totalFileSize.substring(totalFileSize.length - 2)

            // get total device storage size
            val file = android.os.Environment.getDataDirectory()
            val stat = android.os.StatFs(file.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val total = totalBlocks * blockSize
            val totalFormatted = formatFileSize(requireContext(), total)
            binding.totalSize.text = totalFormatted.substring(0, totalFormatted.length - 2)
            binding.totalSizeUnit.text = totalFormatted.substring(totalFormatted.length - 2)
        }

    }

    private fun setUpInitialView() {
        binding.storageLayout.visibility = View.GONE
        binding.loadingFileCount.visibility = View.VISIBLE
        binding.totalFiles.visibility = View.GONE
        binding.totalFilesTitle.visibility = View.GONE
    }


}