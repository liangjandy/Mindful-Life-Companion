package com.example.MindfulLifeCompanion.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.MindfulLifeCompanion.R
import com.google.firebase.auth.FirebaseAuth

class JournalListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: JournalEntryAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var fabAddEntry: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal_list)

        // Initialize RecyclerView and adapter
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = JournalEntryAdapter(object : JournalEntryAdapter.OnItemClickListener {
            override fun onItemClick(journalEntry: JournalEntry) {
                // Handle item click if needed
            }
            override fun onItemEditClick(journalEntry: JournalEntry) {
                val intent = Intent(this@JournalListActivity, AddJournalEntryActivity::class.java)
                intent.putExtra("entryId", journalEntry.id) // Check that journalEntry.id is not null
                startActivity(intent)
            }
            override fun onItemDeleteClick(journalEntry: JournalEntry) {
                deleteEntryFromFirestore(journalEntry.id)
            }
        })
        recyclerView.adapter = adapter

        db = FirebaseFirestore.getInstance()

        // Initialize FloatingActionButton for adding new entries
        fabAddEntry = findViewById(R.id.fabAddEntry)
        fabAddEntry.setOnClickListener {
            startActivity(Intent(this, AddJournalEntryActivity::class.java))
        }

        // Load journal entries from Firestore
        loadJournalEntries()
    }
    private fun deleteEntryFromFirestore(entryId: String?) {
        entryId?.let {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                db.collection("user")
                    .document(userId)
                    .collection("JournalEntries")
                    .document(it)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Entry deleted successfully", Toast.LENGTH_SHORT).show()
                        loadJournalEntries() // Refresh the list after deletion
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to delete entry", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun loadJournalEntries() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("user")
                .document(userId)
                .collection("JournalEntries")
                .orderBy("Date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { snapshot ->
                    val entries = snapshot.documents.mapNotNull { document ->
                        document.toObject(JournalEntry::class.java)?.apply {
                            id = document.id // Ensure 'id' is being set here.
                        }
                    }
                    adapter.submitList(entries)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error fetching entries: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
}