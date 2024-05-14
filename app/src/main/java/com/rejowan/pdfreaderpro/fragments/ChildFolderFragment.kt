package com.rejowan.pdfreaderpro.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.rejowan.pdfreaderpro.R
import com.rejowan.pdfreaderpro.activities.AppSettings
import com.rejowan.pdfreaderpro.adapter.PdfFolderAdapter
import com.rejowan.pdfreaderpro.customView.CustomListGridSwitchView
import com.rejowan.pdfreaderpro.dataClasses.PdfFile
import com.rejowan.pdfreaderpro.dataClasses.PdfFolder
import com.rejowan.pdfreaderpro.databinding.BottomSheetSortingBinding
import com.rejowan.pdfreaderpro.databinding.FragmentChildFolderBinding
import com.rejowan.pdfreaderpro.interfaces.ChildFragmentsCommunication
import com.rejowan.pdfreaderpro.interfaces.OnPdfFolderClicked
import com.rejowan.pdfreaderpro.vms.PdfListViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.activityViewModel


@SuppressLint("NotifyDataSetChanged")
class ChildFolderFragment : Fragment() {

    private val binding: FragmentChildFolderBinding by lazy {
        FragmentChildFolderBinding.inflate(layoutInflater)
    }
    private val pdfListViewModel: PdfListViewModel by activityViewModel()
    private val mutableListOfFolders = mutableListOf<PdfFolder>()

    private lateinit var pdfFolderAdapter: PdfFolderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settingsImageView.setOnClickListener {
            startActivity(Intent(requireContext(), AppSettings::class.java))
        }

        pdfFolderAdapter =
            PdfFolderAdapter(mutableListOfFolders, isGridView = false, object : OnPdfFolderClicked {
                override fun onPdfFolderClicked(folderName: String) {
                    (parentFragment as? ChildFragmentsCommunication)?.onFolderSelected(folderName)
                }
            })

        if (binding.switchView.getCurrentMode() == CustomListGridSwitchView.SwitchMode.GRID) {
            binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
            pdfFolderAdapter.isGridView = true
        } else {
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            pdfFolderAdapter.isGridView = false
        }
        binding.recyclerView.adapter = pdfFolderAdapter


        pdfListViewModel.pdfFiles.observe(viewLifecycleOwner) { pdfFiles ->
            if (pdfFiles.isEmpty()) {
                binding.noFileLayout.visibility = View.VISIBLE
            } else {
                binding.noFileLayout.visibility = View.GONE
            }

            setUpFolderData(pdfFiles)
        }

        binding.switchView.setListener {
            when (it) {
                CustomListGridSwitchView.SwitchMode.GRID -> {
                    binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
                    pdfFolderAdapter.isGridView = true
                    pdfFolderAdapter.notifyDataSetChanged()
                }

                CustomListGridSwitchView.SwitchMode.LIST -> {
                    binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                    pdfFolderAdapter.isGridView = false
                    pdfFolderAdapter.notifyDataSetChanged()
                }
            }
        }

        binding.switchView.shouldRememberState(true)

        binding.sortBy.setOnClickListener {
            showSortingDialog()
        }

    }


    private fun setUpFolderData(pdfFiles: List<PdfFile>) {

        mutableListOfFolders.clear()
        val mapOfFolders = pdfFiles.groupBy { it.parentFolderName }

        mapOfFolders.forEach { (folderName, pdfFiles) ->
            mutableListOfFolders.add(PdfFolder(folderName, pdfFiles))
        }

        val sortedList = mutableListOfFolders.sortedWith(PdfFolder.SortByName)
        pdfFolderAdapter.updateList(sortedList.toList())

    }

    private fun showSortingDialog() {


        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val sortingBinding: BottomSheetSortingBinding = BottomSheetSortingBinding.inflate(
            layoutInflater
        )
        bottomSheetDialog.setContentView(sortingBinding.root)
        bottomSheetDialog.setCancelable(true)
        bottomSheetDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        bottomSheetDialog.window!!.setLayout(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        )

        sortingBinding.tvName.setOnClickListener {
            binding.sortBy.text = getString(R.string.name)
            sortList(PdfFolder.SortByName)
            bottomSheetDialog.dismiss()
        }

        sortingBinding.tvDate.visibility = View.GONE

        sortingBinding.tvSize.setOnClickListener {
            binding.sortBy.text = getString(R.string.size)
            sortList(PdfFolder.SortBySize)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }


    private fun sortList(comparator: Comparator<PdfFolder>) {
        val sortedList = mutableListOfFolders.sortedWith(comparator)
        sortedList.let {
            pdfFolderAdapter.updateList(it)
        }
    }


    override fun onResume() {
        super.onResume()

        if (binding.switchView.getCurrentMode() != binding.switchView.getSavedMode()) {
            if (binding.switchView.getSavedMode() == CustomListGridSwitchView.SwitchMode.GRID) {
                binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
                pdfFolderAdapter.isGridView = true
                pdfFolderAdapter.notifyDataSetChanged()
                binding.switchView.setMode(CustomListGridSwitchView.SwitchMode.GRID)

            } else {
                binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                pdfFolderAdapter.isGridView = false
                pdfFolderAdapter.notifyDataSetChanged()
                binding.switchView.setMode(CustomListGridSwitchView.SwitchMode.LIST)
            }
        }
    }

}