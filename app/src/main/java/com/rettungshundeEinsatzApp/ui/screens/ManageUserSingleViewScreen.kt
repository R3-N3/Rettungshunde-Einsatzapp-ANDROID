package com.rettungshundeEinsatzApp.ui.screens

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity.MODE_PRIVATE
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.rettungshundeEinsatzApp.R
import com.rettungshundeEinsatzApp.activity.ManageUsersOverviewActivity
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataProvider
import com.rettungshundeEinsatzApp.functions.editUser
import com.rettungshundeEinsatzApp.functions.downloadAllUserData
import com.rettungshundeEinsatzApp.ui.ReaAppTheme
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUserSingleViewScreen(
    context: Context,
    application: Application,
    usernameInitial: String,
    emailInitial: String,
    phoneNumberInitial: String,
    callSignInitial: String,
    securityLevelInitial: String,
    trackColorInitial: String,
    userIDInitial: String,
    onCancel: () -> Unit
) {

    ReaAppTheme {
        val scrollState = rememberScrollState()

        val getSecurityLevel = when(securityLevelInitial){
            "1" -> stringResource(id = R.string.security_level_ek)
            "2" -> stringResource(id = R.string.security_level_zf)
            "3" -> stringResource(id = R.string.security_level_admin)
            else -> {stringResource(id = R.string.login_organisation_choice)}
        }
        val colorSaver = Saver<Color, Int>(
            save = { it.toArgb() },
            restore = { Color(it) }
        )
        var username by remember { mutableStateOf(usernameInitial) }
        var email by remember { mutableStateOf(emailInitial) }
        var phoneNumber by remember { mutableStateOf(phoneNumberInitial) }
        var callSign by remember { mutableStateOf(callSignInitial) }
        var showColorPicker by remember { mutableStateOf(false) }
        var selectedColor by rememberSaveable(stateSaver = colorSaver) {
            mutableStateOf(Color(trackColorInitial.toColorInt()))
        }
        var usernameError by remember { mutableStateOf(false) }
        var emailError by remember { mutableStateOf(false) }
        var phoneNumberError by remember { mutableStateOf(false) }
        var callSignError by remember { mutableStateOf(false) }
        val selectedHex = "#%06X".format(0xFFFFFF and selectedColor.toArgb())
        val securityLevels = listOf(stringResource(id = R.string.security_level_ek), stringResource(id = R.string.security_level_zf), stringResource(id = R.string.security_level_admin))
        var expanded by remember { mutableStateOf(false) }
        var selectedSecurityLevel by rememberSaveable { mutableStateOf(getSecurityLevel) }
        var securityLevelError by remember { mutableStateOf(false) }
        val ek = stringResource(R.string.security_level_ek)
        val zf = stringResource(R.string.security_level_zf)
        val admin = stringResource(R.string.security_level_admin)
        var showConfirmDialog by remember { mutableStateOf(false) }
        var isSubmitting by remember { mutableStateOf(false) }
        var showResultDialog by remember { mutableStateOf(false) }
        var resultMessage by remember { mutableStateOf("") }
        var resultSuccess by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.edit_user)) },
                    navigationIcon = {
                        IconButton(onClick = {  }) {
                            Icon(Icons.Default.EditNote, contentDescription = stringResource(id = R.string.dashboard_bottom_menu_group))
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {


                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            if (usernameError) usernameError = false
                        },
                        label = { Text(stringResource(id = R.string.username)) },
                        isError = usernameError,
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp)
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

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            if (phoneNumberError) phoneNumberError = false
                        },
                        label = { Text(stringResource(id = R.string.phonenumber)) },
                        isError = phoneNumberError,
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
                    )

                    OutlinedTextField(
                        value = callSign,
                        onValueChange = {
                            callSign = it
                            if (callSignError) callSignError = false
                        },
                        label = { Text(stringResource(id = R.string.radio_call_name)) },
                        isError = callSignError,
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedSecurityLevel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(id = R.string.security_level)) },
                            isError = securityLevelError,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            securityLevels.forEach { level ->
                                DropdownMenuItem(
                                    text = { Text(level) },
                                    onClick = {
                                        selectedSecurityLevel = level
                                        expanded = false
                                        securityLevelError = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ElevatedButton(
                            onClick = { showColorPicker = true },
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            Text(stringResource(id = R.string.color))
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .background(selectedColor)
                                .border(1.dp, Color.Gray) // optional
                        ){
                            Text(
                                text = selectedHex,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth().padding(10.dp). align(Alignment.Center)
                            )
                        }
                    }

                    if (showColorPicker) {
                        AlertDialog(
                            onDismissRequest = { showColorPicker = false },
                            title = { Text(stringResource(id = R.string.color_select)) },
                            text = {
                                val controller = rememberColorPickerController()
                                Column {
                                    HsvColorPicker(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(450.dp)
                                            .padding(10.dp),
                                        controller = controller,
                                        onColorChanged = { colorEnvelope: ColorEnvelope ->
                                            selectedColor = colorEnvelope.color
                                        }
                                    )
                                    AlphaSlider(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp)
                                            .height(35.dp),
                                        controller = controller,
                                    )
                                    BrightnessSlider(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp)
                                            .height(35.dp),
                                        controller = controller,
                                    )
                                }
                            },
                            confirmButton = {
                                Button(onClick = { showColorPicker = false }) {
                                    Text("OK")
                                }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

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
                            if (!showConfirmDialog) {
                                val allValid = username.isNotBlank() &&
                                        isValidEmail(email) &&
                                        phoneNumber.isNotBlank() &&
                                        callSign.isNotBlank() &&
                                        selectedSecurityLevel.isNotBlank()

                                usernameError = username.isBlank()
                                emailError = !isValidEmail(email)
                                phoneNumberError = phoneNumber.isBlank()
                                callSignError = callSign.isBlank()
                                securityLevelError = selectedSecurityLevel.isBlank()

                                if (!allValid) {
                                    return@Button
                                }
                                showConfirmDialog = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !showConfirmDialog
                    ) {
                        Text(stringResource(id = R.string.change))
                    }
                }

            }

            if (showConfirmDialog) {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("${stringResource(id = R.string.edit_user)}?") },
                    text = { Text(stringResource(id = R.string.change_user_data_text)) },
                    confirmButton = {
                        Button(onClick = {
                            showConfirmDialog = false
                            isSubmitting = true

                            val sharedPreferences = context.getSharedPreferences("REAPrefs", MODE_PRIVATE)
                            val token: String = sharedPreferences.getString("token", "").toString()
                            val serverApiURL = sharedPreferences.getString("serverApiURL", "").toString()
                            val selectedSecurityLevelSend = when (selectedSecurityLevel) {
                                ek -> "1"
                                zf -> "2"
                                admin -> "3"
                                else -> "0"
                            }
                            editUser(
                                serverApiURL, token, username, email, phoneNumber, callSign,
                                selectedSecurityLevelSend, selectedHex, userIDInitial
                            ) { success, message ->
                                isSubmitting = false
                                resultMessage = message
                                resultSuccess = success
                                showResultDialog = true

                                if(resultSuccess) {
                                    val db = AllUserDataProvider.getDatabase(application)

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
                            }

                        }) {
                            Text(stringResource(id = R.string.confirm))
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = {
                            showConfirmDialog = false
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
                            if (resultSuccess){
                                val intentManageUsers = Intent(context, ManageUsersOverviewActivity::class.java)
                                context.startActivity(intentManageUsers)
                                onCancel()
                            }
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
                                text = if (resultSuccess) stringResource(id = R.string.success) else stringResource(id = R.string.failed)
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

