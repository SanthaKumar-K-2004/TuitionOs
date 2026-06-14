package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.SecurePreferencesManager
import com.example.ui.TuitionOSApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // One-time migration of API keys from plain to encrypted SharedPreferences
        SecurePreferencesManager(this).migrateFromPlainPrefs(this)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TuitionOSApp()
            }
        }
    }
}
