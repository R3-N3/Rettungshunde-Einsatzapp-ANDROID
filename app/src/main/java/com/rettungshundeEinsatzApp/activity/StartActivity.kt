package com.rettungshundeEinsatzApp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rettungshundeEinsatzApp.ui.screens.StartScreen

class StartActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        val sharedPreferences = getSharedPreferences("REAPrefs", MODE_PRIVATE) // for write and read preferences. To write: sharedPreferences.edit{putString("test", "abc") } to read: val token = sharedPreferences.getString("token", "")
        val token: String = sharedPreferences.getString("token", "").toString()
        val serverURL = sharedPreferences.getString("serverURL", "").toString()

        // If token and ServerURL is set in preferences, go to DashboardActivity and skip login. Userdata will be checked in DashboardActivity
        if(token != "" && serverURL != ""){
            Log.d("REA-StartActivity", "Token set in sharedPreferences, skipp login")
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
            finish()
        }
        else{
            Log.d("REA-StartActivity", "No Token set in sharedPreferences, login not skipped")
        }

        super.onCreate(savedInstanceState)

        setContent {
            StartScreen(
                onLoginClick = {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
            )
        }
    }
}


