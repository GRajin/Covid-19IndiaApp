package com.rajingangadharan.covid19india

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        supportActionBar?.hide()
        Log.d("covid19india", "About -> Actionbar Hidden")
    }
}