package com.example.dream.ui.season // Adjust package name if needed

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel // For easy ViewModel injection in Composable destinations
import com.example.styleap.data.model.Season // Correct package for Season
import com.example.styleap.data.model.SeasonConstants // Correct package for SeasonConstants
import com.example.styleap.data.model.UserSeasonProgress // Correct package for UserSeasonProgress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SeasonStatusView(
    modifier: Modifier = Modifier, // Allow passing modifiers
    viewModel: SeasonViewModel = hiltViewModel() // Inject ViewModel using Hilt Compose Navigation
) {
    // Collect state from ViewModel
    val season by viewModel.activeSeason.collectAsState()
    val progress by viewModel.userSeasonProgress.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Remember date formatter
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Card( // Use a Card for better visual grouping
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth() // Ensure Column takes width for alignment
        ) {
            if (isLoading) {
                // Show loading indicator
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                // Display season info once loaded
                if (season != null) {
                    Text(
                        text = season!!.seasonName.ifBlank { "Current Season" }, // Fallback name
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Ends on: ${dateFormat.format(season!!.endDate)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Display user progress
                    val currentLevel = progress?.level ?: SeasonConstants.STARTING_LEVEL
                    val currentPoints = progress?.pointsInSeason ?: 0L
                    // Optional: Calculate points needed for next level
                    // val pointsForNextLevel = ((currentLevel + 1) * 100) // Based on calculateLevel logic
                    // val pointsNeeded = (pointsForNextLevel - currentPoints).coerceAtLeast(0)

                    Text("Your Level: $currentLevel", style = MaterialTheme.typography.bodyLarge)
                    Text("Points: $currentPoints", style = MaterialTheme.typography.bodyMedium)
                    // Optional: Progress bar or points needed text
                    // LinearProgressIndicator(progress = (currentPoints % 100) / 100f ) // Example progress within level
                    // Text("Next level in $pointsNeeded points", style = MaterialTheme.typography.bodySmall)

                } else {
                    // Display message when no season is active
                    Text(
                        text = "No active season currently.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

// --- Preview Function ---
// Helps visualize the composable in Android Studio previews

@Preview(showBackground = true)
@Composable
fun SeasonStatusViewPreview_ActiveSeason() {
    // Fake data for preview
    val fakeSeason = Season(seasonId = "s1", seasonName = "Summer Splash '24", endDate = Date(System.currentTimeMillis() + 1000000000))
    val fakeProgress = UserSeasonProgress(userId = "u1", seasonId = "s1", level = 7, pointsInSeason = 745L)

    // Simulate ViewModel state for preview
    val fakeViewModel: SeasonViewModel = object : SeasonViewModel(
        // Provide dummy implementations or mocks if necessary
        seasonService = TODO(),
        seasonRepository = TODO(),
        userRepository = TODO()
    ) {
         override val activeSeason = MutableStateFlow(fakeSeason).asStateFlow()
         override val userSeasonProgress = MutableStateFlow(fakeProgress).asStateFlow()
         override val isLoading = MutableStateFlow(false).asStateFlow()
    }

    MaterialTheme { // Wrap preview in a theme
         SeasonStatusView(viewModel = fakeViewModel) // Pass the fake ViewModel
    }
}

@Preview(showBackground = true)
@Composable
fun SeasonStatusViewPreview_NoSeason() {
     val fakeViewModel: SeasonViewModel = object : SeasonViewModel(
         seasonService = TODO(),
         seasonRepository = TODO(),
         userRepository = TODO()
    ) {
         override val activeSeason = MutableStateFlow<Season?>(null).asStateFlow()
         override val userSeasonProgress = MutableStateFlow<UserSeasonProgress?>(null).asStateFlow()
         override val isLoading = MutableStateFlow(false).asStateFlow()
    }
    MaterialTheme {
        SeasonStatusView(viewModel = fakeViewModel)
    }
}

@Preview(showBackground = true)
@Composable
fun SeasonStatusViewPreview_Loading() {
     val fakeViewModel: SeasonViewModel = object : SeasonViewModel(
         seasonService = TODO(),
         seasonRepository = TODO(),
         userRepository = TODO()
    ) {
         override val isLoading = MutableStateFlow(true).asStateFlow()
    }
     MaterialTheme {
        SeasonStatusView(viewModel = fakeViewModel)
    }
} 