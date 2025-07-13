package com.rettungshundeEinsatzApp.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rettungshundeEinsatzApp.database.alluserdataandlocations.AllUserDataEntity
import com.rettungshundeEinsatzApp.viewmodel.AllUserProfileViewModel
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

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(userList) { user ->
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)) {

                        Text(user.username, style = MaterialTheme.typography.titleMedium)
                        Text("Funkrufname: ${user.radiocallname}")
                        Text(
                            text = "${user.phonenumber}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable {
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = android.net.Uri.parse("tel:${user.phonenumber}")
                                    }
                                    context.startActivity(dialIntent)
                                }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(top = 16.dp),
                            thickness = DividerDefaults.Thickness
                        )
                    }
                }
            }
        }
    }
}
