package com.rettungshundeEinsatzApp.ui.screens

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
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
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rettungshundeEinsatzApp.R
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataProvider
import com.rettungshundeEinsatzApp.functions.uploadNewUser
import com.rettungshundeEinsatzApp.functions.downloadAllUserData
import com.rettungshundeEinsatzApp.functions.hasDigit
import com.rettungshundeEinsatzApp.functions.hasLetter
import com.rettungshundeEinsatzApp.functions.hasMinLength
import com.rettungshundeEinsatzApp.functions.hasSpecialChar
import com.rettungshundeEinsatzApp.ui.ReaAppTheme
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewUserScreen(
    finishActivity: () -> Unit,
    getPrefs: () -> SharedPreferences,
    applicationContext: Application
){
    ReaAppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val scrollState = rememberScrollState()

            var username by remember { mutableStateOf("") }
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var phoneNumber by remember { mutableStateOf("") }
            var callSign by remember { mutableStateOf("") }
            var showColorPicker by remember { mutableStateOf(false) }
            var selectedColor by remember { mutableStateOf(Color.White) }
            var usernameError by remember { mutableStateOf(false) }
            var emailError by remember { mutableStateOf(false) }
            var phoneNumberError by remember { mutableStateOf(false) }
            var callSignError by remember { mutableStateOf(false) }
            var passwordError by remember { mutableStateOf(false) }
            val selectedHex = remember(selectedColor) {
                "#%06X".format(0xFFFFFF and selectedColor.toArgb())
            }
            val securityLevels = listOf(stringResource(id = R.string.security_level_ek), stringResource(id = R.string.security_level_zf), stringResource(id = R.string.security_level_admin))
            var expanded by remember { mutableStateOf(false) }
            var selectedSecurityLevel by remember { mutableStateOf("") }
            var securityLevelError by remember { mutableStateOf(false) }
            var passwordVisible by remember { mutableStateOf(false) }
            var showValidDialog by remember { mutableStateOf(false) }
            var isSubmitting by remember { mutableStateOf(false) }
            var showResultDialog by remember { mutableStateOf(false) }
            var resultMessage by remember { mutableStateOf("") }
            var resultSuccess by remember { mutableStateOf(false) }
            val ek = stringResource(R.string.security_level_ek)
            val zf = stringResource(R.string.security_level_zf)
            val admin = stringResource(R.string.security_level_admin)


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        stringResource(id = R.string.new_user_activity_title),
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .padding(start =16.dp, top = 16.dp, bottom = 5.dp)
                    )

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
                        value = password,
                        onValueChange = {
                            password = it
                            if (passwordError) passwordError = false
                        },
                        label = { Text(stringResource(id = R.string.password)) },
                        isError = passwordError,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible)
                                Icons.Default.Visibility
                            else Icons.Default.VisibilityOff

                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = if (passwordVisible) stringResource(id = R.string.password_hide) else stringResource(id = R.string.password_show))
                            }
                        },
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        PasswordRequirement(stringResource(id = R.string.password_requirement_min_length), hasMinLength(password))
                        PasswordRequirement(stringResource(id = R.string.password_requirement_letter), hasLetter(password))
                        PasswordRequirement(stringResource(id = R.string.password_requirement_digit), hasDigit(password))
                        PasswordRequirement(stringResource(id = R.string.password_requirement_special_char), hasSpecialChar(password))
                    }

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
                        Button(
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
                                    Text(stringResource(id = R.string.ok))
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                    Button(
                        onClick = {
                            if (!showValidDialog) {
                                val allValid = username.isNotBlank() &&
                                        isValidEmail(email) &&
                                        phoneNumber.isNotBlank() &&
                                        callSign.isNotBlank() &&
                                        selectedSecurityLevel.isNotBlank() &&
                                        hasMinLength(password) &&
                                        hasLetter(password) &&
                                        hasDigit(password) &&
                                        hasSpecialChar(password)

                                usernameError = username.isBlank()
                                emailError = !isValidEmail(email)
                                phoneNumberError = phoneNumber.isBlank()
                                callSignError = callSign.isBlank()
                                securityLevelError = selectedSecurityLevel.isBlank()
                                passwordError = !(hasMinLength(password) &&
                                        hasLetter(password) &&
                                        hasDigit(password) &&
                                        hasSpecialChar(password))


                                if (!allValid) {
                                    return@Button
                                }
                                showValidDialog = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !showValidDialog
                    ) {
                        Text(stringResource(id = R.string.create))
                    }
                }
            }

            if (showValidDialog) {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text(stringResource(id = R.string.new_user_activity_valid_Dialog_title)) },
                    text = { Text(stringResource(id = R.string.new_user_activity_valid_Dialog_text)) },
                    confirmButton = {
                        Button(onClick = {
                            showValidDialog = false
                            isSubmitting = true

                            val sharedPreferences = getPrefs()
                            val token: String = sharedPreferences.getString("token", "").toString()
                            val serverApiURL = sharedPreferences.getString("serverApiURL", "").toString()
                            val selectedSecurityLevelSend = when (selectedSecurityLevel) {
                                ek -> "1"
                                zf -> "2"
                                admin -> "3"
                                else -> "0"
                            }
                            Log.d("REA-NewUserActivity", "Securitylevel = $selectedSecurityLevelSend")


                            uploadNewUser(
                                serverApiURL, token, username, email, password, phoneNumber, callSign,
                                selectedSecurityLevelSend, selectedHex
                            ) { success, message ->
                                isSubmitting = false
                                resultMessage = message
                                resultSuccess = success
                                showResultDialog = true

                                if(resultSuccess) {
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
                            Text(stringResource(id = R.string.ok))
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

@Composable
fun PasswordRequirement(label: String, fulfilled: Boolean) {
    val icon = if (fulfilled) "✅" else "❌"
    val color = if (fulfilled) Color(0xFF4CAF50) else Color.Red

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 2.dp)
    ) {
        Text(
            text = icon,
            color = color,
            fontSize = 14.sp,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = label,
            color = color,
            fontSize = 14.sp
        )
    }
}

// ####################### Tp preview the activity Design ######################################
@Preview(name = "Light Mode", showBackground = true)
@Composable
fun PreviewNewUserScreenLight() {
    val fakePrefs = object : SharedPreferences {
        override fun getString(key: String?, defValue: String?) = defValue
        override fun getBoolean(key: String?, defValue: Boolean) = defValue
        override fun getInt(key: String?, defValue: Int) = defValue
        override fun getLong(key: String?, defValue: Long) = defValue
        override fun getFloat(key: String?, defValue: Float) = defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?) = defValues

        override fun contains(key: String?) = false
        override fun getAll(): MutableMap<String, *> = mutableMapOf(
            "token" to "PREVIEW_TOKEN",
            "serverURL" to "https://example.com"
        )
        override fun edit(): SharedPreferences.Editor = DummyEditor()
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    }

    val dummyApp = Application()

    NewUserScreen(
        finishActivity = {},
        getPrefs = { fakePrefs },
        applicationContext = dummyApp
    )
}

@Preview(name = "Dark Mode", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewNewUserScreenDark() {
    val fakePrefs = object : SharedPreferences {
        override fun getString(key: String?, defValue: String?) = defValue
        override fun getBoolean(key: String?, defValue: Boolean) = defValue
        override fun getInt(key: String?, defValue: Int) = defValue
        override fun getLong(key: String?, defValue: Long) = defValue
        override fun getFloat(key: String?, defValue: Float) = defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?) = defValues

        override fun contains(key: String?) = false
        override fun getAll(): MutableMap<String, *> = mutableMapOf(
            "token" to "PREVIEW_TOKEN",
            "serverURL" to "https://example.com"
        )
        override fun edit(): SharedPreferences.Editor = DummyEditor()
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    }

    val dummyApp = Application()

    NewUserScreen(
        finishActivity = {},
        getPrefs = { fakePrefs },
        applicationContext = dummyApp
    )
}

@Preview(name = "Large Font", fontScale = 1.5f, showBackground = true)
@Composable
fun PreviewNewUserScreenLargeFont() {
    val fakePrefs = object : SharedPreferences {
        override fun getString(key: String?, defValue: String?) = defValue
        override fun getBoolean(key: String?, defValue: Boolean) = defValue
        override fun getInt(key: String?, defValue: Int) = defValue
        override fun getLong(key: String?, defValue: Long) = defValue
        override fun getFloat(key: String?, defValue: Float) = defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?) = defValues

        override fun contains(key: String?) = false
        override fun getAll(): MutableMap<String, *> = mutableMapOf(
            "token" to "PREVIEW_TOKEN",
            "serverURL" to "https://example.com"
        )
        override fun edit(): SharedPreferences.Editor = DummyEditor()
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    }

    val dummyApp = Application()

    NewUserScreen(
        finishActivity = {},
        getPrefs = { fakePrefs },
        applicationContext = dummyApp
    )
}

class DummyEditor : SharedPreferences.Editor {
    override fun putString(key: String?, value: String?) = this
    override fun putStringSet(key: String?, values: MutableSet<String>?) = this
    override fun putInt(key: String?, value: Int) = this
    override fun putLong(key: String?, value: Long) = this
    override fun putFloat(key: String?, value: Float) = this
    override fun putBoolean(key: String?, value: Boolean) = this
    override fun remove(key: String?) = this
    override fun clear() = this
    override fun commit() = true
    override fun apply() {}
}