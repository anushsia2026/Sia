package com.anush.sia

import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val text = TextView(this)
        text.text = "Sia"
        text.textSize = 40f
        text.gravity = Gravity.CENTER
        setContentView(text)
    }
}
