package com.androvine.pdfreaderpro.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.androvine.pdfreaderpro.R
import com.androvine.pdfreaderpro.adapter.PdfAdapter
import com.androvine.pdfreaderpro.customView.CustomListGridSwitchView
import com.androvine.pdfreaderpro.dataClasses.PdfFile
import com.androvine.pdfreaderpro.databinding.BottomSheetSortingBinding
import com.androvine.pdfreaderpro.databinding.FragmentFilesBinding
import com.androvine.pdfreaderpro.vms.PdfListViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("NotifyDataSetChanged")
class FilesFragment : Fragment() {


    private val binding: FragmentFilesBinding by lazy {
        FragmentFilesBinding.inflate(layoutInflater)
    }

    private val pdfListViewModel: PdfListViewModel by viewModel()
    private lateinit var pdfAdapter: PdfAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pdfAdapter = PdfAdapter(mutableListOf(), false, binding.recyclerView)

        if (binding.switchView.getCurrentMode() == CustomListGridSwitchView.SwitchMode.GRID) {
            binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
            pdfAdapter.isGridView = true
        } else {
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            pdfAdapter.isGridView = false
        }
        binding.recyclerView.adapter = pdfAdapter

        pdfListViewModel.pdfFiles.observe(viewLifecycleOwner) { pdfFiles ->

            val sortedList = pdfListViewModel.pdfFiles.value?.sortedWith(PdfFile.SortByName)
            sortedList?.let {
                pdfAdapter.updatePdfFiles(it)
            }

            if (pdfFiles.isEmpty()) {
                binding.noFileLayout.visibility = View.VISIBLE
            } else {
                binding.noFileLayout.visibility = View.GONE
            }

        }

        binding.switchView.setListener {
            when (it) {
                CustomListGridSwitchView.SwitchMode.GRID -> {
                    binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
                    pdfAdapter.isGridView = true
                    pdfAdapter.notifyDataSetChanged()
                }

                CustomListGridSwitchView.SwitchMode.LIST -> {
                    binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                    pdfAdapter.isGridView = false
                    pdfAdapter.notifyDataSetChanged()
                }
            }
        }

        binding.switchView.shouldRememberState(true)


        binding.sortBy.setOnClickListener {
            showSortingDialog()
        }


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
            sortList(PdfFile.SortByName)
            bottomSheetDialog.dismiss()
        }

        sortingBinding.tvDate.setOnClickListener {
            binding.sortBy.text = getString(R.string.date)
            sortList(PdfFile.SortByDate)
            bottomSheetDialog.dismiss()
        }

        sortingBinding.tvSize.setOnClickListener {
            binding.sortBy.text = getString(R.string.size)
            sortList(PdfFile.SortBySize)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }


    private fun sortList(comparator: Comparator<PdfFile>) {
        val sortedList = pdfListViewModel.pdfFiles.value?.sortedWith(comparator)
        sortedList?.let {
            pdfAdapter.updatePdfFiles(it)
        }
    }


    override fun onResume() {
        super.onResume()
        if (binding.switchView.getSavedMode() == CustomListGridSwitchView.SwitchMode.GRID) {
            binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
            pdfAdapter.isGridView = true
            pdfAdapter.notifyDataSetChanged()
            binding.switchView.setMode(CustomListGridSwitchView.SwitchMode.GRID)

        } else {
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            pdfAdapter.isGridView = false
            pdfAdapter.notifyDataSetChanged()
            binding.switchView.setMode(CustomListGridSwitchView.SwitchMode.LIST)

        }
    }

}