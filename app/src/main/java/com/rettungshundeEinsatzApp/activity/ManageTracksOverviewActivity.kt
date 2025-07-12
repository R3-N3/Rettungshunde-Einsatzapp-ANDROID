package com.rettungshundeEinsatzApp.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataAndLocationsDatabase
import com.rettungshundeEinsatzApp.ui.screens.ManageTracksOverviewScreen


class ManageTracksOverviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContent {
            ManageTracksOverviewScreen()
        }
    }
}
