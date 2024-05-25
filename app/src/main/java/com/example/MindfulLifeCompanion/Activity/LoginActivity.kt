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
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

open class LoginActivity : AppCompatActivity() {

    lateinit var editTextPassword: EditText
    lateinit var editTextEmail: EditText
    lateinit var buttonReg: TextView
    lateinit var auth: FirebaseAuth
    lateinit var progressBar: ProgressBar
    lateinit var login: Button
    lateinit var forgotPassBtn: TextView

    fun logIn(email: String, password: String) {

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show()
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show()
        } else {
            progressBar.visibility = View.VISIBLE
            try {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            if (user != null && user.isEmailVerified) {
                                progressBar.visibility = View.GONE
                                // Sign in success, update UI with the signed-in user's information
                                Toast.makeText(baseContext, "Login Successful.", Toast.LENGTH_SHORT)
                                    .show()
                                val intent = Intent(applicationContext, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                progressBar.visibility = View.GONE
                                Toast.makeText(
                                    baseContext,
                                    "Please verify your email before logging in.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            // Handle login failure
                            val exception = task.exception as? FirebaseException
                            if (exception != null) {
                                Toast.makeText(
                                    this,
                                    exception.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Login Failed Due to Unknown Reasons",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            progressBar.visibility = View.GONE
                        }
                    }
            } catch (e: FirebaseException) {
                e.printStackTrace()
            }

        }
    }

    public override fun onStart() {
        super.onStart()
        // Initialize firebase user
        val currentUser: FirebaseUser? = auth.currentUser
        // Check condition
        if (currentUser != null && currentUser.isEmailVerified) {
            // When user already sign in redirect to profile activity
            startActivity(
                Intent(
                    this@LoginActivity,
                    MainActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        editTextEmail = findViewById(R.id.username_input)
        editTextPassword = findViewById(R.id.password_input)
        buttonReg = findViewById(R.id.registertext)
        progressBar = findViewById(R.id.progressBar)
        login = findViewById(R.id.login_btn)
        forgotPassBtn = findViewById(R.id.forgot_password_btn)

        buttonReg.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }

        login.setOnClickListener() {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            logIn(email, password)
        }

        forgotPassBtn.setOnClickListener {
            startActivity(
                Intent(this@LoginActivity, ResetPassword::class.java)
            )
        }
    }
}
