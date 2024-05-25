package com.example.MindfulLifeCompanion.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.MindfulLifeCompanion.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

@Suppress("DEPRECATION")
class GoogleLoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private var reqOneTap = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Initialize sign in options the client-id is copied form google-services.json file
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Initialize google sign in client
        googleSignInClient = GoogleSignIn.getClient(this@GoogleLoginActivity, googleSignInOptions)
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
                    this@GoogleLoginActivity,
                    MainActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            finish()
        }

        val intent: Intent = googleSignInClient.signInIntent
        // Start activity for result
        startActivityForResult(intent, reqOneTap)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check condition
        if (requestCode == reqOneTap) {
            // When request code is equal to 100 initialize task
            val signInAccountTask: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)

            // check condition
            if (signInAccountTask.isSuccessful) {
                // When google sign in successful initialize string
                val s = "Google sign in successful"
                // Display Toast
                displayToast(s)
                // Initialize sign in account
                try {
                    // Initialize sign in account
                    val googleSignInAccount = signInAccountTask.getResult(ApiException::class.java)
                    // Check condition
                    if (googleSignInAccount != null) {
                        // When sign in account is not equal to null initialize auth credential
                        val authCredential: AuthCredential = GoogleAuthProvider.getCredential(
                            googleSignInAccount.idToken, null
                        )
                        // Check credential
                        auth.signInWithCredential(authCredential)
                            .addOnCompleteListener(this) { task ->
                                // Check condition
                                if (task.isSuccessful) {
                                    // When task is successful redirect to profile activity
                                    startActivity(
                                        Intent(
                                            this@GoogleLoginActivity,
                                            MainActivity::class.java
                                        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                    // Display Toast
                                    displayToast("Firebase authentication successful")
                                } else {
                                    // When task is unsuccessful display Toast
                                    displayToast(
                                        "Authentication Failed :" + task.exception?.message
                                    )
                                    startActivity(
                                        Intent(
                                            this@GoogleLoginActivity,
                                            LoginActivity::class.java
                                        ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                }
                            }
                    }
                } catch (e: ApiException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun displayToast(s: String) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_SHORT).show()
    }
}