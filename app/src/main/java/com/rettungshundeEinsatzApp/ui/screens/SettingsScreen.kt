package com.rettungshundeEinsatzApp.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rettungshundeEinsatzApp.R
import com.rettungshundeEinsatzApp.activity.StartActivity
import com.rettungshundeEinsatzApp.functions.resetUserData
import com.rettungshundeEinsatzApp.service.myLocation.MyLocationService
import com.rettungshundeEinsatzApp.ui.ReaAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val toastNewLogin = stringResource(id = R.string.dashboard_activity_toast_need_new_login)

    ReaAppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.settings)) },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = stringResource(id = R.string.dashboard_bottom_menu_group)
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(start = 16.dp, top = 10.dp, bottom = 0.dp, end = 16.dp)
            ) {

/*
                // Dark Mode Toggle
                var isDarkModeEnabled by remember { mutableStateOf(false) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Dark Mode", modifier = Modifier.weight(1f))
                    Switch(
                        checked = isDarkModeEnabled,
                        onCheckedChange = { isDarkModeEnabled = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notifications Toggle
                var isNotificationsEnabled by remember { mutableStateOf(true) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(id = R.string.notification), modifier = Modifier.weight(1f))
                    Switch(
                        checked = isNotificationsEnabled,
                        onCheckedChange = { isNotificationsEnabled = it }
                    )
                }
 */

                // Logout Button
                OutlinedButton(
                    onClick = {
                        resetUserData(context)
                        Toast.makeText(context, toastNewLogin, Toast.LENGTH_SHORT).show()
                        val stopServiceIntent = Intent(context, MyLocationService::class.java)
                        context.stopService(stopServiceIntent)
                        val intent = Intent(context, StartActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text(stringResource(id = R.string.logout),
                        fontSize = 20.sp)
                }


            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun SettingsScreenPreviewLight() {
    ReaAppTheme {
        SettingsScreen()
    }
}

@Preview(name = "Dark Mode", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenPreviewDark() {
    ReaAppTheme {
        SettingsScreen()
    }
}

@Preview(name = "Large Font", fontScale = 1.5f, showBackground = true)
@Composable
fun SettingsScreenPreviewLargeFont() {
    ReaAppTheme {
        SettingsScreen()
    }
}