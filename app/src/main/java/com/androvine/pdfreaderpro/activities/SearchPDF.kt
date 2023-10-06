package com.androvine.pdfreaderpro.activities

import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.androvine.pdfreaderpro.adapter.PdfAdapter
import com.androvine.pdfreaderpro.dataClasses.PdfFile
import com.androvine.pdfreaderpro.databinding.ActivitySearchPdfBinding
import com.androvine.pdfreaderpro.interfaces.OnPdfFileClicked
import com.androvine.pdfreaderpro.vms.PdfListViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class SearchPDF : AppCompatActivity() {

    private val binding: ActivitySearchPdfBinding by lazy {
        ActivitySearchPdfBinding.inflate(layoutInflater)
    }

    private val pdfListViewModel: PdfListViewModel by viewModel()
    private lateinit var pdfAdapter: PdfAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        pdfAdapter =
            PdfAdapter(mutableListOf(), false, binding.recyclerView, object : OnPdfFileClicked {
                override fun onPdfFileRenamed(pdfFile: PdfFile, newName: String) {
                    pdfListViewModel.renamePdfFile(pdfFile, newName)
                }

                override fun onPdfFileDeleted(pdfFile: PdfFile) {
                    pdfListViewModel.deletePdfFile(pdfFile)

                }

            })

        binding.recyclerView.adapter = pdfAdapter

        binding.searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (TextUtils.isEmpty(newText)) {
                    pdfAdapter.updatePdfFiles(emptyList())
                    binding.noFileLayout.visibility = View.GONE
                } else {
                    val searchResults = pdfListViewModel.pdfFiles.value?.filter {
                        it.name.contains(
                            newText!!, true
                        )
                    } ?: emptyList()
                    if (searchResults.isEmpty()) {
                        binding.noFileLayout.visibility = View.VISIBLE
                        pdfAdapter.updatePdfFiles(emptyList())
                    } else {
                        binding.noFileLayout.visibility = View.GONE
                        pdfAdapter.updatePdfFiles(searchResults)
                    }
                }
                return true
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