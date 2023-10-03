package com.androvine.pdfreaderpro.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.androvine.pdfreaderpro.R
import com.androvine.pdfreaderpro.databinding.FragmentChildFolderBinding
import com.androvine.pdfreaderpro.interfaces.ChildFragmentsCommunication


class ChildFolderFragment : Fragment() {

    private val binding: FragmentChildFolderBinding by lazy {
        FragmentChildFolderBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        folderAdapter.setOnItemClickListener { folder ->
//            (parentFragment as? ChildFragmentsCommunication)?.onFolderSelected(folder.name)
//        }

    }

}