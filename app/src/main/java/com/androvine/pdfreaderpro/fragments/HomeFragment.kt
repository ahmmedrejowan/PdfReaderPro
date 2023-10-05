package com.androvine.pdfreaderpro.fragments

import android.os.Bundle
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

            Log.e("HomeFragment", "onViewCreated: total pdf files: ${pdfFiles.size}")

        }

    }

    private fun setUpInitialView() {


    }


}