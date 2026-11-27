package com.dpjha.slidingnumbersplus.ui.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.dpjha.slidingnumbersplus.R

val Ink = Color(0xFF150F33)
val Panel = Color(0xA818103A)
val Line = Color(0x29FFECC8)
val Cream = Color(0xFFFFF4DC)
val Gold = Color(0xFFFFC54D)
val GoldDark = Color(0xFFC9771A)
val Coral = Color(0xFFFF6B6B)
val Teal = Color(0xFF43E6D2)
val Violet = Color(0xFF9B7BFF)

val Display = FontFamily(Font(R.font.lilita_one))
val Body = FontFamily(Font(R.font.nunito_bold, FontWeight.Bold), Font(R.font.nunito_black, FontWeight.Black))

@Composable
fun SlidingNumbersTheme(content: @Composable () -> Unit) {
    val colors = darkColorScheme(primary = Gold, onPrimary = Ink, secondary = Teal, background = Ink, surface = Ink, onSurface = Cream, onBackground = Cream)
    MaterialTheme(colorScheme = colors) {
        CompositionLocalProvider(LocalTextStyle provides TextStyle(fontFamily = Body, fontWeight = FontWeight.Bold, color = Cream)) {
            content()
        }
    }
}
