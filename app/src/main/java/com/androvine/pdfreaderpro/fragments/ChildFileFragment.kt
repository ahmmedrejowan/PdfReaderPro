package com.androvine.pdfreaderpro.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.androvine.pdfreaderpro.databinding.FragmentChildFileBinding


class ChildFileFragment : Fragment() {

    private val binding: FragmentChildFileBinding by lazy {
        FragmentChildFileBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        arguments?.getString("SELECTED_FOLDER")?.let { folder ->
//            // Load files from the selected folder...
//        }


    }

}