package com.rettungshundeEinsatzApp.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.rettungshundeEinsatzApp.R
import com.rettungshundeEinsatzApp.activity.EditMyUserDataActivity
import com.rettungshundeEinsatzApp.activity.StartActivity
import com.rettungshundeEinsatzApp.functions.resetUserData
import com.rettungshundeEinsatzApp.service.myLocation.MyLocationService
import com.rettungshundeEinsatzApp.ui.ReaAppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val toastNewLogin = stringResource(id = R.string.dashboard_activity_toast_need_new_login)
    val sharedPreferences = context.getSharedPreferences("REAPrefs", Context.MODE_PRIVATE)
    val coroutineScope = rememberCoroutineScope()
    val colorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colorPickerController = rememberColorPickerController()
    val myTrackColor = sharedPreferences.getString("myTrackColor", "#FF0000FF") ?: "#FF0000FF"
    val colorSaver = Saver<Color, Int>(
        save = { it.toArgb() },
        restore = { Color(it) }
    )
    var selectedColor by rememberSaveable(stateSaver = colorSaver) {
        mutableStateOf(Color(myTrackColor.toColorInt()))
    }


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

                val email = sharedPreferences.getString("email", "").toString()
                val phoneNumber = sharedPreferences.getString("phoneNumber", "").toString()
                val username = sharedPreferences.getString("username", "").toString()
                val securityLevel = when(sharedPreferences.getString("securityLevel", "").toString()){
                    "1" -> stringResource(id = R.string.security_level_ek)
                    "2" -> stringResource(id = R.string.security_level_zf)
                    "3" -> stringResource(id = R.string.security_level_admin)
                    else -> {"Fehler"}
                }
                val radioCallName = sharedPreferences.getString("radioCallName", "").toString()


                Text(
                    text = "Meine Daten",
                    fontSize = 22.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                Text(
                    text = "Telefonnummer: $phoneNumber \n E-Mail-Adresse: $email \n Benutzername: $username \n Funkrufname: $radioCallName \n Sicherheitslevel: $securityLevel \n Meine Track-Farbe: " ,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .background(selectedColor)
                        .border(1.dp, Color.Gray) // optional
                ){
                    Text(
                        text = myTrackColor,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth().padding(10.dp). align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))


                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            colorSheetState.show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Meine Track Farbe ändern",
                        fontSize = 20.sp)
                }

                OutlinedButton(
                    onClick = {
                        val intent = Intent(context, EditMyUserDataActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Meine Daten ändern",
                        fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.height(64.dp))

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









                if (colorSheetState.isVisible) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            coroutineScope.launch { colorSheetState.hide() }
                        },
                        sheetState = colorSheetState
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.color_select),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            HsvColorPicker(
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(200.dp)
                                    .align(Alignment.CenterHorizontally),
                                controller = colorPickerController,
                                onColorChanged = { colorEnvelope ->
                                    selectedColor = colorEnvelope.color
                                }
                            )

                            BrightnessSlider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(30.dp)
                                    .padding(vertical = 8.dp),
                                controller = colorPickerController
                            )

                            AlphaSlider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(30.dp),
                                controller = colorPickerController
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    sharedPreferences.edit {
                                        putString("myTrackColor", "#%08X".format(selectedColor.toArgb()))
                                    }
                                    coroutineScope.launch { colorSheetState.hide() }
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(stringResource(id = R.string.finish))
                            }
                        }
                    }
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