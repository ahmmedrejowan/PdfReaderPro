package com.androvine.pdfreaderpro.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.androvine.pdfreaderpro.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private val binding: FragmentHomeBinding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Recent"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Favorite"))

    }


}