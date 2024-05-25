package com.example.MindfulLifeCompanion.Activity

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.MindfulLifeCompanion.Model.Quote
import com.example.MindfulLifeCompanion.Model.QuoteGenerator
import com.example.MindfulLifeCompanion.R
import com.facebook.login.LoginManager
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class MainActivity : AppCompatActivity() {
    private lateinit var quoteGenerator: QuoteGenerator
    private lateinit var userPreferences: SharedPreferences
    private lateinit var quoteTextDisplay: TextView
    private lateinit var quoteAuthorDisplay: TextView
    private lateinit var quoteCategoryDisplay: TextView
    private lateinit var bookMark: ImageView
    private lateinit var progressBar: ProgressBar
    var auth = FirebaseAuth.getInstance()
    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        //Binds objects to the view
        val moodTrackerButton: ImageView = findViewById(R.id.moodTrackerBtn)
        val journalButton: ImageView = findViewById(R.id.journalBtn)
        val quoteGenButton: ImageView = findViewById(R.id.quoteBtn)
        val medicationReminderButton: ImageView = findViewById(R.id.medicationReminderBtn)
        val calendarButton: ImageView = findViewById(R.id.calendar_view)
        val settingButton: ImageView = findViewById(R.id.settingsBtn)
        val aboutButton: ImageView = findViewById(R.id.aboutBtn)
        val logoutButton: ImageView = findViewById(R.id.logout_btn)
        val quoteDisplay: LinearLayout = findViewById(R.id.quote_display)
        progressBar = findViewById(R.id.quoteLoadProgressBar)
        bookMark = findViewById(R.id.bookmark)
        quoteCategoryDisplay = findViewById(R.id.quote_category)
        quoteTextDisplay = findViewById(R.id.quote_text)
        quoteAuthorDisplay = findViewById(R.id.quote_author)
        userPreferences = getSharedPreferences("User_" + auth.currentUser?.uid, MODE_PRIVATE)
        quoteGenerator = QuoteGenerator(userPreferences)

        //Listener to update the quote displayed in dashboard if it is pinned from the quote gen activity
        val catcherForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == 123) {
                    val data = result.data
                    if (data != null) {
                        val newQuote = Quote(
                            data.getStringExtra("quoteText"),
                            data.getStringExtra("quoteAuthor"),
                            data.getStringExtra("quoteCategory"),
                            data.getBooleanExtra("quoteBookmarked", false),
                            data.getBooleanExtra("quotePinned", false),
                            data.getStringExtra("quoteID"),
                        )
                        quoteGenerator.currentQuote = newQuote
                        println("new quote:" + newQuote)
                        updateQuoteDisplay()
                        quoteGenerator.saveToDefaultPinned(userPreferences)
                    }
                } else if (result.resultCode == 0) {

                    lifecycleScope.launch {
                        quoteGenerator.currentQuote =
                            quoteGenerator.getDefaultPinned(userPreferences)
                        if (quoteGenerator.currentQuote.id == null) {
                            checkDatabaseAndUpdateBookmark()
                        }
                        quoteGenerator.saveToDefaultPinned(userPreferences)
                        updateQuoteDisplay()
                    }
                }
            }

        //set onclick Listeners for buttons
        quoteDisplay.setOnClickListener() {
            quoteGenButton.performClick()
        }

        journalButton.setOnClickListener {
            val intent = Intent(applicationContext, AddJournalEntryActivity::class.java)
            startActivity(intent)

        }

        moodTrackerButton.setOnClickListener {
            val intent = Intent(applicationContext, MoodTrackerActivity::class.java)
            startActivity(intent)
        }

        //Quote Generator Button
        quoteGenButton.setOnClickListener {
            val intent = Intent(applicationContext, QuoteGeneratorActivity::class.java)

            putExtrasCurrentQuote(intent)

            catcherForResult.launch(intent)
        }

        medicationReminderButton.setOnClickListener {
            val intent = Intent(applicationContext, MedicationReminderActivity::class.java)
            startActivity(intent)
        }

        calendarButton.setOnClickListener {
            val intent = Intent(applicationContext, CalendarView::class.java)
            startActivity(intent)
        }

        aboutButton.setOnClickListener {
            val intent = Intent(applicationContext, AboutActivity::class.java)
            startActivity(intent)
        }

        settingButton.setOnClickListener {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
        }


        logoutButton.setOnClickListener {
            logout()
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        //bookmark quote
        bookMark.setOnClickListener() {
            if (quoteGenerator.currentQuote.bookmarked!!) {
                bookMark.setImageResource(R.drawable.bookmark_icon_outline)
                //unbookmark quote
                db.collection("user").document(auth.uid!!).collection("SavedQuotes")
                    .document(quoteGenerator.currentQuote.id!!)
                    .delete().addOnSuccessListener { result ->
                        quoteGenerator.currentQuote.bookmarked = false;
                        quoteGenerator.currentQuote.id = null;
                    }
            } else {
                //bookmarks quote by saving to database
                bookMark.setImageResource(R.drawable.bookmark_icon_filled)
                db.collection("user").document(auth.uid!!).collection("SavedQuotes").add(
                    hashMapOf(
                        "quote_text" to quoteGenerator.currentQuote.quoteText,
                        "quote_author" to quoteGenerator.currentQuote.quoteAuthor,
                        "quote_category" to quoteGenerator.currentQuote.quoteCategory,
                        "entry_date" to Timestamp(Date())
                    )
                ).addOnSuccessListener { result ->
                    quoteGenerator.currentQuote.bookmarked = true;
                    quoteGenerator.currentQuote.id = result.id
                    userPreferences.edit().putString("pin_quote_ID", result.id)
                        .putBoolean("pin_quote_bookmarked", true).commit()
                }
                    .addOnFailureListener { result ->
                        Log.d(TAG, result.stackTraceToString())
                    }
            }
        }


        // Mood of the week
        refreshCalendar()
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            quoteGenerator.dailyQuoteUpdate(userPreferences, this@MainActivity)
            updateQuoteDisplay()
            progressBar.visibility = View.GONE
        }
    }

    private fun putExtrasCurrentQuote(intent: Intent) {
        intent.putExtra("quoteText", quoteGenerator.currentQuote.quoteText)
        intent.putExtra("quoteAuthor", quoteGenerator.currentQuote.quoteAuthor)
        intent.putExtra("quoteCategory", quoteGenerator.currentQuote.quoteCategory)
        intent.putExtra("quoteBookmarked", quoteGenerator.currentQuote.bookmarked)
        intent.putExtra("quotePinned", true)
        intent.putExtra("quoteID", quoteGenerator.currentQuote.id)
    }

    private fun updateQuoteDisplay() {
        quoteCategoryDisplay.text = quoteGenerator.currentQuote.quoteCategory
        quoteTextDisplay.text = quoteGenerator.currentQuote.quoteText
        quoteAuthorDisplay.text = ("- ${quoteGenerator.currentQuote.quoteAuthor}")
        if (quoteGenerator.currentQuote.bookmarked!!) {
            bookMark.setImageResource(R.drawable.bookmark_icon_filled)
        } else {
            bookMark.setImageResource(R.drawable.bookmark_icon_outline)
        }
    }

    private fun logout() {
        val mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser

        if (currentUser != null) {
            for (provider in currentUser.providerData) {
                val providerId = provider.providerId
                if (providerId == "facebook.com") {
                    // User logged in with Facebook
                    LoginManager.getInstance().logOut()
                    break
                } else if (providerId == "google.com") {
                    // User logged in with Google
                    mAuth.signOut()
                    break
                } else {
                    // User logged in with email and password
                    mAuth.signOut()
                    break
                }
            }
        }
    }


    private suspend fun checkDatabaseAndUpdateBookmark() = suspendCoroutine { continuation ->
        db.collection("user").document(auth.currentUser!!.uid).collection("SavedQuotes")
            .whereEqualTo("quote_text", quoteGenerator.currentQuote.quoteText).get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    quoteGenerator.currentQuote.bookmarked = true
                    quoteGenerator.currentQuote.id = result.documents[0].id
                }
                continuation.resume(value = "success")
            }
    }

    // Moods Of The Week implementation (Copied from CalendarView) is down here ==========

    fun getCurrentDaySlot(): Int {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        val dayOfTheWeek = sdf.format(Date())

        Log.d("Day", dayOfTheWeek)

        // Could be implemented better but these numbers getting returned represent the exact slot # in the calendar
        if (dayOfTheWeek.equals("Monday")) {
            return 1
        } else if (dayOfTheWeek.equals("Tuesday")) {
            return 2
        } else if (dayOfTheWeek.equals("Wednesday")) {
            return 3
        } else if (dayOfTheWeek.equals("Thursday")) {
            return 4
        } else if (dayOfTheWeek.equals("Friday")) {
            return 5
        } else if (dayOfTheWeek.equals("Saturday")) {
            return 6
        } else if (dayOfTheWeek.equals("Sunday")) {
            return 7
        } else {
            return -1 // this should never happen
        }
    }

    private val slotIds =
        arrayOf(R.id.Slot1, R.id.Slot2, R.id.Slot3, R.id.Slot4, R.id.Slot5, R.id.Slot6, R.id.Slot7)

    fun replaceCalendarImage(slotNum: Int, mood: String) {
        // viewbyid only takes an int, not strings :( so im gonna do this
//            val slotIds = arrayOf(R.id.Slot1, R.id.Slot2, R.id.Slot3, R.id.Slot4, R.id.Slot5, R.id.Slot6, R.id.Slot7, R.id.Slot8, R.id.Slot9, R.id.Slot10, R.id.Slot11, R.id.Slot12, R.id.Slot13, R.id.Slot14, R.id.Slot15, R.id.Slot16, R.id.Slot17, R.id.Slot18, R.id.Slot19, R.id.Slot20, R.id.Slot21)

        val selectedSlotId = slotIds[slotNum - 1]
        val selectedImageView: ImageView = findViewById(selectedSlotId)

        if (mood == "Happy") {
            selectedImageView.setImageResource(R.drawable.mood_icon_happy_512px)
        } else if (mood == "Angry") {
            selectedImageView.setImageResource(R.drawable.mood_icon_angry_512px)
        } else if (mood == "Nervous") {
            selectedImageView.setImageResource(R.drawable.mood_icon_nervous_512px)
        } else if (mood == "Productive") {
            selectedImageView.setImageResource(R.drawable.mood_icon_productive_512px)
        } else if (mood == "Sad") {
            selectedImageView.setImageResource(R.drawable.mood_icon_sad_512px)
        }
    }

    private fun refreshCalendar() {
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

        for (i in 1..getCurrentDaySlot()) {
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

            for (i in 0..datesWithMoods.size - 1) {
                if (dates.contains(datesWithMoods[i])) {
                    replaceCalendarImage(
                        getCurrentDaySlot() - dates.indexOf(datesWithMoods[i]),
                        moods[i]
                    );
                }
            }
        }
    }
}