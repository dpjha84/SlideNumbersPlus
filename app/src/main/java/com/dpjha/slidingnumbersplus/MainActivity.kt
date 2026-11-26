package com.dpjha.slidingnumbersplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dpjha.slidingnumbersplus.ui.GameScreen
import com.dpjha.slidingnumbersplus.ui.theme.SlidingNumbersTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SlidingNumbersTheme {
                GameScreen()
            }
        }
    }
}
