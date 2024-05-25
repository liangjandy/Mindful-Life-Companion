package com.example.MindfulLifeCompanion.Activity

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.MindfulLifeCompanion.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import java.util.Date
import java.util.Locale


class MoodTrackerActivity : AppCompatActivity() {

    private lateinit var moodButtonHappy : Button
    private lateinit var moodButtonProductive : Button
    private lateinit var moodButtonSad : Button
    private lateinit var moodButtonNervous : Button
    private lateinit var moodButtonAngry : Button
    private lateinit var backToDashboard : Button

    private fun storeMood(mood: String){
        val sdf = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
        val currentDate = sdf.format(Date())

        val moodEntry = hashMapOf(
            "Date" to currentDate,
            "Mood" to mood,
        )

        val db = Firebase.firestore
        val firebaseAuth = FirebaseAuth.getInstance()

        val userId = firebaseAuth.currentUser!!.uid
        val userRef = db.collection("user").document(userId)
        userRef.collection("MoodEntries").document(currentDate).set(moodEntry)
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_tracker)

        moodButtonHappy = findViewById(R.id.mood_happy)
        moodButtonProductive = findViewById(R.id.mood_productive)
        moodButtonSad = findViewById(R.id.mood_sad)
        moodButtonNervous = findViewById(R.id.mood_nervous)
        moodButtonAngry = findViewById(R.id.mood_angry)
        backToDashboard = findViewById(R.id.back)

        moodButtonHappy.setOnClickListener {
            storeMood("Happy")
            val intent = Intent(applicationContext, CalendarView::class.java)
            startActivity(intent)
            finish();
        }
        moodButtonProductive.setOnClickListener {
            storeMood("Productive")
            val intent = Intent(applicationContext, CalendarView::class.java)
            startActivity(intent)
            finish();
        }
        moodButtonSad.setOnClickListener {
            storeMood("Sad")
            val intent = Intent(applicationContext, CalendarView::class.java)
            startActivity(intent)
            finish();
        }
        moodButtonNervous.setOnClickListener {
            storeMood("Nervous")
            val intent = Intent(applicationContext, CalendarView::class.java)
            startActivity(intent)
            finish();
        }
        moodButtonAngry.setOnClickListener {
            storeMood("Angry")
            val intent = Intent(applicationContext, CalendarView::class.java)
            startActivity(intent)
            finish();
        }

        backToDashboard.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish();
        }
    }
}