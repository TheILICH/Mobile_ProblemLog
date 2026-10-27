package com.example.problemlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.problemlog.ui.navigation.ProblemLogApp
import com.example.problemlog.ui.theme.ProblemLogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProblemLogTheme {
                ProblemLogApp()
            }
        }
    }
}
