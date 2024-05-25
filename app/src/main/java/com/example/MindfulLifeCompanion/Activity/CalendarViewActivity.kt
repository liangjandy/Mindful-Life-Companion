package com.example.MindfulLifeCompanion.Activity

import android.content.ContentValues.TAG
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.MindfulLifeCompanion.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.Locale



class CalendarView : AppCompatActivity() {

    private lateinit var backToDashboard : Button

    fun getCurrentDaySlot(): Int{
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        val dayOfTheWeek = sdf.format(Date())

        Log.d("Day", dayOfTheWeek)

        // Could be implemented better but these numbers getting returned represent the exact slot # in the calendar
        if (dayOfTheWeek.equals("Monday")){
            return 22
        }
        else if (dayOfTheWeek.equals("Tuesday")){
            return 23
        }
        else if (dayOfTheWeek.equals("Wednesday")){
            return 24
        }
        else if (dayOfTheWeek.equals("Thursday")){
            return 25
        }
        else if (dayOfTheWeek.equals("Friday")){
            return 26
        }
        else if (dayOfTheWeek.equals("Saturday")){
            return 27
        }
        else if (dayOfTheWeek.equals("Sunday")){
            return 28
        }
        else{
            return -1 // this should never happen
        }
    }
    private val slotIds = arrayOf(R.id.Slot1, R.id.Slot2, R.id.Slot3, R.id.Slot4, R.id.Slot5, R.id.Slot6, R.id.Slot7, R.id.Slot8, R.id.Slot9, R.id.Slot10, R.id.Slot11, R.id.Slot12, R.id.Slot13, R.id.Slot14, R.id.Slot15, R.id.Slot16, R.id.Slot17, R.id.Slot18, R.id.Slot19, R.id.Slot20, R.id.Slot21, R.id.Slot22, R.id.Slot23, R.id.Slot24, R.id.Slot25, R.id.Slot26, R.id.Slot27)

    fun replaceCalendarImage(slotNum: Int, mood: String){
            // viewbyid only takes an int, not strings :( so im gonna do this
//            val slotIds = arrayOf(R.id.Slot1, R.id.Slot2, R.id.Slot3, R.id.Slot4, R.id.Slot5, R.id.Slot6, R.id.Slot7, R.id.Slot8, R.id.Slot9, R.id.Slot10, R.id.Slot11, R.id.Slot12, R.id.Slot13, R.id.Slot14, R.id.Slot15, R.id.Slot16, R.id.Slot17, R.id.Slot18, R.id.Slot19, R.id.Slot20, R.id.Slot21)

            val selectedSlotId = slotIds[slotNum-1]
            val selectedImageView: ImageView = findViewById(selectedSlotId)

            if (mood == "Happy"){
                selectedImageView.setImageResource(R.drawable.mood_icon_happy_512px)
            }
            else if (mood == "Angry"){
                selectedImageView.setImageResource(R.drawable.mood_icon_angry_512px)
            }
            else if (mood == "Nervous"){
                selectedImageView.setImageResource(R.drawable.mood_icon_nervous_512px)
            }
            else if (mood == "Productive"){
                selectedImageView.setImageResource(R.drawable.mood_icon_productive_512px)
            }
            else if (mood == "Sad"){
                selectedImageView.setImageResource(R.drawable.mood_icon_sad_512px)
            }

    }

    // return for this function dont work, gonna do a cool move and not use this lol
    fun getMoodFromStringDate(stringDate: String): String{
        val db = Firebase.firestore
        val firebaseAuth = FirebaseAuth.getInstance()

        val userId = firebaseAuth.currentUser!!.uid
        val userRef = db.collection("user").document(userId)

        var moodToReturn: String = "n/a"

        userRef.collection("MoodEntries").document(stringDate).get().addOnSuccessListener { document ->
            moodToReturn = document.data?.get("Mood").toString()
        }
        return moodToReturn
    }

    private fun refreshCalendar(){
        // im going to first get the current day slot and set calendar image
        // to whatever today's mood is

        // then, im going to find yesterday's mood, decrement the slot ill replace image for
        // until it reaches slot 1
        val sdf = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        var dateToInsert = sdf.format(Date())
        var slotToInsert = getCurrentDaySlot()

        val db = Firebase.firestore
        val firebaseAuth = FirebaseAuth.getInstance()

        val userId = firebaseAuth.currentUser!!.uid
        val userRef = db.collection("user").document(userId)

        val dates = mutableListOf<String>()
        var datesWithMoods = mutableListOf<String>()
        var moods = mutableListOf<String>()

        for (i in 1.. getCurrentDaySlot()){
            dates.add(dateToInsert)
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            dateToInsert = sdf.format(calendar.time)
        }

        // https://firebase.google.com/docs/firestore/query-data/get-data
        // Im going to use this to get all data from MoodEntries collection

        userRef.collection("MoodEntries").get().addOnSuccessListener { documents ->
            for (document in documents) {
                datesWithMoods.add(document.data["Date"].toString())
                moods.add(document.data["Mood"].toString())
            }
            Log.d("MoodEntriesA", datesWithMoods.toString());
            Log.d("MoodEntriesA", datesWithMoods[0].toString());

            for (i in 0..datesWithMoods.size-1){
                if (dates.contains(datesWithMoods[i])){
                    replaceCalendarImage(getCurrentDaySlot() - dates.indexOf(datesWithMoods[i]), moods[i]);
                }
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_view)

        backToDashboard = findViewById(R.id.back)

        refreshCalendar()

        backToDashboard.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}