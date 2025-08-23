package com.rettungshundeEinsatzApp.ui

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext


private val LightColorScheme = lightColorScheme(

    //primary = PrimaryColor, // for example color of button
    //onPrimary = OnPrimaryColor, // text color on primary color
    //primaryContainer = PrimaryVariantColor,
    //onPrimaryContainer = Color.White,
    //secondary = SecondaryColor,
    //onSecondary = Color.White,
    // background = BackgroundColor,
    //surface = BackgroundColor,
    //onBackground = Color.Black,
    //surface = Color.White,
    //onSurface = Color.Black,
    error = ErrorColor,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    //primary = PrimaryColor,
    //onPrimary = OnPrimaryColor,
    //primaryContainer = PrimaryVariantColor,
    //onPrimaryContainer = Color.White,
    //secondary = SecondaryColor,
    //onSecondary = Color.White,
    //background = Color.Black,
    //onBackground = Color.White,
    //surface = Color.Black,
    //onSurface = Color.White,
    error = ErrorColor,
    onError = Color.Black
)

@RequiresApi(Build.VERSION_CODES.S)
private fun dynamicScheme(context: Context, dark: Boolean) =
    if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

@Composable
fun ReaAppTheme(
    useDynamicColor: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()
    val dynamic = useDynamicColor

    val colorScheme = remember(context, darkTheme, dynamic) {
        if (dynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching { dynamicScheme(context, darkTheme) }
                .getOrElse { if (darkTheme) DarkColorScheme else LightColorScheme }
        } else {
            if (darkTheme) DarkColorScheme else LightColorScheme
        }
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}