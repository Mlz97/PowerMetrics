package com.example.feperfectsquat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.feperfectsquat.ui.theme.FEPerfectSquatTheme
import com.example.feperfectsquat.ui.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FEPerfectSquatTheme {
                AppNavigation()
            }
        }
    }
}