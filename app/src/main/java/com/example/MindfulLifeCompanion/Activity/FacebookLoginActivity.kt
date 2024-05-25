package com.example.MindfulLifeCompanion.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.MindfulLifeCompanion.R
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.CallbackManager.Factory.create
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import java.util.Arrays


class FacebookLoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var callBackManager: CallbackManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facebook_login)
        auth = FirebaseAuth.getInstance()

        callBackManager = create()

        LoginManager.getInstance().registerCallback(callBackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    handleFacebookAccessToken(result.accessToken)
                }

                override fun onCancel() {
                    startActivity(
                        Intent(this@FacebookLoginActivity, LoginActivity::class.java)
                    )
                    finish()
                }

                override fun onError(exception: FacebookException) {
                    exception.printStackTrace()
                    startActivity(
                        Intent(this@FacebookLoginActivity, LoginActivity::class.java)
                    )
                    finish()
                }
            })

        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callBackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun handleFacebookAccessToken(token: AccessToken) {
        lateinit var userData : HashMap<String, String>

        val credential = FacebookAuthProvider.getCredential(token.token)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val s = "Facebook Sign In Sucessful"
                    displayToast(s);
                    startActivity(
                        Intent(this@FacebookLoginActivity, MainActivity::class.java)
                    )
                    finish()
                }
                else {
                    // If sign in fails, display a message to the user.
                    val s = task.exception?.message
                    displayToast(s)
                    startActivity(
                        Intent(this@FacebookLoginActivity, LoginActivity::class.java)
                    )
                    finish()
                }
            }
    }


    private fun displayToast(s: String?) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_SHORT).show()
    }
}