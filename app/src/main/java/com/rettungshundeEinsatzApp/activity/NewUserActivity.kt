package com.rettungshundeEinsatzApp.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rettungshundeEinsatzApp.ui.screens.NewUserScreen

class NewUserActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewUserScreen(
                finishActivity = { finish() },
                getPrefs = { getSharedPreferences("REAPrefs", MODE_PRIVATE) },
                applicationContext = application
            )
        }
    }
}