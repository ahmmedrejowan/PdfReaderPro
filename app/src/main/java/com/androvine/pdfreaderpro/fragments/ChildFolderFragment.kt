package com.androvine.pdfreaderpro.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.androvine.pdfreaderpro.R
import com.androvine.pdfreaderpro.adapter.PdfFolderAdapter
import com.androvine.pdfreaderpro.customView.CustomListGridSwitchView
import com.androvine.pdfreaderpro.dataClasses.PdfFile
import com.androvine.pdfreaderpro.dataClasses.PdfFolder
import com.androvine.pdfreaderpro.databinding.BottomSheetSortingBinding
import com.androvine.pdfreaderpro.databinding.FragmentChildFolderBinding
import com.androvine.pdfreaderpro.interfaces.ChildFragmentsCommunication
import com.androvine.pdfreaderpro.interfaces.OnPdfFolderClicked
import com.androvine.pdfreaderpro.vms.PdfListViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.viewModel


class ChildFolderFragment : Fragment() {

    private val binding: FragmentChildFolderBinding by lazy {
        FragmentChildFolderBinding.inflate(layoutInflater)
    }
    private val pdfListViewModel: PdfListViewModel by viewModel()
    private val mutableListOfFolders = mutableListOf<PdfFolder>()

    lateinit var pdfFolderAdapter: PdfFolderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
}