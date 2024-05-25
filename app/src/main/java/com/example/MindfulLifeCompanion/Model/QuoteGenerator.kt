package com.example.MindfulLifeCompanion.Model

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.ContextCompat.getString
import com.example.MindfulLifeCompanion.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException
import java.time.LocalDate
import kotlin.coroutines.suspendCoroutine


class QuoteGenerator {
    private val client = OkHttpClient()
    var currentQuote = Quote()

    constructor() {
        currentQuote = Quote()
    }

    constructor(preferences: SharedPreferences) {
        val pinnedText = preferences.getString("pinned_quote_text", null)
        val pinnedAuthor = preferences.getString("pinned_quote_author", null)
        val pinnedCategory = preferences.getString("pinned_quote_category", null)
        val pinnedBookmarked = preferences.getBoolean("pinned_quote_bookmarked", false)
        currentQuote = Quote(pinnedText, pinnedAuthor, pinnedCategory, pinnedBookmarked, true, null)
    }

    suspend fun updateQuoteFromAPI(category: String, context: Context): Quote = suspendCoroutine { continuation ->
        var generatedQuote = Quote();
        val request = Request.Builder()
            .url("https://api.api-ninjas.com/v1/quotes?category=${category}").header("X-Api-Key", getString(context, R.string.api_key))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                generatedQuote.quoteCategory = ""
                generatedQuote.quoteText =
                    "Error occurred when trying to update quote. Are you online?"
                generatedQuote.quoteAuthor = ""
                continuation.resumeWith(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                val grabbedQuote = JSONArray(response.body!!.string()).getJSONObject(0)
                val quoteText = grabbedQuote.getString("quote")
                val quoteAuthor = grabbedQuote.getString("author")
                val quoteCategory = grabbedQuote.getString("category")
                val catString = quoteCategory[0].uppercaseChar() + quoteCategory.substring(1)
                generatedQuote = Quote(quoteText, quoteAuthor, catString)
                generatedQuote.pinned = false

                continuation.resumeWith(Result.success(generatedQuote))
            }
        })
    }

    fun saveToDefault(sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        editor.putString("default_quote_text", currentQuote.quoteText)
        editor.putString("default_quote_author", currentQuote.quoteAuthor)
        editor.putString("default_quote_category", currentQuote.quoteCategory)
        editor.putBoolean("default_quote_bookmarked", currentQuote.bookmarked!!)
        editor.putBoolean("default_quote_pinned", true)
        editor.putString("default_quote_ID", currentQuote.id)

        editor.commit()
    }

    fun saveToDefaultPinned(sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        editor.putString("pin_quote_text", currentQuote.quoteText)
        editor.putString("pin_quote_author", currentQuote.quoteAuthor)
        editor.putString("pin_quote_category", currentQuote.quoteCategory)
        editor.putBoolean("pin_quote_bookmarked", currentQuote.bookmarked!!)
        editor.putBoolean("pin_quote_pinned", true)
        editor.putString("pin_quote_ID", currentQuote.id)

        editor.commit()
    }

    fun getDefaultPinned(sharedPreferences: SharedPreferences): Quote {
        val text = sharedPreferences.getString("pin_quote_text", null)
        val author = sharedPreferences.getString("pin_quote_author", null)
        val category = sharedPreferences.getString("pin_quote_category", null)
        val bookmarked = sharedPreferences.getBoolean("pin_quote_bookmarked", false)
        val pinned = sharedPreferences.getBoolean("pin_quote_pinned", true)
        val id = sharedPreferences.getString("pin_quote_ID", null)

        return Quote(text, author, category, bookmarked, pinned, id)
    }

    fun getDefaultQuote(sharedPreferences: SharedPreferences): Quote {
        val text = sharedPreferences.getString("default_quote_text", null)
        val author = sharedPreferences.getString("default_quote_author", null)
        val category = sharedPreferences.getString("default_quote_category", null)
        val bookmarked = sharedPreferences.getBoolean("default_quote_bookmarked", false)
        val pinned = sharedPreferences.getBoolean("default_quote_pinned", true)
        val id = sharedPreferences.getString("default_quote_ID", null)
        return Quote(text, author, category, bookmarked, pinned, id)
    }

    suspend fun dailyQuoteUpdate(sharedPreferences: SharedPreferences, context: Context) {
        val lastUpdated = LocalDate.parse(
            sharedPreferences.getString(
                "quote_last_updated",
                LocalDate.now().minusDays(1).toString()
            )
        )
        val currentDate = LocalDate.now()
        val defaultCategory = sharedPreferences.getString("quote_category_preference", "Happiness")

        if (currentDate > lastUpdated) {
            currentQuote = updateQuoteFromAPI(defaultCategory!!, context)
            sharedPreferences.edit().putString("quote_last_updated", currentDate.toString()).apply()
            saveToDefaultPinned(sharedPreferences)
            saveToDefault(sharedPreferences)
        } else {
            updateQuoteDisplayFromPinned(sharedPreferences)
        }
    }

    private fun updateQuoteDisplayFromPinned(userPreferences: SharedPreferences) {
        val quoteText = userPreferences.getString("pin_quote_text", null)
        val quoteAuthor = userPreferences.getString("pin_quote_author", null)
        val quoteCategory = userPreferences.getString("pin_quote_category", null)
        val quoteBookmarked = userPreferences.getBoolean("pin_quote_bookmarked", true)
        val quoteID = userPreferences.getString("pin_quote_ID", null)

        println("currently pinned quote" + currentQuote)

        currentQuote = Quote(quoteText, quoteAuthor, quoteCategory, quoteBookmarked, true, quoteID)
    }

    override fun toString(): String {
        return ("${currentQuote.quoteText}\n" +
                "${currentQuote.quoteAuthor}\n" +
                "${currentQuote.quoteCategory}\n" +
                "${currentQuote.bookmarked}\n" +
                "${currentQuote.pinned}\n" +
                "${currentQuote.id}")
    }
}
