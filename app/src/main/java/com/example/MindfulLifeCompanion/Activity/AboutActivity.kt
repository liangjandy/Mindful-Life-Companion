package com.example.MindfulLifeCompanion.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.MindfulLifeCompanion.R

class AboutActivity: AppCompatActivity() {

    private lateinit var backToDashboard : Button
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_layout)

        backToDashboard = findViewById(R.id.back)



        backToDashboard.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish();
        }
    }
}
