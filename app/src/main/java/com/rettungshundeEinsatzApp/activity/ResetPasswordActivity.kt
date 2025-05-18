package com.rettungshundeEinsatzApp.activity


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rettungshundeEinsatzApp.ui.screens.ResetPasswordScreen

class ResetPasswordActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ResetPasswordScreen(
                finishActivity = { finish() }
            )
        }
    }
}