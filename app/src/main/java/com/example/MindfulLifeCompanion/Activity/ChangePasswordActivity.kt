package com.example.MindfulLifeCompanion.Activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.MindfulLifeCompanion.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var oldPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var changePasswordButton: Button
    private lateinit var backToSettings : Button
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        backToSettings = findViewById(R.id.back_to_settings)
        oldPasswordEditText = findViewById(R.id.old_password_edit_text)
        newPasswordEditText = findViewById(R.id.new_password_edit_text)
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text)
        changePasswordButton = findViewById(R.id.change_password_button)


        //BUTTON
        backToSettings.setOnClickListener {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            finish();
        }

        changePasswordButton.setOnClickListener {
            val oldPassword = oldPasswordEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // Perform validation here (e.g., check if fields are not empty, new password matches confirm password, etc.)

            // If validation passes, use Firebase Authentication to change the password
            val user = auth.currentUser
            if (user != null) {
                val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
                user.reauthenticate(credential)
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            user.updatePassword(newPassword)
                                .addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        // Password updated successfully
                                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                                        finish()
                                    } else {
                                        // Failed to update password
                                        Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            // Re-authentication failed
                            Toast.makeText(this, "Re-authentication failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
}
