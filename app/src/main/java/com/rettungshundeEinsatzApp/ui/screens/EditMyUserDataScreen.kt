package com.rettungshundeEinsatzApp.ui.screens


import android.app.Application
import android.util.Log
import android.util.Patterns
import androidx.activity.ComponentActivity.MODE_PRIVATE
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rettungshundeEinsatzApp.R
import com.rettungshundeEinsatzApp.ui.ReaAppTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Dialog
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataProvider
import com.rettungshundeEinsatzApp.functions.downloadAllUserData
import com.rettungshundeEinsatzApp.functions.editMyUserData
import kotlinx.coroutines.launch
import androidx.core.content.edit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMyUserDataScreen(
    applicationContext: Application,
    onCancel: () -> Unit
) {

    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("REAPrefs", MODE_PRIVATE)

    var username by rememberSaveable { mutableStateOf(sharedPreferences.getString("username", "").toString()) }
    var email by rememberSaveable { mutableStateOf(sharedPreferences.getString("email", "").toString()) }
    var phoneNumber by rememberSaveable { mutableStateOf(sharedPreferences.getString("phoneNumber", "").toString()) }
    var radioCallName by rememberSaveable { mutableStateOf(sharedPreferences.getString("radioCallName", "").toString()) }
    var securityLevel = when(sharedPreferences.getString("securityLevel", "").toString()){
        "1" -> stringResource(id = R.string.security_level_ek)
        "2" -> stringResource(id = R.string.security_level_zf)
        "3" -> stringResource(id = R.string.security_level_admin)
        else -> {
            stringResource(id = R.string.error)
        }
    }
    val myTrackColor = sharedPreferences.getString("myTrackColor", "#FF0000FF") ?: "#FF0000FF"
    Log.d("test",myTrackColor)
    val coroutineScope = rememberCoroutineScope()
    val confirmSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val resultSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var usernameError by rememberSaveable { mutableStateOf(false) }
    var emailError by rememberSaveable { mutableStateOf(false) }
    var phoneNumberError by rememberSaveable { mutableStateOf(false) }
    var radioCallNameError by rememberSaveable { mutableStateOf(false) }
    var securityLevelError by rememberSaveable { mutableStateOf(false) }
    var isSubmitting by rememberSaveable { mutableStateOf(false) }
    var resultMessage by rememberSaveable { mutableStateOf("") }
    var resultSuccess by rememberSaveable { mutableStateOf(false) }

    ReaAppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.edit_my_data_title)) },
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
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {

                Text(
                    text = stringResource(id = R.string.change_my_user_data_info_text) ,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                    },
                    label = { Text(stringResource(id = R.string.username)) },
                    isError = usernameError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    readOnly = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    )
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                    },
                    label = { Text(stringResource(id = R.string.email)) },
                    isError = emailError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    readOnly = false,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    )
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = it
                    },
                    label = { Text(stringResource(id = R.string.phonenumber)) },
                    isError = phoneNumberError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    readOnly = false,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    )
                )

                OutlinedTextField(
                    value = radioCallName,
                    onValueChange = {
                        radioCallName = it
                    },
                    label = { Text(stringResource(id = R.string.radio_call_name)) },
                    isError = radioCallNameError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    readOnly = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )

                OutlinedTextField(
                    value = securityLevel,
                    onValueChange = {
                        securityLevel = it
                    },
                    label = { Text(stringResource(id = R.string.security_level)) },
                    isError = securityLevelError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    readOnly = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onCancel()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(id = R.string.cancel))
                    }
                    Button(
                        onClick = {

                            val allValid = username.isNotBlank() &&
                                    isValidEmail(email) &&
                                    phoneNumber.isNotBlank() &&
                                    radioCallName.isNotBlank() &&
                                    securityLevel.isNotBlank()

                            usernameError = username.isBlank()
                            emailError = !isValidEmail(email)
                            phoneNumberError = phoneNumber.isBlank()
                            radioCallNameError = radioCallName.isBlank()
                            securityLevelError = securityLevel.isBlank()

                            if (!allValid) {
                                return@Button
                            }

                            coroutineScope.launch {
                                confirmSheetState.show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        //enabled = !showConfirmDialog
                    ) {
                        Text(stringResource(id = R.string.save))
                    }
                }


                if (confirmSheetState.isVisible) {
                    ModalBottomSheet(
                        onDismissRequest = { /* ... */ },
                        sheetState = confirmSheetState
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(stringResource(id = R.string.dialog_edit_my_data_confirm_title), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(id = R.string.dialog_edit_my_data_confirm_text), modifier = Modifier.padding(top = 8.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                TextButton(
                                    onClick = {
                                        coroutineScope.launch { confirmSheetState.hide() }
                                    }
                                ) { Text(stringResource(id = R.string.cancel)) }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = {
                                        coroutineScope.launch { confirmSheetState.hide() }
                                        isSubmitting = true
                                        val token: String = sharedPreferences.getString("token", "").toString()
                                        val serverApiURL = sharedPreferences.getString("serverApiURL", "").toString()
                                        Log.d("EditMyUserDataScreen", "$username  $email   $phoneNumber" )
                                        editMyUserData(
                                            serverApiURL, token, email, phoneNumber
                                        ) { success, message ->
                                            isSubmitting = false
                                            resultMessage = message
                                            resultSuccess = success

                                            if(resultSuccess) {

                                                sharedPreferences.edit {
                                                    putString("email", email)
                                                    putString("phoneNumber", phoneNumber)
                                                }

                                                val db = AllUserDataProvider.getDatabase(applicationContext)

                                                downloadAllUserData(
                                                    token,
                                                    serverApiURL,
                                                    db.allUserDataDao()
                                                ) { message2 ->
                                                    val responseMessageArray2 = message2.split(",")
                                                        .toTypedArray() // [status, message,]
                                                    Log.d(
                                                        "REA-DashboardActivity",
                                                        "GetAllUserData: status: ${responseMessageArray2[0]}   message: ${responseMessageArray2[1]}"
                                                    )
                                                }
                                            }



                                            coroutineScope.launch {
                                                resultSheetState.show()
                                            }
                                        }
                                    }
                                ) { Text(stringResource(id = R.string.save)) }
                            }
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
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(stringResource(id = R.string.saving), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }


                if (resultSheetState.isVisible) {
                    ModalBottomSheet(
                        onDismissRequest = { /* hide result sheet */ },
                        sheetState = resultSheetState
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if(resultSuccess){
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green, modifier = Modifier.size(48.dp))
                                Text(stringResource(id = R.string.saved), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp))
                                Text(text = resultMessage)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { onCancel() }) { Text("OK") }
                            }
                            else{
                                Icon(Icons.Default.Cancel, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                                Text(stringResource(id = R.string.result_error), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp))
                                Text(text = resultMessage)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { coroutineScope.launch {
                                    resultSheetState.hide()
                                } }) { Text(stringResource(id = R.string.ok)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
