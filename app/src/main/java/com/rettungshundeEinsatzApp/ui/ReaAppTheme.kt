package com.rettungshundeEinsatzApp.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
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

/*
@Composable
fun ReaAppThemeWithOwnColor(
    content: @Composable () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        //typography = Typography, // optional definition of text
        //shapes = Shapes, // optional definition of shapes
        content = content
    )
}
*/

@Composable
fun ReaAppTheme(
    useDynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()

    val colorScheme = when {
        useDynamicColor -> {
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}