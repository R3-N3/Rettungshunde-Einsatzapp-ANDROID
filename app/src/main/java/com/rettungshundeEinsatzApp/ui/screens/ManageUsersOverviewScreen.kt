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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rettungshundeEinsatzApp.R
import com.rettungshundeEinsatzApp.activity.ManageUserSingleViewActivity
import com.rettungshundeEinsatzApp.activity.ManageUsersOverviewActivity
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataEntity
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataProvider
import com.rettungshundeEinsatzApp.functions.deleteUser
import com.rettungshundeEinsatzApp.functions.downloadAllUserData
import com.rettungshundeEinsatzApp.ui.ReaAppTheme
import com.rettungshundeEinsatzApp.viewmodel.AllUserProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsersOverviewScreen(
    context: Context,
    application: Application,
    finishActivity: () -> Unit
) {

    val sharedPreferences = context.getSharedPreferences("REAPrefs", MODE_PRIVATE)
    val token: String = sharedPreferences.getString("token", "").toString()
    val serverApiURL = sharedPreferences.getString("serverApiURL", "").toString()
    val myUserName = sharedPreferences.getString("username", "").toString()
    var showDeleteNotPossibleDialog by remember { mutableStateOf(false) }
    var showDeleteUserDialog: Boolean by remember { mutableStateOf(false) }
    var isSubmitting: Boolean by remember { mutableStateOf(false) }
    var showResultDialog: Boolean by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }
    var resultSuccess by remember { mutableStateOf(false) }
    var toDeleteUserName = ""

    ReaAppTheme {
        val viewModel: AllUserProfileViewModel = viewModel()
        val userList by viewModel.userList.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.settings_manage_users)) },
                    navigationIcon = {
                        IconButton(onClick = {  }) {
                            Icon(Icons.Default.Groups, contentDescription = stringResource(id = R.string.dashboard_bottom_menu_group))
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(start =16.dp, top = 10.dp, bottom = 16.dp, end = 16.dp)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(userList) { user ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = user.username,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = "ID: ${user.id}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = stringResource(id = R.string.radio_call_name) + ": ${user.radiocallname}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = stringResource(id = R.string.email) + ": ${user.email}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                val secLevel = when (user.securitylevel) {
                                    1 -> ": " + stringResource(id = R.string.security_level_ek)
                                    2 -> ": " + stringResource(id = R.string.security_level_zf)
                                    3 -> ": " + stringResource(id = R.string.security_level_admin)
                                    else -> ": unknown"
                                }

                                Text(
                                    text = stringResource(id = R.string.security_level) + secLevel,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.track_color) + ": ${user.trackColor}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    user.trackColor?.let { colorCode ->
                                        runCatching {
                                            Color(colorCode.toColorInt())
                                        }.getOrNull()?.let { parsedColor ->
                                            Box(
                                                modifier = Modifier
                                                    .padding(start = 4.dp)
                                                    .size(20.dp)
                                                    .background(parsedColor, shape = MaterialTheme.shapes.small)
                                                    .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.small)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = stringResource(id = R.string.phonenumber) + ": ${user.phonenumber}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 0.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = {
                                    val intentManageSingleUser = Intent(context, ManageUserSingleViewActivity::class.java)
                                    intentManageSingleUser.putExtra("username",user.username)
                                    intentManageSingleUser.putExtra("email", user.email)
                                    intentManageSingleUser.putExtra("radioCallName",user.radiocallname)
                                    intentManageSingleUser.putExtra("securityLevel", user.securitylevel.toString())
                                    intentManageSingleUser.putExtra("trackColor", user.trackColor.toString())
                                    intentManageSingleUser.putExtra("phoneNumber",user.phonenumber)
                                    intentManageSingleUser.putExtra("id",user.id.toString())
                                    context.startActivity(intentManageSingleUser)
                                    finishActivity()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = stringResource(id = R.string.edit_user),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                IconButton(onClick = {
                                    if(user.username == myUserName){
                                        showDeleteNotPossibleDialog = true
                                    }
                                    else{
                                        toDeleteUserName = user.username
                                        showDeleteUserDialog = true
                                    }

                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(id = R.string.delete_user),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showDeleteNotPossibleDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteNotPossibleDialog = false },
                    confirmButton = {
                        Button(onClick = {
                            showDeleteNotPossibleDialog = false
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
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = Color.Red
                            )
                            Text(
                                text = stringResource(id = R.string.dialog_delete_user_not_possible_title)
                            )
                        }
                    },
                    text = {
                        Text(
                            text = stringResource(id = R.string.dialog_delete_user_not_possible_text)
                        )
                    }
                )
            }

            if (showDeleteUserDialog) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.Yellow
                            )
                            Text(
                                text = stringResource(id = R.string.dialog_delete_user_title)
                            )
                        } },
                    text = { Text(stringResource(id = R.string.dialog_delete_user_text_line1) + toDeleteUserName + stringResource(id = R.string.dialog_delete_user_text_line2)) },
                    confirmButton = {
                        Button(onClick = {
                            showDeleteUserDialog = false
                            isSubmitting = true


                            deleteUser(
                                serverApiURL, token, toDeleteUserName
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
                            showDeleteUserDialog = false
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
                                val intent = Intent(context, ManageUsersOverviewActivity::class.java)
                                context.startActivity(intent)
                                finishActivity()

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
                            text = stringResource(id = R.string.dialog_delete_user_successful_line1) + resultMessage + stringResource(id = R.string.dialog_delete_user_successful_line2)
                        )
                    }
                )
            }
        }
    }
}



// ####################### Tp preview the activity Design ######################################
@Preview(name = "Light Mode", showBackground = true)
@Composable
fun PreviewManageUsersOverviewScreenLight() {
    val dummyUsers = listOf(
        AllUserDataEntity(
            id = 1,
            username = "max.doe",
            email = "max@example.com",
            phonenumber = "0123456789",
            securitylevel = 2,
            radiocallname = "28/MM",
            trackColor = "#FF0000"
        ),
        AllUserDataEntity(
            id = 2,
            username = "jane.doe",
            email = "jane@example.com",
            phonenumber = "0987654321",
            securitylevel = 1,
            radiocallname = "28/JD",
            trackColor = "#00FF00"
        )
    )

    ReaAppTheme {
        ManageUsersOverviewPreviewScreen(userList = dummyUsers)
    }
}

@Preview(name = "Dark Mode", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewManageUsersOverviewScreenDark() {
    val dummyUsers = listOf(
        AllUserDataEntity(
            id = 1,
            username = "max.doe",
            email = "max@example.com",
            phonenumber = "0123456789",
            securitylevel = 2,
            radiocallname = "28/MM",
            trackColor = "#FF0000"
        ),
        AllUserDataEntity(
            id = 2,
            username = "jane.doe",
            email = "jane@example.com",
            phonenumber = "0987654321",
            securitylevel = 1,
            radiocallname = "28/JD",
            trackColor = "#00FF00"
        )
    )

    ReaAppTheme {
        ManageUsersOverviewPreviewScreen(userList = dummyUsers)
    }
}

@Preview(name = "Large Font", fontScale = 1.5f, showBackground = true)
@Composable
fun PreviewManageUsersOverviewScreenLarge() {
    val dummyUsers = listOf(
        AllUserDataEntity(
            id = 1,
            username = "max.doe",
            email = "max@example.com",
            phonenumber = "0123456789",
            securitylevel = 2,
            radiocallname = "28/MM",
            trackColor = "#FF0000"
        ),
        AllUserDataEntity(
            id = 2,
            username = "jane.doe",
            email = "jane@example.com",
            phonenumber = "0987654321",
            securitylevel = 1,
            radiocallname = "28/JD",
            trackColor = "#00FF00"
        )
    )

    ReaAppTheme {
        ManageUsersOverviewPreviewScreen(userList = dummyUsers)
    }
}

@Composable
fun ManageUsersOverviewPreviewScreen(userList: List<AllUserDataEntity>) {
    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(userList) { user ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = user.username, style = MaterialTheme.typography.titleMedium)
                    Text(text = "Radio Call name: ${user.radiocallname}")
                    Text(text = "E-Mail: ${user.email}")
                    Text(text = "phone number: ${user.phonenumber}")
                    Text(text = "Level: ${user.securitylevel}")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Track-Color: ${user.trackColor}")
                        Spacer(modifier = Modifier.width(8.dp))
                        runCatching { Color(user.trackColor?.toColorInt() ?: 0) }
                            .getOrNull()?.let { parsedColor ->
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(parsedColor)
                                        .border(1.dp, Color.Gray)
                                )
                            }
                    }
                }
            }
        }
    }
}
