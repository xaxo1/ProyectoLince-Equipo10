package com.equipo10.lince

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.equipo10.lince.ui.navigation.LinceApp
import com.equipo10.lince.ui.theme.LinceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Íconos de la barra de estado en blanco (van sobre el café de la TopAppBar y del Login)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent {
            LinceTheme {
                LinceApp()
            }
        }
    }
}
