package com.example.MindfulLifeCompanion.Activity

import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.MindfulLifeCompanion.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ResetPassword : AppCompatActivity() {
    private lateinit var resetPasswordBtn: Button
    private lateinit var goBackBtn: Button
    private lateinit var recoveryEmailInputBox: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        resetPasswordBtn = findViewById(R.id.reset_password_btn)
        goBackBtn = findViewById(R.id.back_to_login_btn)
        recoveryEmailInputBox = findViewById(R.id.recovery_email_input)

        resetPasswordBtn.setOnClickListener {
            sendPasswordResetEmail(recoveryEmailInputBox.text.toString())
        }

        goBackBtn.setOnClickListener {
            finish()
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show()
        } else {
            Firebase.auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset sent to $email", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}