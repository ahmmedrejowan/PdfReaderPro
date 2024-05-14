package com.rejowan.pdfreaderpro.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rejowan.pdfreaderpro.R
import com.rejowan.pdfreaderpro.databinding.FragmentFolderBinding
import com.rejowan.pdfreaderpro.interfaces.ChildFragmentsCommunication


class FolderFragment : Fragment(), ChildFragmentsCommunication {


    private val binding: FragmentFolderBinding by lazy {
        FragmentFolderBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ChildFolderFragment()).commit()


    }

    override fun onFolderSelected(folder: String) {

        val filesFragment = ChildFileFragment().apply {
            arguments = Bundle().apply {
                putString("SELECTED_FOLDER", folder)
            }
        }

        childFragmentManager.beginTransaction().replace(R.id.fragment_container, filesFragment)
            .addToBackStack(null).commit()

    }

    fun handleFragmentBackPressed(): Boolean {
        if (childFragmentManager.backStackEntryCount > 0) {
            childFragmentManager.popBackStack()
            return true // Back press was handled by the fragment
        }
        return false // Back press was not handled by the fragment
    }



}