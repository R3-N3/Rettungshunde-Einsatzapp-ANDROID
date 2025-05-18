package com.rettungshundeEinsatzApp.ui.components

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rettungshundeEinsatzApp.R
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganisationDropdown(
    selectedOrgOption: String,
    onOptionSelected: (String) -> Unit,
    isEnabled: Boolean,
    isError: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val orgOptions = getOrgOptions(context)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOrgOption,
            onValueChange = {},
            readOnly = true,
            enabled = isEnabled,
            singleLine = true,
            isError = isError,
            label = { Text(stringResource(id = R.string.login_organisation)) },
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Open Dropdown Menu")
            },
            modifier = Modifier
                .menuAnchor(PrimaryNotEditable, isEnabled)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 5.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            orgOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}



fun getOrgOptions(context: Context): List<String> {
    val list = mutableListOf(
        context.getString(R.string.login_organisation_choice),
        context.getString(R.string.login_organisation_choice_1),
        context.getString(R.string.login_organisation_choice_2)
    )

    val isDebug = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

    if (isDebug) {
        Log.d("DEBUG_TEST", "Debug block reached")
        list.add("Debug/Test")
    }

    return list
}


fun getServerApiUrlForOrganisation(context: Context, selectedOption: String): String {
    val orgOptions = getOrgOptions(context)
    return when (selectedOption) {
        orgOptions[1] -> "https://api.rettungshunde-einsatzapp.de/brh28/"
        orgOptions[2] -> "https://api.rettungshunde-einsatzapp.de/demo/"
        orgOptions[3] -> "https://api.rettungshunde-einsatzapp.de/debug/"
        else -> ""
    }
}

