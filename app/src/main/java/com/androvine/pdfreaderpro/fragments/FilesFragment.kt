package com.androvine.pdfreaderpro.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.androvine.pdfreaderpro.adapter.PdfAdapter
import com.androvine.pdfreaderpro.databinding.FragmentFilesBinding
import com.androvine.pdfreaderpro.vms.PdfListViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


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

        pdfAdapter = PdfAdapter(mutableListOf())
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = pdfAdapter

        pdfListViewModel.pdfFiles.observe(viewLifecycleOwner) { pdfFiles ->
            pdfAdapter.updatePdfFiles(pdfFiles)

            if (pdfFiles.isEmpty()) {
                binding.noFileLayout.visibility = View.VISIBLE
            } else {
                binding.noFileLayout.visibility = View.GONE
            }

        }


    }

}