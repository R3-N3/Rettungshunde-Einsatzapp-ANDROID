package com.rettungshundeEinsatzApp.ui.screens

import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Square
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rettungshundeEinsatzApp.R
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataAndLocationsDatabase
import com.rettungshundeEinsatzApp.database.mylocallocation.MyLocationDatabase
import com.rettungshundeEinsatzApp.functions.deleteAllGPSData
import com.rettungshundeEinsatzApp.functions.deleteMyGPSData
import com.rettungshundeEinsatzApp.ui.ReaAppTheme
import com.rettungshundeEinsatzApp.viewmodel.MyTrackViewModel
import com.rettungshundeEinsatzApp.viewmodel.UsersWithTracksViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTracksOverviewScreen() {

    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("REAPrefs", MODE_PRIVATE)
    val myUserName = sharedPreferences.getString("username", "").toString()
    val db = AllUserDataAndLocationsDatabase.getInstance(context)
    val userDao = db.allUserDataDao()
    val locationDao = db.allUsersLocationsDao()
    val viewModel: UsersWithTracksViewModel = viewModel(factory = UsersWithTracksViewModel.Factory(userDao, locationDao))
    val myLocationDao = MyLocationDatabase.getDatabase(context).locationDao()
    val myTrackViewModel: MyTrackViewModel = viewModel(factory = MyTrackViewModel.Factory(myLocationDao))

    val securityLevel = sharedPreferences.getString("securityLevel", "1")?.toIntOrNull() ?: 1
    val token: String = sharedPreferences.getString("token", "").toString()
    val serverApiURL = sharedPreferences.getString("serverApiURL", "").toString()

    val myTrackStats by myTrackViewModel.trackStats.collectAsState()
    val usersWithTrackStats by viewModel.usersWithTrackStats.collectAsState(initial = emptyList())

    val coroutineScope = rememberCoroutineScope()
    val confirmDeleteMyGPSDataSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val confirmDeleteAllGPSDataSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val resultSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isSubmitting = false
    var resultMessage by rememberSaveable { mutableStateOf("") }
    var resultSuccess by rememberSaveable { mutableStateOf(false) }

    ReaAppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Route,
                                contentDescription = stringResource(id = R.string.contacts)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Trackverwaltung")
                        }
                    },
                )
                 }
        ) { paddingValues ->

            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                item {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                confirmDeleteMyGPSDataSheetState.show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Lösche Meine GPS Daten", fontSize = 18.sp)
                    }

                    if (securityLevel > 1) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    confirmDeleteAllGPSDataSheetState.show()
                                }
                            },
                            enabled = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Alle GPS Daten löschen", fontSize = 18.sp)
                        }

                        Button(
                            onClick = { },
                            enabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Alle Tracks als .gpx Datei exportieren", fontSize = 18.sp)
                        }
                    }

                    Text(
                        text = "Mein Track",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 32.dp, start = 16.dp, bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            if (myTrackStats.pointCount == 0) {
                                Text("Kein eigener Track vorhanden", style = MaterialTheme.typography.bodyMedium)
                            } else {
                                Text(myUserName, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 4.dp))
                                Text("Anzahl Wegpunkte: ${myTrackStats.pointCount}", style = MaterialTheme.typography.bodyMedium)
                                Text("Gesamtlänge: ${"%.1f".format(myTrackStats.totalDistanceMeters)} m", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    Text(
                        text = "Alle Benutzer",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 32.dp, start = 16.dp, bottom = 8.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        if (usersWithTrackStats.isEmpty()) {
                            Text(
                                "Keine Tracks vorhanden",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        } else {
                            Column(modifier = Modifier.padding(8.dp)) {
                                usersWithTrackStats.forEachIndexed { index, userStats ->
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = userStats.user.username,
                                            style = MaterialTheme.typography.titleLarge,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                        Text(
                                            text = "Anzahl Wegpunkte: ${userStats.trackCount}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Gesamtlänge: ${"%.1f".format(userStats.totalDistanceMeters)} m",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (index < usersWithTrackStats.lastIndex) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(top = 8.dp),
                                                thickness = 1.dp,
                                                color = DividerDefaults.color
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (confirmDeleteMyGPSDataSheetState.isVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        coroutineScope.launch { confirmDeleteMyGPSDataSheetState.hide() }
                    },
                    sheetState = confirmDeleteMyGPSDataSheetState
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text("⚠️ Meine GPS Daten Löschen", style = MaterialTheme.typography.titleMedium)
                        Text("Möchtest du deine GPS-Daten löschen? Diese werden danach nicht mehr auf der Karte angezeigt!", modifier = Modifier.padding(top = 8.dp))
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    coroutineScope.launch { confirmDeleteMyGPSDataSheetState.hide() }
                                }
                            ) { Text(stringResource(id = R.string.cancel)) }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    isSubmitting = true
                                    deleteMyGPSData(context) { success, message ->
                                        Log.d("ManageTracksOverviewScreen", "Lösche meine Daten: $success – $message")
                                        resultMessage = message
                                        resultSuccess = success
                                        coroutineScope.launch {
                                            confirmDeleteMyGPSDataSheetState.hide()
                                            resultSheetState.show()
                                        }
                                        isSubmitting = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) { Text("Meine GPS-Daten Löschen") }
                        }
                    }
                }
            }

            if (confirmDeleteAllGPSDataSheetState.isVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        coroutineScope.launch { confirmDeleteAllGPSDataSheetState.hide() }
                    },
                    sheetState = confirmDeleteAllGPSDataSheetState
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text("⚠️ Alle GPS Daten Löschen", style = MaterialTheme.typography.titleMedium)
                        Text("Möchtest du ALLE GPS Daten löschen? Damit sind werden alle GPS Daten auf dem Server gelöscht und steht unwiederuflich nicht mehr zur verfügung!", modifier = Modifier.padding(top = 8.dp))
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    coroutineScope.launch { confirmDeleteAllGPSDataSheetState.hide() }
                                }
                            ) { Text(stringResource(id = R.string.cancel)) }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    isSubmitting = true
                                    deleteAllGPSData(serverApiURL, token, locationDao) { success, message ->
                                        Log.d("ManageTracksOverviewScreen", "Lösche Alle Daten: $success – $message")
                                        resultMessage = message
                                        resultSuccess = success
                                        coroutineScope.launch {
                                            confirmDeleteAllGPSDataSheetState.hide()
                                            resultSheetState.show()
                                        }
                                        isSubmitting = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) { Text("Alle GPS-Daten Löschen") }
                        }
                    }
                }
            }

            if (resultSheetState.isVisible) {
                ModalBottomSheet(
                    onDismissRequest = { coroutineScope.launch { resultSheetState.hide() } },
                    sheetState = resultSheetState
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (resultSuccess) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green, modifier = Modifier.size(48.dp))
                            Text("Erfolg", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp))
                        } else {
                            Icon(Icons.Default.Cancel, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                            Text("Fehler!", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp))
                        }
                        Text(text = resultMessage)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { coroutineScope.launch { resultSheetState.hide() } }) { Text("OK") }
                    }
                }
            }

            if (isSubmitting) {
                Dialog(onDismissRequest = { }) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(12.dp))
                            Text("Speichern...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}