package com.androvine.pdfreaderpro.activities

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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


        onBackPressedDispatcher.addCallback(this) {
            val currentFragment = getCurrentFragment()
            if (currentFragment is FolderFragment) {
                if (currentFragment.handleFragmentBackPressed()) {
                    return@addCallback
                }
            }
            if (binding.viewPager.currentItem != 0) {
                binding.viewPager.setCurrentItem(0, false)
                return@addCallback
            } else {
                finish()
            }
        }


    }

//    fun refreshMediaStore(context: Context) {
//        // Use MediaScannerConnection to scan the entire external storage directory
//        MediaScannerConnection.scanFile(
//            context,
//            arrayOf(Environment.getExternalStorageDirectory().toString()),
//            null
//        ) { _, _ ->
//        }
//    }


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

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }


}