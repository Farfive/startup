package com.example.styleap.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OndemandVideo // Correct icon name
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.styleap.R // Adjust if R file is elsewhere

@Composable
fun ProgressScreen(
    modifier: Modifier = Modifier,
    username: String,
    points: Int,
    level: Int,
    progress: Float, // Progress towards next level (0.0f to 1.0f)
    isAdAvailable: Boolean,
    isWithdrawEnabled: Boolean,
    isLoading: Boolean,
    onWatchAdClick: () -> Unit,
    onWithdrawClick: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize() // Take full size within the Box
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User Info Card
            UserInfoCard(username = username, points = points, level = level)

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Bar Card
            ProgressIndicatorCard(progress = progress)

            Spacer(modifier = Modifier.height(24.dp))

            // Watch Ad Button
            Button(
                onClick = onWatchAdClick,
                enabled = isAdAvailable && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    Icons.Filled.OndemandVideo,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(id = R.string.watch_ad_button))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Withdraw Button
            OutlinedButton(
                onClick = onWithdrawClick,
                enabled = isWithdrawEnabled && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(stringResource(id = R.string.action_withdraw_points))
            }

            // Pushes content towards top
            Spacer(modifier = Modifier.weight(1f))
        }

        // Loading Indicator (overlay)
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun UserInfoCard(
    modifier: Modifier = Modifier,
    username: String,
    points: Int,
    level: Int
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        // elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = username,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.progress_points_format, points),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.progress_level_format, level),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun ProgressIndicatorCard(
    modifier: Modifier = Modifier,
    progress: Float
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        // elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.progress_header),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                // Customize trackColor, color as needed via MaterialTheme or params
                // trackColor = MaterialTheme.colorScheme.surfaceVariant,
                // color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Preview(showBackground = true, name = "Progress Screen - Ready")
@Composable
fun ProgressScreenPreview() {
    Surface {
        ProgressScreen(
            username = "PlayerOne",
            points = 120,
            level = 2,
            progress = 0.6f,
            isAdAvailable = true,
            isWithdrawEnabled = true,
            isLoading = false,
            onWatchAdClick = {},
            onWithdrawClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Progress Screen - Loading")
@Composable
fun ProgressScreenLoadingPreview() {
    Surface {
        ProgressScreen(
            username = "PlayerOne",
            points = 120,
            level = 2,
            progress = 0.6f,
            isAdAvailable = false,
            isWithdrawEnabled = false,
            isLoading = true,
            onWatchAdClick = {},
            onWithdrawClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Progress Screen - No Ad")
@Composable
fun ProgressScreenNoAdPreview() {
    Surface {
        ProgressScreen(
            username = "PlayerOne",
            points = 120,
            level = 2,
            progress = 0.6f,
            isAdAvailable = false,
            isWithdrawEnabled = true,
            isLoading = false,
            onWatchAdClick = {},
            onWithdrawClick = {}
        )
    }
} 