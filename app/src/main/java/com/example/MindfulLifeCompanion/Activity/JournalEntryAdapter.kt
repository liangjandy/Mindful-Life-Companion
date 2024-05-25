package com.example.MindfulLifeCompanion.Activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.MindfulLifeCompanion.Activity.JournalEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.MindfulLifeCompanion.R
import com.google.firebase.auth.FirebaseAuth

class JournalEntryAdapter(private val listener: OnItemClickListener) :
    ListAdapter<JournalEntry, JournalEntryAdapter.JournalEntryViewHolder>(JournalEntryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalEntryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_journal_entry, parent, false)
        return JournalEntryViewHolder(view)
    }

    override fun onBindViewHolder(holder: JournalEntryViewHolder, position: Int) {
        val journalEntry = getItem(position)
        holder.bind(journalEntry)
    }

    inner class JournalEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {


        private val textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        private val textViewContent: TextView = itemView.findViewById(R.id.textViewContent)
        private val buttonDelete: Button = itemView.findViewById(R.id.buttonDeleteEntry)
        private val buttonEdit: Button = itemView.findViewById(R.id.buttonEditEntry)




        init {
            itemView.setOnClickListener(this)
            buttonDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemDeleteClick(getItem(position))
                }
            }
            buttonEdit.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemEditClick(getItem(position))
                }
            }
        }

        fun bind(journalEntry: JournalEntry) {

            textViewTitle.text = journalEntry.Title ?: "No Title"
            textViewContent.text = journalEntry.Content ?: "No Content"
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(getItem(position))
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(journalEntry: JournalEntry)
        fun onItemDeleteClick(journalEntry: JournalEntry)
        fun onItemEditClick(journalEntry: JournalEntry)
    }

    class JournalEntryDiffCallback : DiffUtil.ItemCallback<JournalEntry>() {
        override fun areItemsTheSame(oldItem: JournalEntry, newItem: JournalEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: JournalEntry, newItem: JournalEntry): Boolean {
            return oldItem == newItem
        }
    }
}