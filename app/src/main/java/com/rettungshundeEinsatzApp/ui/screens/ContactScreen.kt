package com.rettungshundeEinsatzApp.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataEntity
import com.rettungshundeEinsatzApp.viewmodel.userdata.AllUserProfileViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.net.toUri
import com.rettungshundeEinsatzApp.R
import com.rettungshundeEinsatzApp.ui.ReaAppTheme


@Composable
fun ContactScreen(viewModel: AllUserProfileViewModel = viewModel()) {
    val userList by viewModel.userList.collectAsState()
    ReaAppTheme { ContactScreenContent(userList = userList) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreenContent(userList: List<AllUserDataEntity>) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Contacts,
                            contentDescription = stringResource(id = R.string.contacts)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(stringResource(id = R.string.contacts))
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(start = 16.dp, top = 10.dp, bottom = 0.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(id = R.string.name), modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                Text(stringResource(id = R.string.radio_call_name), modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                Text(stringResource(id = R.string.phonenumber), modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
            }

            HorizontalDivider()

            LazyColumn {
                items(userList) { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(user.username, modifier = Modifier.weight(1f))
                        Text(user.radiocallname, modifier = Modifier.weight(1f))
                        Text(
                            text = user.phonenumber,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = "tel:${user.phonenumber}".toUri()
                                    }
                                    context.startActivity(intent)
                                }
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun ContactScreenPreviewLight() {
    val dummyList = listOf(
        AllUserDataEntity(
            id = 1,
            username = "Max ",
            email = "max@example.com",
            phonenumber = "+49 123 456789",
            securitylevel = 2,
            radiocallname = "28-123",
            trackColor = "#ff00cc"
        ),
        AllUserDataEntity(
            id = 2,
            username = "Anna ",
            email = "anna@example.com",
            phonenumber = "0160 987654",
            securitylevel = 1,
            radiocallname = "28-456",
            trackColor = null
        )
    )

    ReaAppTheme{ContactScreenContent(userList = dummyList)}

}

@Preview(name = "Dark Mode", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ContactScreenPreviewDark() {
    val dummyList = listOf(
        AllUserDataEntity(
            id = 1,
            username = "Max ",
            email = "max@example.com",
            phonenumber = "+49 123 456789",
            securitylevel = 2,
            radiocallname = "28-123",
            trackColor = "#ff00cc"
        ),
        AllUserDataEntity(
            id = 2,
            username = "Anna ",
            email = "anna@example.com",
            phonenumber = "0160 987654",
            securitylevel = 1,
            radiocallname = "28-456",
            trackColor = null
        )
    )

    ReaAppTheme{ContactScreenContent(userList = dummyList)}

}

@Preview(name = "Large Font", fontScale = 1.5f, showBackground = true)
@Composable
fun ContactScreenPreviewLarge() {
    val dummyList = listOf(
        AllUserDataEntity(
            id = 1,
            username = "Max ",
            email = "max@example.com",
            phonenumber = "+49 123 456789",
            securitylevel = 2,
            radiocallname = "28-123",
            trackColor = "#ff00cc"
        ),
        AllUserDataEntity(
            id = 2,
            username = "Anna ",
            email = "anna@example.com",
            phonenumber = "0160 987654",
            securitylevel = 1,
            radiocallname = "28-456",
            trackColor = null
        )
    )


    ReaAppTheme{ContactScreenContent(userList = dummyList)}

}