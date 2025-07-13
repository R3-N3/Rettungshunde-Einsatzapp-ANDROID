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
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DeleteForever
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
import com.rettungshundeEinsatzApp.database.area.AreaDatabase
import com.rettungshundeEinsatzApp.functions.deleteAllAreas
import com.rettungshundeEinsatzApp.ui.ReaAppTheme
import com.rettungshundeEinsatzApp.viewmodel.AreaViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAreaOverviewScreen() {

    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("REAPrefs", MODE_PRIVATE)

    val areaDao = AreaDatabase.getDatabase(context).areaDao()
    val areaViewModel: AreaViewModel = viewModel(factory = AreaViewModel.Factory(areaDao))
    val areas by areaViewModel.areas.collectAsState()

    val securityLevel = sharedPreferences.getString("securityLevel", "1")?.toIntOrNull() ?: 1
    val token: String = sharedPreferences.getString("token", "").toString()
    val serverApiURL = sharedPreferences.getString("serverApiURL", "").toString()

    val coroutineScope = rememberCoroutineScope()
    val confirmDeleteAllAreasSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
                                Icons.Default.Square,
                                contentDescription = stringResource(id = R.string.contacts)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Flächenverwaltung")
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

                    if (securityLevel > 1) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    confirmDeleteAllAreasSheetState.show()
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
                            Text("Alle Flächen löschen", fontSize = 18.sp)
                        }
                    }

                    Text(
                        text = "Alle Flächen",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 32.dp, start = 16.dp, bottom = 8.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        if (areas.isEmpty()) {
                            Text(
                                "Keine Flächen vorhanden",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        } else {
                            Column(modifier = Modifier.padding(8.dp)) {
                                areas.forEachIndexed { index, areaWithCoords ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = areaWithCoords.area.title,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = areaWithCoords.area.desc,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Koordinatenpunkte: ${areaWithCoords.coordinates.size}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        // Divider nur anzeigen, wenn es nicht der letzte Eintrag ist
                                        if (index < areas.lastIndex) {
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

            if (confirmDeleteAllAreasSheetState.isVisible) {
                ModalBottomSheet(
                    onDismissRequest = {
                        coroutineScope.launch { confirmDeleteAllAreasSheetState.hide() }
                    },
                    sheetState = confirmDeleteAllAreasSheetState
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text("⚠️ Alle Flächen Löschen", style = MaterialTheme.typography.titleMedium)
                        Text("Möchtest du alle Flächen löschen? Diese werden dann nicht mehr für dich und andere auf der Karte angezeigt", modifier = Modifier.padding(top = 8.dp))
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    coroutineScope.launch { confirmDeleteAllAreasSheetState.hide() }
                                }
                            ) { Text(stringResource(id = R.string.cancel)) }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    isSubmitting = true
                                    deleteAllAreas(serverApiURL, token, areaDao) { success, message ->
                                        Log.d("ManageTracksOverviewScreen", "Lösche Alle Daten: $success – $message")
                                        resultMessage = message
                                        resultSuccess = success
                                        coroutineScope.launch {
                                            confirmDeleteAllAreasSheetState.hide()
                                            resultSheetState.show()
                                        }
                                        isSubmitting = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) { Text("Alle Flächen Löschen") }
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