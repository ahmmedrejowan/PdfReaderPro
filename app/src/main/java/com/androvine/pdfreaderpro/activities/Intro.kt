package com.androvine.pdfreaderpro.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.androvine.pdfreaderpro.R
import com.androvine.pdfreaderpro.databinding.ActivityIntroBinding

class Intro : AppCompatActivity() {

    private val binding: ActivityIntroBinding by lazy {
        ActivityIntroBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
    }


}