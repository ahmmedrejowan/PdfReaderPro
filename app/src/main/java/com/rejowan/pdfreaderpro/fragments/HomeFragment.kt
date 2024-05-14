package com.rejowan.pdfreaderpro.fragments

import android.content.Intent
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
import com.rejowan.pdfreaderpro.activities.AppSettings
import com.rejowan.pdfreaderpro.adapter.FavoriteAdapter
import com.rejowan.pdfreaderpro.adapter.RecentPdfAdapter
import com.rejowan.pdfreaderpro.customView.CustomListGridSwitchView
import com.rejowan.pdfreaderpro.dataClasses.PdfFile
import com.rejowan.pdfreaderpro.dataClasses.RecentModel
import com.rejowan.pdfreaderpro.database.FavoriteDBHelper
import com.rejowan.pdfreaderpro.database.RecentDBHelper
import com.rejowan.pdfreaderpro.databinding.FragmentHomeBinding
import com.rejowan.pdfreaderpro.interfaces.OnPdfFileClicked
import com.rejowan.pdfreaderpro.interfaces.OnRecentClicked
import com.rejowan.pdfreaderpro.vms.PdfListViewModel
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.File


class HomeFragment : Fragment() {

    private val binding: FragmentHomeBinding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }

    private val pdfListViewModel: PdfListViewModel by activityViewModel()

    private lateinit var recentAdapter: RecentPdfAdapter
    private lateinit var recentDBHelper: RecentDBHelper

    private lateinit var favoriteAdapter: FavoriteAdapter
    private lateinit var favoriteDBHelper: FavoriteDBHelper

    val recentList: MutableList<RecentModel> = mutableListOf()
    val favoriteList: MutableList<PdfFile> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupDB()
        setupList()

        binding.settingsImageView.setOnClickListener {
            startActivity(Intent(requireContext(),AppSettings::class.java))
        }

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Recent"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Favorite"))

        setUpInitialView()

        pdfListViewModel.pdfFiles.observe(viewLifecycleOwner) { pdfFiles ->
            updateUI(pdfFiles)
        }

        setupAdapter()


        setUpSWitchViewAndUpdate()

        binding.recyclerViewRecent.visibility = View.VISIBLE
        binding.recyclerViewFavorite.visibility = View.GONE

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    binding.recyclerViewRecent.visibility = View.VISIBLE
                    binding.recyclerViewFavorite.visibility = View.GONE
                } else {
                    binding.recyclerViewRecent.visibility = View.GONE
                    binding.recyclerViewFavorite.visibility = View.VISIBLE
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                //
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                //
            }

        })


    }

    private fun setUpSWitchViewAndUpdate() {

        if (binding.switchView.getCurrentMode() == CustomListGridSwitchView.SwitchMode.GRID) {
            binding.recyclerViewRecent.layoutManager = GridLayoutManager(requireContext(), 2)
            recentAdapter.isGridView = true

            binding.recyclerViewFavorite.layoutManager = GridLayoutManager(requireContext(), 2)
            favoriteAdapter.isGridView = true
        } else {
            binding.recyclerViewRecent.layoutManager = LinearLayoutManager(requireContext())
            recentAdapter.isGridView = false

            binding.recyclerViewFavorite.layoutManager = LinearLayoutManager(requireContext())
            favoriteAdapter.isGridView = false

        }

        binding.recyclerViewRecent.adapter = recentAdapter
        checkAndUpdateListRecent()

        binding.recyclerViewFavorite.adapter = favoriteAdapter
        checkAndUpdateListFavorite()



        binding.switchView.setListener {
            when (it) {
                CustomListGridSwitchView.SwitchMode.GRID -> {
                    binding.recyclerViewRecent.layoutManager =
                        GridLayoutManager(requireContext(), 2)
                    recentAdapter.isGridView = true
                    recentAdapter.notifyDataSetChanged()

                    binding.recyclerViewFavorite.layoutManager =
                        GridLayoutManager(requireContext(), 2)
                    favoriteAdapter.isGridView = true
                    favoriteAdapter.notifyDataSetChanged()
                }

                CustomListGridSwitchView.SwitchMode.LIST -> {
                    binding.recyclerViewRecent.layoutManager = LinearLayoutManager(requireContext())
                    recentAdapter.isGridView = false
                    recentAdapter.notifyDataSetChanged()

                    binding.recyclerViewFavorite.layoutManager =
                        LinearLayoutManager(requireContext())
                    favoriteAdapter.isGridView = false
                    favoriteAdapter.notifyDataSetChanged()

                }
            }
        }

        binding.switchView.shouldRememberState(true)


    }

    private fun setupAdapter() {

        recentAdapter = RecentPdfAdapter(mutableListOf(),
            false,
            binding.recyclerViewRecent,
            object : OnRecentClicked {
                override fun onFavorite(recentModel: RecentModel) {
                    //
                }

                override fun onRename(recentModel: RecentModel, newName: String) {
                    //
                }

                override fun onRemoveFromRecent(recentModel: RecentModel) {
                    recentDBHelper.deleteRecentItem(recentModel.path)
                    recentList.remove(recentModel)
                    recentAdapter.updatePdfFiles(recentList)
                }

                override fun onDeleted(recentModel: RecentModel) {
                    //
                }


            })

        favoriteAdapter = FavoriteAdapter(
            mutableListOf(),
            false,
            binding.recyclerViewFavorite,
            object : OnPdfFileClicked {
                override fun onPdfFileRenamed(pdfFile: PdfFile, newName: String) {
                    //
                }

                override fun onPdfFileDeleted(pdfFile: PdfFile) {
                    favoriteDBHelper.deleteFavorite(pdfFile.path)
                    favoriteList.remove(pdfFile)
                    favoriteAdapter.updatePdfFiles(favoriteList)
                }

            })

    }

    private fun updateUI(pdfFiles: List<PdfFile>) {
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
        val totalFree = stat.availableBlocksLong * blockSize
        val totalUsed = total - totalFree
        val usedFormatted = formatFileSize(requireContext(), totalUsed)
        binding.totalSize.text = usedFormatted.substring(0, usedFormatted.length - 2)
        binding.totalSizeUnit.text = usedFormatted.substring(usedFormatted.length - 2)

        var pdfSizeInPercentage = (pdfFiles.sumOf { it.size } * 100) / totalUsed
        Log.e("Storage Details", "Total: $total , Total Free: $totalFree , Total Used: $totalUsed , pdfSize: ${pdfFiles.sumOf { it.size }}, pdfSizeInPercentage: $pdfSizeInPercentage")


        if (pdfSizeInPercentage < 3){
            pdfSizeInPercentage = 3
        } else {
            pdfSizeInPercentage += 3
        }

        binding.progressBar.progress = pdfSizeInPercentage.toInt()


    }

    private fun setupList() {
        recentList.clear()
        recentList.addAll(recentDBHelper.getAllRecentItem())

        favoriteList.clear()
        favoriteList.addAll(favoriteDBHelper.getAllFavoriteItem())

    }

    private fun setupDB() {
        recentDBHelper = RecentDBHelper(requireContext())
        favoriteDBHelper = FavoriteDBHelper(requireContext())

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
            recentList.clear()
            recentList.addAll(recentDBHelper.getAllRecentItem())
            checkAndUpdateListRecent()

            favoriteList.clear()
            favoriteList.addAll(favoriteDBHelper.getAllFavoriteItem())
            checkAndUpdateListFavorite()

        }, 1000)


        if (binding.switchView.getCurrentMode() != binding.switchView.getSavedMode()) {
            if (binding.switchView.getSavedMode() == CustomListGridSwitchView.SwitchMode.GRID) {
                binding.recyclerViewRecent.layoutManager = GridLayoutManager(requireContext(), 2)
                recentAdapter.isGridView = true
                recentAdapter.notifyDataSetChanged()
                binding.switchView.setMode(CustomListGridSwitchView.SwitchMode.GRID)

                binding.recyclerViewFavorite.layoutManager = GridLayoutManager(requireContext(), 2)
                favoriteAdapter.isGridView = true
                favoriteAdapter.notifyDataSetChanged()
                binding.switchView.setMode(CustomListGridSwitchView.SwitchMode.GRID)
            } else {
                binding.recyclerViewRecent.layoutManager = LinearLayoutManager(requireContext())
                recentAdapter.isGridView = false
                recentAdapter.notifyDataSetChanged()
                binding.switchView.setMode(CustomListGridSwitchView.SwitchMode.LIST)

                binding.recyclerViewFavorite.layoutManager = LinearLayoutManager(requireContext())
                favoriteAdapter.isGridView = false
                favoriteAdapter.notifyDataSetChanged()
                binding.switchView.setMode(CustomListGridSwitchView.SwitchMode.LIST)


            }
        }
    }

    private fun checkAndUpdateListFavorite() {

        favoriteList.forEach {

            val file = File(it.path)
            if (!file.exists()) {
                favoriteDBHelper.deleteFavorite(it.path)
            }

        }

        favoriteAdapter.updatePdfFiles(favoriteList)
    }

    private fun checkAndUpdateListRecent() {
        recentList.forEach {
//            if (!it.isUri){
//                val file = File(it.path)
//                if (!file.exists()) {
//                    recentDBHelper.deleteRecentItem(it.path)
//                }
//            }
        }

        recentAdapter.updatePdfFiles(recentList)

    }

}