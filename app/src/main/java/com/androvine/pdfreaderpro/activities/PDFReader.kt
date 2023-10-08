package com.androvine.pdfreaderpro.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.androvine.pdfreaderpro.R
import com.androvine.pdfreaderpro.databinding.ActivityPdfreaderBinding
import com.androvine.pdfreaderpro.reader.PdfViewer
import com.androvine.pdfreaderpro.reader.interfaces.OnErrorListener
import com.androvine.pdfreaderpro.reader.interfaces.OnPageChangedListener
import com.androvine.pdfreaderpro.reader.utils.PdfPageQuality
import com.androvine.pdfreaderpro.utils.FormattingUtils.Companion.resizeName
import java.io.File
import java.io.IOException

class PDFReader : AppCompatActivity() {

    private val binding: ActivityPdfreaderBinding by lazy {
        ActivityPdfreaderBinding.inflate(layoutInflater)
    }

    private var pdfPath: String = ""
    private var pdfName: String = "null"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        pdfPath = intent.getStringExtra("pdfPath").toString()
        pdfName = intent.getStringExtra("pdfName").toString()

        binding.ivBack.setOnClickListener {
            finish()
        }

        if (pdfName.isNotEmpty() && pdfPath.isNotEmpty()) {
            setupPdfView()
        }

    }

    private fun setupPdfView() {

        binding.title.text = resizeName(pdfName)

        val file = File(pdfPath)

        PdfViewer.Builder(binding.rootView).setMaxZoom(3f).setZoomEnabled(true)
            .quality(PdfPageQuality.QUALITY_1440).setOnErrorListener(object : OnErrorListener {
                override fun onFileLoadError(e: Exception) {
                    Log.e("PDFReader", "onFileLoadError: ${e.message}")
                }

                override fun onAttachViewError(e: Exception) {
                    Log.e("PDFReader", "onAttachViewError: ${e.message}")
                }

                override fun onPdfRendererError(e: IOException) {
                    Log.e("PDFReader", "onPdfRendererError: ${e.message}")
                }

            }).setOnPageChangedListener(object : OnPageChangedListener {
                override fun onPageChanged(page: Int, total: Int) {
                    binding.tvCounter.text = getString(R.string.pdf_page_counter, page, total)
                }

            }).build().load(file)

    }


}