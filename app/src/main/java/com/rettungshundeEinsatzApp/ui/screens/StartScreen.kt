package com.rettungshundeEinsatzApp.ui.screens

import android.util.Log
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.rettungshundeEinsatzApp.R
import com.rettungshundeEinsatzApp.ui.ReaAppTheme

@Composable
fun StartScreen(onLoginClick: () -> Unit) {

    val isDarkMode = isSystemInDarkTheme()
    val theSans = FontFamily(
        Font(R.font.the_sans_bold)
    )
    val logoRes = if (isDarkMode) {
        R.drawable.rea_icon_logo_light
    } else {
        R.drawable.rea_icon_logo_dark
    }

    ReaAppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .windowInsetsPadding(WindowInsets.statusBars) // to avoid text in or behind status bar
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = logoRes),
                    contentDescription = stringResource(id = R.string.logo_description),
                    modifier = Modifier.size(180.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Box{
                    Column{
                        TitleLine(stringResource(id = R.string.app_name_first_word), theSans)
                        TitleLine(stringResource(id = R.string.app_name_second_word), theSans)
                        TitleLine(stringResource(id = R.string.app_name_third_word), theSans)
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))

                Button(
                    onClick = {
                        Log.d("REA-StartActivity", "Button Login pressed")
                        onLoginClick()
                    },
                    modifier = Modifier
                        .width(250.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(stringResource(id = R.string.login))
                }
            }
        }
    }
}

@Composable
fun TitleLine(text: String, fontFamily: FontFamily) {
    Text(
        text = buildAnnotatedString {
            append(text)
            addStyle(
                style = SpanStyle(
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily,
                    fontSize = 35.sp
                ),
                start = 0,
                end = 1
            )
        },
        fontSize = 25.sp
    )
}

// ####################### Tp preview the activity Design ######################################
@Preview(name = "Light Mode", showBackground = true)
@Composable
fun PreviewStartScreenLight() {
    StartScreen(onLoginClick = {})
}

@Preview(name = "Dark Mode", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewStartScreenDark() {
    StartScreen(onLoginClick = {})
}

@Preview(name = "Large Font", fontScale = 1.5f, showBackground = true)
@Composable
fun PreviewStartScreenLargeFont() {
    StartScreen(onLoginClick = {})
}
