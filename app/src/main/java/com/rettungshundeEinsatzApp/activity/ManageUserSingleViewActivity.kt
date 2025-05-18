package com.rettungshundeEinsatzApp.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rettungshundeEinsatzApp.ui.screens.ManageUserSingleViewScreen

class ManageUserSingleViewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            ManageUserSingleViewScreen(
                context = this,
                application = application,
                usernameInitial = intent.getStringExtra("username") ?: "",
                emailInitial = intent.getStringExtra("email") ?: "",
                phoneNumberInitial = intent.getStringExtra("phoneNumber") ?: "",
                callSignInitial = intent.getStringExtra("radioCallName") ?: "",
                securityLevelInitial = intent.getStringExtra("securityLevel") ?: "",
                trackColorInitial = intent.getStringExtra("trackColor") ?: "#FF0000",
                userIDInitial = intent.getStringExtra("id") ?: "",
                onCancel = { finish() }
            )
        }
    }
}
