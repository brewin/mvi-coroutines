package com.github.brewin.mvicoroutines.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.brewin.mvicoroutines.R
import kotlinx.android.synthetic.main.app_activity.*

class AppActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_activity)
        setSupportActionBar(toolbar)
        supportActionBar?.title = null
    }
}