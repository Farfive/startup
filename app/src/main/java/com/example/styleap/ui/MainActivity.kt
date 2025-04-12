package com.example.styleap.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.styleap.ui.navigation.AppNavigation // Import the NavHost composable
import com.example.styleap.ui.theme.DreamTheme // Import your Compose Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Add if using Hilt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure edge-to-edge display if desired (optional but recommended for modern look)
        // enableEdgeToEdge()

        setContent {
            DreamTheme { // Apply your custom theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Set up the navigation host
                    AppNavigation()
                }
            }
        }
    }
} 