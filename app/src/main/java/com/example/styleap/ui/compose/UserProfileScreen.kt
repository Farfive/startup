package com.example.styleap.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.styleap.R // Adjust if R file is elsewhere

@Composable
fun UserProfileScreen(
    modifier: Modifier = Modifier,
    username: String,
    points: Int,
    level: Int,
    isPremium: Boolean, // To conditionally show/hide the premium card or change button
    onPurchasePremiumClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_account_circle),
            contentDescription = stringResource(id = R.string.desc_user_avatar),
            modifier = Modifier
                .size(80.dp)
                .padding(top = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = username,
            style = MaterialTheme.typography.headlineSmall
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

        Spacer(modifier = Modifier.height(32.dp))

        if (!isPremium) { // Show card only if user is not premium
            PremiumCard(onPurchasePremiumClick = onPurchasePremiumClick)
        } else {
           Text(
               text = stringResource(id = R.string.premium_active),
               style = MaterialTheme.typography.labelLarge,
               color = MaterialTheme.colorScheme.tertiary // Example color
           )
        }

        // Add Spacer to push content up if needed, or handle scrolling
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    onPurchasePremiumClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        // Use elevated or filled based on your theme design
        // elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        // colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.premium_benefits_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary // Use theme colors
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.premium_benefits_description),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onPurchasePremiumClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    Icons.Filled.WorkspacePremium,
                    contentDescription = null, // Button text describes action
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(id = R.string.purchase_premium_button))
            }
        }
    }
}

@Preview(showBackground = true, name = "User Profile - Not Premium")
@Composable
fun UserProfileScreenNotPremiumPreview() {
    Surface {
        UserProfileScreen(
            username = "JohnDoe",
            points = 250,
            level = 5,
            isPremium = false,
            onPurchasePremiumClick = {}
        )
    }
}

@Preview(showBackground = true, name = "User Profile - Premium")
@Composable
fun UserProfileScreenPremiumPreview() {
    Surface {
        UserProfileScreen(
            username = "JanePremium",
            points = 1500,
            level = 10,
            isPremium = true,
            onPurchasePremiumClick = {}
        )
    }
} 