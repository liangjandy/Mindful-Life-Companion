package com.example.MindfulLifeCompanion.Activity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class JournalEntry(
    var id: String? = null,
    var Title: String? = null,
    var Content: String? = null,
    var Date: String? = null
) {
    constructor() : this("", "", "", "")
    fun isRecent(): Boolean {
        return LocalDate.parse(Date, DateTimeFormatter.ISO_DATE).isAfter(LocalDate.now().minusDays(7))
    }
}
