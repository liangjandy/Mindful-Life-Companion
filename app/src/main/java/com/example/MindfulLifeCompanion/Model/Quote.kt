package com.example.MindfulLifeCompanion.Model

class Quote {
    var quoteText: String? = null
    var quoteAuthor: String? = null
    var quoteCategory: String? = null
    var bookmarked: Boolean? = false
    var pinned: Boolean = false
    var id: String? = null

    constructor(quoteText: String?, quoteAuthor: String?, quoteCategory: String?) {
        this.quoteText = quoteText
        this.quoteAuthor = quoteAuthor
        this.quoteCategory = quoteCategory
    }

    constructor(
        quoteText: String?,
        quoteAuthor: String?,
        quoteCategory: String?,
        bookmarked: Boolean?,
        pinned: Boolean,
        id: String?
    ) {
        this.quoteText = quoteText
        this.quoteAuthor = quoteAuthor
        this.quoteCategory = quoteCategory
        this.bookmarked = bookmarked
        this.id = id
        this.pinned = pinned
    }

    constructor()

    override fun toString(): String {
        return ("${quoteText} ${quoteAuthor} ${quoteCategory} ${bookmarked} ${pinned} ${id}")
    }
}