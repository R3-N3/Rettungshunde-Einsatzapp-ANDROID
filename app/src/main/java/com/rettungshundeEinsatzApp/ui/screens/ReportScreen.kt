package com.rettungshundeEinsatzApp.ui.screens


import android.app.DatePickerDialog
import android.app.Service.MODE_PRIVATE
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rettungshundeEinsatzApp.R
import com.rettungshundeEinsatzApp.ui.ReaAppTheme
import java.util.Calendar
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import com.rettungshundeEinsatzApp.functions.uploadReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun ReportScreen() {
    ReaAppTheme { ReportScreenContent() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreenContent() {
    val context = LocalContext.current
    var reportText by rememberSaveable { mutableStateOf("") }
    val sharedPreferences = context.getSharedPreferences("REAPrefs", MODE_PRIVATE)
    val myUserName = sharedPreferences.getString("username", "").toString()
    val scrollState = rememberScrollState()
    var showValidDialog by rememberSaveable { mutableStateOf(false) }
    var isSubmitting by rememberSaveable { mutableStateOf(false) }
    var showResultDialog by rememberSaveable { mutableStateOf(false) }
    var resultMessage by rememberSaveable { mutableStateOf("") }
    var resultSuccess by rememberSaveable { mutableStateOf(false) }
    var selectedDate by rememberSaveable {
        val calendar = Calendar.getInstance()
        mutableStateOf("%02d.%02d.%04d".format(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR)))
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Receipt,
                            contentDescription = stringResource(id = R.string.report)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(stringResource(id = R.string.report))
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
        ) {



            Text(
                text = stringResource(id = R.string.report_info_text),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            OutlinedTextField(
                value = myUserName,
                onValueChange = {
                },
                label = { Text(stringResource(id = R.string.name)) },
                isError = false,
                singleLine = true,
                readOnly = true,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            )

            DatePickerTextField(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )

            OutlinedTextField(
                value = reportText,
                onValueChange = { reportText = it },
                label = { Text(stringResource(id = R.string.report)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = false,
                maxLines = 6,
                minLines = 6,
                placeholder = { Text(stringResource(id = R.string.report_description_input_field)) }
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, bottom = 0.dp, end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        (context as? android.app.Activity)?.finish()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = true
                ) {
                    Text(stringResource(id = R.string.cancel))
                }

                OutlinedButton(
                    onClick = {
                        showValidDialog = true
                    },
                    modifier = Modifier.weight(1f),
                    enabled = true
                ) {
                    Text(stringResource(id = R.string.send))                }


            }
        }
    }

    if (showValidDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(id = R.string.send_report_title)) },
            text = { Text(stringResource(id = R.string.send_report_message)) },
            confirmButton = {
                Button(onClick = {
                    val token: String = sharedPreferences.getString("token", "").toString()
                    val serverApiURL = sharedPreferences.getString("serverApiURL", "").toString()

                    CoroutineScope(Dispatchers.Main).launch {
                        isSubmitting = true

                        val (success, message) = uploadReport(serverApiURL, token, myUserName, selectedDate, reportText)

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
                    if (resultSuccess) (context as? android.app.Activity)?.finish()
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


@Composable
fun DatePickerTextField(
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    val label = stringResource(id = R.string.select_date)
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, y: Int, m: Int, d: Int ->
            onDateSelected("%02d.%02d.%04d".format(d, m + 1, y))
        }, year, month, day
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { datePickerDialog.show() }
    ) {
        OutlinedTextField(
            value = selectedDate,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            colors = TextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledIndicatorColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.background
            )
        )
    }
}

// ####################### Tp preview the activity Design ######################################
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ReportScreenPreview() {
    ReportScreen()
}