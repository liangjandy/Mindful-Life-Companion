package com.example.MindfulLifeCompanion.Activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.example.MindfulLifeCompanion.R
import com.google.firebase.auth.FirebaseAuth

class SettingActivity: AppCompatActivity() {

/*    private lateinit var backToDashboard : Button*/
    private lateinit var whiteButton: ImageView
/*    private lateinit var imageButton: ImageView*/
    private lateinit var changePassword : ImageView
    lateinit var categoryList: Spinner
    private lateinit var userPreferences: SharedPreferences
    private lateinit var savedPreferencesBtn : Button
    var auth = FirebaseAuth.getInstance()
    lateinit var defaultQuoteCategory : String

    private lateinit var darkMode : SwitchCompat



    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_layout)

/*        backToDashboard = findViewById(R.id.back)*/
        changePassword = findViewById(R.id.change_pass_btn)
        whiteButton = findViewById(R.id.white_btn)
/*        imageButton = findViewById(R.id.quote_btn)*/
        categoryList = findViewById(R.id.selectCategories)
        savedPreferencesBtn = findViewById(R.id.save_preferences_btn)
        userPreferences = getSharedPreferences("User_" + auth.currentUser?.uid, MODE_PRIVATE)
        darkMode = findViewById<SwitchCompat>(R.id.dark_mode_switch)

        //BUTTON
/*        backToDashboard.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish();
        }*/

        whiteButton.setOnClickListener {
            finish()
        }

/*        imageButton.setOnClickListener {
            finish()
        }*/

        changePassword.setOnClickListener {
            val intent = Intent(applicationContext, ChangePasswordActivity::class.java)
            startActivity(intent)
            finish();

        }

        val sharedPreferences = getSharedPreferences("Mode", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val nightMode = sharedPreferences.getBoolean("night",false)


        if (nightMode){
            darkMode.isChecked = true
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }


        darkMode.setOnCheckedChangeListener { darkMode, isChecked ->


            if (!isChecked){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                editor.putBoolean("night", false)
                editor.apply()

            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                editor.putBoolean("night",true)
                editor.apply()
            }
        }

        savedPreferencesBtn.setOnClickListener {
            userPreferences.edit().putString("quote_category_preference", defaultQuoteCategory).apply()
            finish()
        }

        //Default Quote Category
        defaultQuoteCategory = userPreferences.getString("quote_category_preference", "").toString()
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.categories,
            R.layout.quote_category_spinner_theme
        )
        fillCategoryList(adapter)

    }

    //function to fill quote category spinner
    private fun fillCategoryList(adapter: ArrayAdapter<CharSequence>) {

        categoryList.adapter = adapter

        categoryList.setSelection(adapter.getPosition(userPreferences.getString("quote_category_preference", "Happiness")))

        categoryList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                defaultQuoteCategory = ""
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 == 0) {
                    defaultQuoteCategory = ""
                } else {
                    if (p0 != null) {
                        defaultQuoteCategory = p0.getItemAtPosition(p2).toString()
                    }
                }
            }
        }
    }
}
