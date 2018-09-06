package com.github.brewin.mvicoroutines.view.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.brewin.mvicoroutines.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
    }
}
