package com.androvine.pdfreaderpro.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.androvine.pdfreaderpro.R
import com.androvine.pdfreaderpro.adapter.FragmentAdapter
import com.androvine.pdfreaderpro.databaseRecent.RecentDBVM
import com.androvine.pdfreaderpro.databinding.ActivityHomeBinding
import com.androvine.pdfreaderpro.fragments.FolderFragment
import com.androvine.pdfreaderpro.vms.PdfListViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class Home : AppCompatActivity() {

    private val binding: ActivityHomeBinding by lazy {
        ActivityHomeBinding.inflate(layoutInflater)
    }

    private val pdfListViewModel: PdfListViewModel by viewModel()
    private val recentViewModel : RecentDBVM by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        setupBottomNav()

        recentViewModel.allRecent.observe(this) { recentEntities ->
            // Update your UI with the list of recentEntities.
            // For instance, you might update an adapter of a RecyclerView.

            Log.e("Home", "Recent Size: " + recentEntities.size)
            if (recentEntities.isNotEmpty()) {
                Log.e("Recent", "Recent 0 Name: " + recentEntities[0].name)
            }
        }


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






    /* ways to add room database
 val newRecentEntry = RecentEntity(
 name = "example.pdf",
 path = "/path/to/example.pdf",
 size = 1024L,
 dateModified = System.currentTimeMillis(),
 parentFolderName = "/path/to/",
 lastOpened = System.currentTimeMillis(),
 lastPageOpened = 1
 )

 viewModel.insertRecent(newRecentEntry)


  // Let's say you've got an instance of RecentEntity you wish to delete:
val entryToDelete = ... // fetched or selected somehow

viewModel.deleteRecent(entryToDelete)




// Let's say you've got an instance of RecentEntity you wish to update:
val entryToUpdate = ... // fetched or selected somehow

entryToUpdate.name = "newName.pdf"
// make other changes as needed

viewModel.updateRecent(entryToUpdate)


viewModel.allRecent.observe(viewLifecycleOwner, Observer { recentEntities ->
    // Update your UI with the list of recentEntities.
    // For instance, you might update an adapter of a RecyclerView.
})






    */












}