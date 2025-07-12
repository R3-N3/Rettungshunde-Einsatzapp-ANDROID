package com.rettungshundeEinsatzApp.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataAndLocationsDatabase
import com.rettungshundeEinsatzApp.ui.screens.ManageAreaOverviewScreen
import com.rettungshundeEinsatzApp.ui.screens.ManageTracksOverviewScreen


class ManageAreaOverviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContent {
            ManageAreaOverviewScreen()
        }
    }
}
