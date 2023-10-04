package com.androvine.pdfreaderpro.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.androvine.pdfreaderpro.R
import com.androvine.pdfreaderpro.adapter.FragmentAdapter
import com.androvine.pdfreaderpro.databinding.ActivityHomeBinding
import com.androvine.pdfreaderpro.fragments.FolderFragment

class Home : AppCompatActivity() {

    private val binding: ActivityHomeBinding by lazy {
        ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        setupBottomNav()


//        onBackPressedDispatcher.addCallback(this) {
//            if (binding.viewPager.currentItem == 2 && supportFragmentManager.backStackEntryCount > 0) {
//                supportFragmentManager.popBackStack()
//                true // Return true to indicate that the back press was handled
//            } else {
//                false // Return false to allow the system to handle the back press
//            }
//        }



    }

    override fun onBackPressed() {
        val currentFragment = getCurrentFragment()

        if (currentFragment is FolderFragment) {
            Log.d("BACK_PRESS", "Found FolderFragment")
            if (currentFragment.handleFragmentBackPressed()) {
                Log.d("BACK_PRESS", "Handled by FolderFragment")
                return
            }
        }

        super.onBackPressed()
    }

    private fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentByTag("f${binding.viewPager.currentItem}")
    }



    private fun setupBottomNav() {

        binding.viewPager.adapter = FragmentAdapter(supportFragmentManager, lifecycle)

        binding.viewPager.isUserInputEnabled = false

        val itemToPageMap = mapOf(
            R.id.nav_home to 0, R.id.nav_files to 1, R.id.nav_folder to 2
        )

        binding.bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_search -> {
                    val intent = Intent(this@Home, SearchPDF::class.java)
                    startActivity(intent)
                    false
                }

                else -> {
                    itemToPageMap[item.itemId]?.let {
                        binding.viewPager.setCurrentItem(it, false)
                        true
                    } ?: false
                }
            }

        }


        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.bottomNavView.menu.getItem(position).isChecked = true
            }
        })


    }




}