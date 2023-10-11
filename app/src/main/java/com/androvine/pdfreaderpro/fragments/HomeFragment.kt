package com.androvine.pdfreaderpro.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.Formatter.formatFileSize
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.androvine.pdfreaderpro.adapter.RecentPdfAdapter
import com.androvine.pdfreaderpro.customView.CustomListGridSwitchView
import com.androvine.pdfreaderpro.dataClasses.RecentModel
import com.androvine.pdfreaderpro.database.RecentDBHelper
import com.androvine.pdfreaderpro.databinding.FragmentHomeBinding
import com.androvine.pdfreaderpro.interfaces.OnRecentClicked
import com.androvine.pdfreaderpro.vms.PdfListViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel


class HomeFragment : Fragment() {

    private val binding: FragmentHomeBinding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }

    private val pdfListViewModel: PdfListViewModel by activityViewModel()

    private lateinit var recentAdapter: RecentPdfAdapter
    private lateinit var recentDBHelper: RecentDBHelper


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



        recentAdapter = RecentPdfAdapter(mutableListOf(),
            false,
            binding.recyclerView,
            object : OnRecentClicked {
                override fun onInfo(recentModel: RecentModel) {
                    //
                }

                override fun onShare(recentModel: RecentModel) {
                    //
                }

                override fun onFavorite(recentModel: RecentModel) {
                    //
                }

                override fun onRename(recentModel: RecentModel, newName: String) {
                    //
                }

                override fun onRemoveFromRecent(recentModel: RecentModel) {
                    //
                }

                override fun onDeleted(recentModel: RecentModel) {
                    //
                }


            })


        if (binding.switchView.getCurrentMode() == CustomListGridSwitchView.SwitchMode.GRID) {
            binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
            recentAdapter.isGridView = true
        } else {
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recentAdapter.isGridView = false

        }
        binding.recyclerView.adapter = recentAdapter



        binding.switchView.setListener {
            when (it) {
                CustomListGridSwitchView.SwitchMode.GRID -> {
                    binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
                    recentAdapter.isGridView = true
                    recentAdapter.notifyDataSetChanged()
                }

                CustomListGridSwitchView.SwitchMode.LIST -> {
                    binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                    recentAdapter.isGridView = false
                    recentAdapter.notifyDataSetChanged()
                }
            }
        }

        binding.switchView.shouldRememberState(true)
        recentDBHelper = RecentDBHelper(requireContext())

        val recentList = recentDBHelper.getAllRecentItem()
        recentAdapter.updatePdfFiles(recentList)

    }

    private fun setUpInitialView() {
        binding.storageLayout.visibility = View.GONE
        binding.loadingFileCount.visibility = View.VISIBLE
        binding.totalFiles.visibility = View.GONE
        binding.totalFilesTitle.visibility = View.GONE
    }


    override fun onResume() {
        super.onResume()

        Log.e("TAG", "onResume: ")


        Handler(Looper.getMainLooper()).postDelayed({
            val recentList = recentDBHelper.getAllRecentItem()
            recentAdapter.updatePdfFiles(recentList)
        }, 1000)


        if (binding.switchView.getCurrentMode() != binding.switchView.getSavedMode()) {
            if (binding.switchView.getSavedMode() == CustomListGridSwitchView.SwitchMode.GRID) {
                binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
                recentAdapter.isGridView = true
                recentAdapter.notifyDataSetChanged()
                binding.switchView.setMode(CustomListGridSwitchView.SwitchMode.GRID)

            } else {
                binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recentAdapter.isGridView = false
                recentAdapter.notifyDataSetChanged()
                binding.switchView.setMode(CustomListGridSwitchView.SwitchMode.LIST)

            }
        }
    }

}