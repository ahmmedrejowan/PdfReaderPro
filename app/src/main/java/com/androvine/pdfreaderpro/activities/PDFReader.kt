package com.androvine.pdfreaderpro.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.androvine.pdfreaderpro.databinding.ActivityPdfreaderBinding
import com.androvine.pdfreaderpro.utils.FormattingUtils.Companion.resizeName
import java.io.File

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
        binding.customPdfView.fromFile(file)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .defaultPage(0)
            .enableAnnotationRendering(true)
            .password(null)
            .scrollHandle(null)
            .enableAntialiasing(true)
            .spacing(0)
            .load()


    }


}