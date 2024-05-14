package com.rejowan.pdfreaderpro.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.rejowan.pdfreaderpro.fragments.FilesFragment
import com.rejowan.pdfreaderpro.fragments.FolderFragment
import com.rejowan.pdfreaderpro.fragments.HomeFragment

class FragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {


    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                HomeFragment()
            }

            1 -> {
                FilesFragment()
            }

            2 -> {
                FolderFragment()
            }

            else -> {
                HomeFragment()
            }
        }
    }


}