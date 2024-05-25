package com.example.MindfulLifeCompanion.Activity

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.MindfulLifeCompanion.Model.Quote
import com.example.MindfulLifeCompanion.Model.QuoteGenerator
import com.example.MindfulLifeCompanion.R
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.util.Date


class QuoteGeneratorActivity : AppCompatActivity(), BookmarksViewAdapter.RecyclerViewEvent {
    lateinit var generateQuoteButton: Button
    lateinit var quoteDisplayText: TextView
    lateinit var quoteDisplayAuthor: TextView
    lateinit var categoryList: Spinner
    lateinit var quoteGenerator: QuoteGenerator
    lateinit var bookmarkedQuotes: RecyclerView
    lateinit var pinButton: ImageView
    lateinit var backButton: ImageView
    lateinit var saveButton: Button
    var selectedCategory = ""
    lateinit var bookmarkedQuotesData: ArrayList<Quote>
    var auth = FirebaseAuth.getInstance()
    var db = Firebase.firestore
    lateinit var selectedQuote: Quote

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quote_generator)
        //initialize objects
        bookmarkedQuotesData = ArrayList()
        //Binding objects to the view

        generateQuoteButton = findViewById(R.id.generateQuote)
        quoteDisplayText = findViewById(R.id.quote)
        quoteDisplayAuthor = findViewById(R.id.author)
        saveButton = findViewById(R.id.saveQuote)
        categoryList = findViewById(R.id.selectCategories)
        quoteGenerator = QuoteGenerator()
        bookmarkedQuotes = findViewById(R.id.bookmarkedQuotesRV)
        pinButton = findViewById(R.id.pin_btn)
        backButton = findViewById(R.id.back_from_quoteGen_btn)

        //Async function to update data after it is available
        generateQuoteButton.setOnClickListener() {
            lifecycleScope.launch {
                selectedQuote = quoteGenerator.updateQuoteFromAPI(selectedCategory, this@QuoteGeneratorActivity)
                updateQuoteDisplay(selectedQuote)
            }
        }

        backButton.setOnClickListener {
            finish()
        }

        pinButton.setOnClickListener() {

            if (quoteGenerator.currentQuote.id == selectedQuote.id) {
                selectedQuote.pinned = !selectedQuote.pinned

            } else {
                quoteGenerator.currentQuote.pinned = !quoteGenerator.currentQuote.pinned
                selectedQuote.pinned = !selectedQuote.pinned
            }


            if (selectedQuote.pinned) {
                quoteGenerator.currentQuote = selectedQuote
                updatePin(quoteGenerator.currentQuote)
                intent.putExtra("quoteText", quoteGenerator.currentQuote.quoteText)
                intent.putExtra("quoteAuthor", quoteGenerator.currentQuote.quoteAuthor)
                intent.putExtra("quoteCategory", quoteGenerator.currentQuote.quoteCategory)
                intent.putExtra("quoteID", quoteGenerator.currentQuote.id)
                intent.putExtra("quoteBookmarked", quoteGenerator.currentQuote.bookmarked)
                intent.putExtra("quotePinned", quoteGenerator.currentQuote.pinned)
                setResult(123, intent)
            } else {
                updatePin(selectedQuote)
                setResult(0, intent)
            }


        }

        saveButton.setOnClickListener() {
            val currentQuote = selectedQuote

            if (!currentQuote.bookmarked!!) {
                //bookmarks quote by saving to database
                db.collection("user").document(auth.uid!!).collection("SavedQuotes").add(
                    hashMapOf(
                        "quote_text" to currentQuote.quoteText,
                        "quote_author" to currentQuote.quoteAuthor,
                        "quote_category" to currentQuote.quoteCategory,
                        "entry_date" to Timestamp(Date())
                    )
                ).addOnSuccessListener { result ->
                    currentQuote.bookmarked = true;
                    currentQuote.id = result.id
                    (bookmarkedQuotes.adapter as BookmarksViewAdapter).addItem(currentQuote)
                    bookmarkedQuotes.smoothScrollToPosition(0)
                }
                    .addOnFailureListener { result ->
                        Log.d(ContentValues.TAG, result.stackTraceToString())
                    }
            }
        }


        //Retrieve current quote from Dashboard
        quoteGenerator.currentQuote = getQuoteFromDashboard()


        setUpBookmarkedQuotesModels().addOnSuccessListener {
            val bookMarksViewAdapter = BookmarksViewAdapter(bookmarkedQuotesData, this)
            bookmarkedQuotes.adapter = bookMarksViewAdapter
            bookmarkedQuotes.layoutManager = LinearLayoutManager(this@QuoteGeneratorActivity)

            if (quoteGenerator.currentQuote.bookmarked == true) {
                val bookmarkAdapter = bookmarkedQuotes.adapter as BookmarksViewAdapter
                quoteGenerator.currentQuote =
                    bookmarkedQuotesData[bookmarkAdapter.getPositionFromID(
                        quoteGenerator.currentQuote.id!!
                    )]

            }
        }

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.categories,
            R.layout.quote_category_spinner_theme
        )

        fillCategoryList(adapter)

        selectedQuote = quoteGenerator.currentQuote
        updatePin(selectedQuote)
    }

    //Retrieve current quote from Dashboard
    private fun getQuoteFromDashboard(): Quote {
        val quoteText = intent.getStringExtra("quoteText")
        val quoteAuthor = intent.getStringExtra("quoteAuthor")
        val quoteCategory = intent.getStringExtra("quoteCategory")
        val quoteBookmarked = intent.getBooleanExtra("quoteBookmarked", false)
        val quoteID = intent.getStringExtra("quoteID")
        val quotePinned = intent.getBooleanExtra("quotePinned", true)

        return Quote(quoteText, quoteAuthor, quoteCategory, quoteBookmarked, quotePinned, quoteID)
    }

    private fun fillCategoryList(adapter: ArrayAdapter<CharSequence>) {

        categoryList.adapter = adapter

        categoryList.setSelection(adapter.getPosition(quoteGenerator.currentQuote.quoteCategory))

        categoryList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                selectedCategory = "happiness"
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 == 0) {
                    selectedCategory = ""
                } else {
                    if (p0 != null) {
                        selectedCategory = p0.getItemAtPosition(p2).toString()
                    }
                }
            }
        }
    }

    private fun setUpBookmarkedQuotesModels(): Task<Void> {
        val taskCompletionSource = TaskCompletionSource<Void>()
        val task = taskCompletionSource.task

        db.collection("user").document(auth.currentUser!!.uid).collection("SavedQuotes")
            .orderBy("entry_date", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { result ->
                for (quote in result) {
                    val qt = quote.get("quote_text").toString()
                    val qa = quote.get("quote_author").toString()
                    val qc = quote.get("quote_category").toString()
                    val qid = quote.id
                    if (qid == quoteGenerator.currentQuote.id) {
                        bookmarkedQuotesData.add(Quote(qt, qa, qc, true, true, qid))

                    } else {
                        bookmarkedQuotesData.add(Quote(qt, qa, qc, true, false, qid))
                    }
                }
                taskCompletionSource.setResult(null)
            }
        return task
    }

    override fun onStart() {
        super.onStart()

        updateQuoteDisplay(quoteGenerator.currentQuote)
    }

    override fun onItemClick(position: Int) {
        selectedQuote = bookmarkedQuotesData[position]
        updateQuoteDisplay(selectedQuote)
    }

    override fun onDeleteClick(position: Int) {
        val selectedQuote = bookmarkedQuotesData[position]
        db.collection("user").document(auth.currentUser!!.uid).collection("SavedQuotes")
            .document(selectedQuote.id!!).delete()
        val bookmarksAdapter = bookmarkedQuotes.adapter as BookmarksViewAdapter
        selectedQuote.bookmarked = false;
        bookmarksAdapter.removeItem(position)
        val userPreferences = getSharedPreferences("User_" + auth.currentUser?.uid, MODE_PRIVATE)
        if (selectedQuote.id == userPreferences.getString("pin_quote_ID", null)) {
            userPreferences.edit().putBoolean("pin_quote_bookmarked", false).remove("pin_quote_ID")
                .commit()
        }
        if (selectedQuote.id == userPreferences.getString("default_quote_ID", null)) {
            userPreferences.edit().putBoolean("default_quote_bookmarked", false)
                .remove("default_quote_ID").commit()
        }
    }

    private fun updatePin(selected: Quote) {
        if (selected.pinned) {
            pinButton.setImageResource(R.drawable.pin_btn_filled)
        } else {
            pinButton.setImageResource(R.drawable.pin_btn_outline)
        }
    }

    private fun updateQuoteDisplay(selected: Quote) {
        quoteDisplayText.text = selected.quoteText
        quoteDisplayAuthor.text = "-${selected.quoteAuthor}"
        val stringAdapter = categoryList.adapter as ArrayAdapter<String>
        categoryList.setSelection(stringAdapter.getPosition(selected.quoteCategory))
        updatePin(selected)
    }
}