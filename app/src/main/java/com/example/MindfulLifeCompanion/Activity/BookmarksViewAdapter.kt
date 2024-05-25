package com.example.MindfulLifeCompanion.Activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.MindfulLifeCompanion.Activity.BookmarksViewAdapter.BookmarkViewHolder
import com.example.MindfulLifeCompanion.Model.Quote
import com.example.MindfulLifeCompanion.R

class BookmarksViewAdapter(
    private val bookmarkedQuotesModels: ArrayList<Quote>,
    private val listener: RecyclerViewEvent,
) : RecyclerView.Adapter<BookmarkViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_view_row, parent, false)
        return BookmarkViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        holder.quoteText.text = bookmarkedQuotesModels[position].quoteText.toString()
        holder.quoteAuthor.text = bookmarkedQuotesModels[position].quoteAuthor.toString()
        holder.quoteCategory.text = bookmarkedQuotesModels[position].quoteCategory.toString()
    }

    override fun getItemCount(): Int {
        return bookmarkedQuotesModels.size
    }

    fun getPositionFromID(string: String): Int {
        for (quote in bookmarkedQuotesModels) {
            if (string == quote.id) {
                return bookmarkedQuotesModels.indexOf(quote)
            }
        }
        return -1
    }
    fun getPosition(quote: Quote) : Int {
        return bookmarkedQuotesModels.indexOf(quote)
    }
    fun removeItem(position: Int) {
        bookmarkedQuotesModels.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, bookmarkedQuotesModels.size)
    }

    fun addItem(quote: Quote) {
        bookmarkedQuotesModels.add(0, quote)
        notifyItemInserted(0)
    }
    interface RecyclerViewEvent {
        fun onItemClick(position: Int)
        fun onDeleteClick(position: Int)
    }

    inner class BookmarkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val quoteText: TextView = itemView.findViewById(R.id.rv_quote)
        val quoteAuthor: TextView = itemView.findViewById(R.id.rv_author)
        val quoteCategory: TextView = itemView.findViewById(R.id.rv_category)
        val deleteButton : ImageView = itemView.findViewById(R.id.rv_delete_btn)

        init {
            itemView.setOnClickListener(this)
            deleteButton.setOnClickListener{
                listener.onDeleteClick(adapterPosition)
            }
        }

        override fun onClick(p0: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }

    }
}
