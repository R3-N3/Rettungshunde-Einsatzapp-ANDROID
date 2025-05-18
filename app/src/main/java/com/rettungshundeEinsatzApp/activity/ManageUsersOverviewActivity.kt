package com.rettungshundeEinsatzApp.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rettungshundeEinsatzApp.ui.screens.ManageUsersOverviewScreen


class ManageUsersOverviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            ManageUsersOverviewScreen(
                context = this,
                application = application,
                finishActivity = { finish() }
            )
        }
    }
}
