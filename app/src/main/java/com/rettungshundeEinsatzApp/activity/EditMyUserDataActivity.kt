package com.rettungshundeEinsatzApp.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rettungshundeEinsatzApp.ui.screens.EditMyUserDataScreen

class EditMyUserDataActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            EditMyUserDataScreen(
                applicationContext = application,
                onCancel = { finish() }
            )
        }
    }
}
