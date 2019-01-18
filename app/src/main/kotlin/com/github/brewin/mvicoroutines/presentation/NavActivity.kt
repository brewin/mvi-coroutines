package com.github.brewin.mvicoroutines.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.brewin.mvicoroutines.R
import kotlinx.android.synthetic.main.nav_activity.*

class NavActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nav_activity)
        setSupportActionBar(toolbar)
        supportActionBar?.title = null
    }
}
