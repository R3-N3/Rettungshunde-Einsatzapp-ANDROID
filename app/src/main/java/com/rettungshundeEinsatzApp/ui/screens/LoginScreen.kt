package com.rettungshundeEinsatzApp.ui.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.rettungshundeEinsatzApp.R
import com.rettungshundeEinsatzApp.functions.checkLoginParam
import com.rettungshundeEinsatzApp.ui.ReaAppTheme
import androidx.compose.ui.platform.LocalContext
import com.rettungshundeEinsatzApp.activity.ResetPasswordActivity
import com.rettungshundeEinsatzApp.ui.components.OrganisationDropdown
import com.rettungshundeEinsatzApp.ui.components.getServerApiUrlForOrganisation

@Composable
fun LoginScreen(
    onLoginSuccess: (token: String, serverURL: String) -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("REAPrefs", Context.MODE_PRIVATE)

    val isDarkMode = isSystemInDarkTheme()
    var isEnabled by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var serverApiURL by remember { mutableStateOf("") }
    val selectedOrgOptionDefault = stringResource(id = R.string.login_organisation_choice)
    var selectedOrgOption by remember { mutableStateOf(selectedOrgOptionDefault) }

    val errorToast = stringResource(id = R.string.login_login)

    ReaAppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                val logo = if (isDarkMode) R.drawable.rea_icon_logo_light else R.drawable.rea_icon_logo_dark
                Image(
                    painter = painterResource(logo),
                    contentDescription = stringResource(id = R.string.logo_description),
                    modifier = Modifier.size(100.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))


                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    value = username,
                    onValueChange = { username = it },
                    enabled = isEnabled,
                    singleLine = true,
                    isError = isError,
                    label = { Text(stringResource(id = R.string.username)) },
                    supportingText = {
                        if (isError) Text(stringResource(id = R.string.login_check_input), color = MaterialTheme.colorScheme.error)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    value = password,
                    onValueChange = { password = it },
                    enabled = isEnabled,
                    singleLine = true,
                    isError = isError,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    label = { Text(stringResource(id = R.string.password)) },
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) stringResource(id = R.string.password_hide) else stringResource(id = R.string.password_show)
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(icon, contentDescription = description)
                        }
                    },
                    supportingText = {
                        if (isError) Text(stringResource(id = R.string.login_check_input), color = MaterialTheme.colorScheme.error)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OrganisationDropdown(
                    selectedOrgOption = selectedOrgOption,
                    onOptionSelected = { selectedOrgOption = it },
                    isEnabled = isEnabled,
                    isError = isError
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        isEnabled = false
                        val apiUrl = getServerApiUrlForOrganisation(context, selectedOrgOption)
                        serverApiURL = apiUrl

                        if (serverApiURL.isNotBlank() && username.isNotBlank() && password.isNotBlank()) {
                            checkLoginParam(context, username, password, serverApiURL) { message ->
                                val response = message.split(",")
                                val status = response[0]
                                val responseToken = response.getOrElse(2) { "" }
                                if (status == "success") {
                                    Log.d("LoginScreen", "Login Success, Server API URL: $serverApiURL  and token = $responseToken")
                                    sharedPreferences.edit {
                                        putString("token", responseToken)
                                        putString("serverApiURL", serverApiURL)
                                    }
                                    onLoginSuccess(responseToken, serverApiURL)
                                } else {
                                    isError = true
                                    isEnabled = true
                                    Toast.makeText(context, errorToast, Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            isError = true
                            isEnabled = true
                        }
                    },
                    enabled = isEnabled
                ) {
                    Text(stringResource(id = R.string.login_login))
                }

                Spacer(modifier = Modifier.height(100.dp))

                Button(
                    onClick = {
                        context.startActivity(Intent(context, ResetPasswordActivity::class.java))
                    },
                    enabled = true
                ) {
                    Text(stringResource(id = R.string.login_resetPassword))
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun PreviewLoginScreenLight() {
    LoginScreen { _, _ -> }
}

@Preview(name = "Dark Mode", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewLoginScreenDark() {
    LoginScreen { _, _ -> }
}

@Preview(name = "Large Font", fontScale = 1.5f, showBackground = true)
@Composable
fun PreviewLoginScreenLargeFont() {
    LoginScreen { _, _ -> }
}