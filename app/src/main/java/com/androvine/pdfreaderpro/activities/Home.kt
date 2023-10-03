package com.androvine.pdfreaderpro.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.androvine.pdfreaderpro.R
import com.androvine.pdfreaderpro.adapter.FragmentAdapter
import com.androvine.pdfreaderpro.databinding.ActivityHomeBinding

class Home : AppCompatActivity() {

    private val binding: ActivityHomeBinding by lazy {
        ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        setupBottomNav()
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