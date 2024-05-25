package com.example.MindfulLifeCompanion.Activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.MindfulLifeCompanion.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.ktx.actionCodeSettings

class RegistrationActivity : AppCompatActivity() {

    lateinit var editTextPassword : EditText
    lateinit var editTextEmail : EditText
    lateinit var editTextConfirmPassword : EditText
    lateinit var buttonReg : Button
    lateinit var auth : FirebaseAuth
    lateinit var progressBar : ProgressBar
    lateinit var textView : TextView
    private val db: FirebaseFirestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = FirebaseAuth.getInstance()
        editTextEmail = findViewById(R.id.username_input)
        editTextPassword = findViewById(R.id.password_input)
        editTextConfirmPassword = findViewById(R.id.confirmPassword_input)
        buttonReg = findViewById(R.id.register_btn)
        progressBar = findViewById(R.id.progressBar)
        textView = findViewById(R.id.logintext)

        textView.setOnClickListener {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        buttonReg.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            val email: String = editTextEmail.text.toString()
            val password: String = editTextPassword.text.toString()
            val confirmPass: String = editTextConfirmPassword.text.toString()

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPass) {
                Toast.makeText(this, "Passwords Do Not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        progressBar.visibility = View.GONE
                        if (task.isSuccessful) {
                            val actionCodeSettings: ActionCodeSettings = actionCodeSettings {
                                // Set the URL the user will be redirected to after clicking the email verification link
                                url = "https://mindful-life-companion.firebaseapp.com/__/auth/action?mode=action&oobCode=code"
                                // Set the email address to be verified
                                setHandleCodeInApp(true)
                            }

                            // Send the verification email to the user
                            auth.currentUser?.sendEmailVerification(actionCodeSettings)?.addOnCompleteListener { verificationTask ->
                                if (verificationTask.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "Email Verification Sent.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Failed to Send Email Verification.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            val userMap = hashMapOf(
                                "email" to email,
                                "password" to password
                            )

                            val userId = FirebaseAuth.getInstance().currentUser!!.uid

                            db.collection("user").document(userId).set(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "New Profile Successfully Created!", Toast.LENGTH_SHORT)
                                        .show()
                                }

                            val intent = Intent(applicationContext, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            val exception = task.exception as? FirebaseException
                            if (exception != null) {
                                Toast.makeText(this, exception.message.toString(), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Registration Failed Due to Unknown Reasons", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            } catch (e: FirebaseException) {
                // Handle Firebase exception
            }
        }
    }
}
