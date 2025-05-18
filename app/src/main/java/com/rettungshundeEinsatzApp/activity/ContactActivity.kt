package com.rettungshundeEinsatzApp.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rettungshundeEinsatzApp.ui.screens.ContactScreen

class ContactActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ContactScreen()
        }
    }
    /*
    override fun onStart() {
        super.onStart()
        // for example registerLogging oder BroadcastReceiver
    }

    override fun onResume() {
        super.onResume()
        // UI-Refresh or ViewModel.triggerRefresh()
    }

    override fun onPause() {
        super.onPause()
        // for example save scroll state or UI
    }

    override fun onStop() {
        super.onStop()
        // for example stop network
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup of ViewModel, lists, etc.
    }
    */
}