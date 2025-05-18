package com.rettungshundeEinsatzApp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import com.rettungshundeEinsatzApp.R
import com.rettungshundeEinsatzApp.functions.resetPassword
import com.rettungshundeEinsatzApp.ui.ReaAppTheme
import com.rettungshundeEinsatzApp.ui.components.OrganisationDropdown
import com.rettungshundeEinsatzApp.ui.components.getServerApiUrlForOrganisation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    finishActivity: () -> Unit
) {

    val scrollState = rememberScrollState()
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var showValidDialog by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }
    var resultSuccess by remember { mutableStateOf(false) }
    var isEnabled by remember { mutableStateOf(true) }
    var orgError by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var serverApiURL by remember { mutableStateOf("") }

    val selectedOrgOptionDefault = stringResource(id = R.string.login_organisation_choice)
    var selectedOrgOption by remember { mutableStateOf(selectedOrgOptionDefault) }


    ReaAppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Passwort Reset") },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.LockReset, contentDescription = "Bild")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    text = "Zum zurücksetzen ihrer E-Mail Adresse wählen Sie zunächst bitte ihre Organisation aus. Im Anschluss geben Sie die E-Mail Adresse mit, mit welcher Sie von ihrer Organisation für diese App angemeldet wurden. Im Anschluss bestätigen Sie ihre Eingaben. Wenn diese korrekt sind, erhalten Sie eine E-Mail mit weiteren Anweisungen."
                )

                Spacer(modifier = Modifier.height(16.dp))

                OrganisationDropdown(
                    selectedOrgOption = selectedOrgOption,
                    onOptionSelected = { selectedOrgOption = it },
                    isEnabled = isEnabled,
                    isError = orgError
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (emailError) emailError = false
                    },
                    label = { Text(stringResource(id = R.string.email)) },
                    isError = emailError,
                    singleLine = true,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
                )

                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            finishActivity()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !showValidDialog
                    ) {
                        Text(stringResource(id = R.string.cancel))
                    }

                    OutlinedButton(
                        onClick = {
                            orgError = false
                            emailError = false
                            var allValid = true
                            val url = getServerApiUrlForOrganisation(context, selectedOrgOption)
                            serverApiURL = url

                            if (!showValidDialog) {
                                if(url.isBlank()){
                                    allValid = false
                                    orgError = true
                                }
                                if(!isValidEmail(email)){
                                    allValid = false
                                    emailError = true
                                }
                                if (!allValid) {
                                    return@OutlinedButton
                                }
                                showValidDialog = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !showValidDialog
                    ) {
                        Text("Zurücksetzen")
                    }
                }
            }

            if (showValidDialog) {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("Passwort zurücksetzen") },
                    text = { Text("Wollen Sie ihr Passwort wirklic zurücksetzen? Dannach wird Ihr Account von allen Geräten agmeledet und Sie müssen sich in jedem Gerät erneut anmelden. ") },
                    confirmButton = {
                        Button(onClick = {
                            showValidDialog = false
                            isSubmitting = true
                            isEnabled = false

                            resetPassword(serverApiURL, email,
                            ) { success, message ->
                                isSubmitting = false
                                resultMessage = message
                                resultSuccess = success
                                showResultDialog = true
                            }

                      }) {
                          Text(stringResource(id = R.string.confirm))
                      }
                  },
                  dismissButton = {
                      OutlinedButton(onClick = {
                          showValidDialog = false
                      }) {
                          Text(stringResource(id = R.string.cancel))
                      }
                  }
                )
            }

          if (isSubmitting) {
              AlertDialog(
                  onDismissRequest = {},
                  confirmButton = {},
                  title = { Text(stringResource(id = R.string.please_wait)) },
                  text = {
                      Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(16.dp),
                          modifier = Modifier.fillMaxWidth()
                      ) {
                          CircularProgressIndicator()
                          Text(stringResource(id = R.string.submit_data))
                      }
                  }
              )
          }

            if (showResultDialog) {
              AlertDialog(
                  onDismissRequest = { showResultDialog = false },
                  confirmButton = {
                      Button(onClick = {
                          showResultDialog = false
                          if (resultSuccess) finishActivity()
                      }) {
                          Text("OK")
                      }
                  },
                  title = {
                      Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(12.dp)
                      ) {
                          Icon(
                              imageVector = if (resultSuccess) Icons.Filled.CheckCircle else Icons.Filled.Error,
                              contentDescription = null,
                              tint = if (resultSuccess) Color(0xFF4CAF50) else Color.Red
                          )

                          Text(
                              text = if (resultSuccess) stringResource(id = R.string.success) else stringResource(
                                  id = R.string.failed
                              )
                          )
                      }
                  },
                  text = {
                      Text(
                          text = resultMessage
                      )
                  }
              )
          }

        }
    }
}

private fun isValidEmail(email: String): Boolean {
  return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}


