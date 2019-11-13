package com.example.nonogramapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.nonogramapp.parse_nonogram.NonogramReader

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val nonogramParser = NonogramReader(applicationContext.assets.open("mypattern.json").bufferedReader())
        val nonogram = nonogramParser.read()
    }
}
