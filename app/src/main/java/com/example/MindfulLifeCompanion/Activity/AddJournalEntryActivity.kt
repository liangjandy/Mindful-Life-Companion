package com.example.MindfulLifeCompanion.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import java.util.Date
import com.example.MindfulLifeCompanion.R

class AddJournalEntryActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var editTextTitle: EditText
    private lateinit var editTextContent: EditText
    private lateinit var buttonAddEntry: Button
    private lateinit var buttonDeleteEntry: Button
    private lateinit var buttonBackToMain: Button
    private var isEditMode = false
    private var entryId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_journal_entry)

        db = FirebaseFirestore.getInstance()

        editTextTitle = findViewById(R.id.editTextTitle)
        editTextContent = findViewById(R.id.editTextContent)
        buttonAddEntry = findViewById(R.id.buttonAddEntry)
        buttonDeleteEntry = findViewById(R.id.buttonDeleteEntry)
        buttonBackToMain = findViewById(R.id.buttonBackToMain)

        entryId = intent.getStringExtra("entryId")
        if (entryId != null) {
            isEditMode = true
            buttonAddEntry.text = "Update Entry"
            buttonDeleteEntry.visibility = View.VISIBLE
            fetchEntryFromFirestore(entryId!!)  // Ensure this method is correctly implemented
        }

        buttonAddEntry.setOnClickListener(addEntryClickListener)
        buttonDeleteEntry.setOnClickListener(deleteEntryClickListener)
        buttonBackToMain.setOnClickListener(backToMainClickListener)
    }

    private val addEntryClickListener = View.OnClickListener {
        val title = editTextTitle.text.toString()
        val content = editTextContent.text.toString()
        val currentDate = Date().toString() // Get current date and time

        val journalEntry = hashMapOf(
            "Title" to title,
            "Content" to content,
            "Date" to currentDate
        )

        if (isEditMode) {
            updateEntryInFirestore(entryId!!, journalEntry)
        } else {
            addEntryToFirestore(journalEntry)
        }
    }

    private val deleteEntryClickListener = View.OnClickListener {
        if (isEditMode) {
            deleteEntryFromFirestore(entryId!!)
        }
    }

    private val backToMainClickListener = View.OnClickListener {
        navigateToMainActivity()
    }

    private fun fetchEntryFromFirestore(entryId: String) {
        db.collection("user")
            .document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
            .collection("JournalEntries")
            .document(entryId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    editTextTitle.setText(document.getString("Title"))
                    editTextContent.setText(document.getString("Content"))
                } else {
                    Toast.makeText(this, "Entry not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch entry", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
    fun openJournalListActivity(view: View) {
        val intent = Intent(this, JournalListActivity::class.java)
        startActivity(intent)
    }

    private fun addEntryToFirestore(journalEntry: HashMap<String, String>) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val entryData = HashMap<String, Any>() // Create a new HashMap with Any type for values
            entryData.putAll(journalEntry) // Copy the contents of journalEntry to entryData

            db.collection("user")
                .document(userId)
                .collection("JournalEntries")
                .add(entryData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Entry added successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to add entry", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateEntryInFirestore(entryId: String, journalEntry: HashMap<String, String>) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("user")
                .document(userId)
                .collection("JournalEntries")
                .document(entryId)
                .set(journalEntry)
                .addOnSuccessListener {
                    Toast.makeText(this, "Entry updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update entry", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteEntryFromFirestore(entryId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("user")
                .document(userId)
                .collection("JournalEntries")
                .document(entryId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Entry deleted successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to delete entry", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}