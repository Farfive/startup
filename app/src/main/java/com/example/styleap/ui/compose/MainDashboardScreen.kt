package com.example.styleap.ui.compose

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.styleap.R
import com.example.styleap.data.model.Employee // Keep dummy for now if CompanyProfileScreen wasn't updated
import com.example.styleap.data.model.EmployeeInfo
import com.example.styleap.ui.navigation.AppDestinations
import com.example.styleap.ui.viewmodel.*
import androidx.compose.ui.unit.dp

// Data class defining bottom navigation items
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter") // Will be used by NavHost
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    navController: NavHostController, // Main NavController (passed from AppNavigation)
    onLogout: () -> Unit
) {
    val bottomNavItems = listOf(
        // Define your bottom navigation items here
        BottomNavItem(
            label = stringResource(R.string.bottom_nav_home), // Add string resource
            icon = Icons.Filled.Home,
            route = AppDestinations.DASHBOARD_HOME_ROUTE
        ),
        BottomNavItem(
            label = stringResource(R.string.bottom_nav_progress), // Add string resource
            icon = Icons.Filled.ShowChart,
            route = AppDestinations.DASHBOARD_PROGRESS_ROUTE
        ),
        BottomNavItem(
            label = stringResource(R.string.bottom_nav_profile), // Add string resource
            icon = Icons.Filled.AccountCircle,
            route = AppDestinations.DASHBOARD_USER_PROFILE_ROUTE
        ),
         BottomNavItem(
            label = stringResource(R.string.bottom_nav_company), // Add string resource
            icon = Icons.Filled.Business,
            route = AppDestinations.DASHBOARD_COMPANY_PROFILE_ROUTE
        )
    )

    // Use a separate NavController for the content within the dashboard
    val dashboardNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by dashboardNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            dashboardNavController.navigate(item.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(dashboardNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // Nested NavHost for the dashboard content
        NavHost(
            navController = dashboardNavController,
            startDestination = AppDestinations.DASHBOARD_HOME_ROUTE, // Start at home
            modifier = Modifier.padding(innerPadding) // Apply padding from Scaffold
        ) {
            composable(AppDestinations.DASHBOARD_HOME_ROUTE) {
                // --- Home Screen Content --- 
                HomeScreenContent(onLogout = onLogout)
            }
            composable(AppDestinations.DASHBOARD_PROGRESS_ROUTE) {
                ProgressScreenContent() // Extracted content
            }
            composable(AppDestinations.DASHBOARD_USER_PROFILE_ROUTE) {
                UserProfileScreenContent() // Extracted content
            }
            composable(AppDestinations.DASHBOARD_COMPANY_PROFILE_ROUTE) {
                CompanyProfileScreenContent() // Extracted content
            }
            // Add other composables for nested routes if needed
        }
    }
}

// --- Placeholder Home Screen --- 
@Composable
fun HomeScreenContent(onLogout: () -> Unit) {
     Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.home_title), // Add string resource
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Example Placeholder Card 1
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Feature Area 1", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text("Content related to the first feature or summary information goes here.")
            }
        }

        // Example Placeholder Card 2
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Feature Area 2", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text("More content or perhaps quick actions could be placed here.")
            }
        }

        Spacer(modifier = Modifier.weight(1f)) // Push logout button to bottom

        // Example Logout Button
        Button(onClick = onLogout) {
            Text(stringResource(R.string.action_logout)) // Add string resource
        }
    }
}

// --- Extracted Content Composables --- //
// These fetch their own ViewModels and handle their state, same as before

@Composable
fun ProgressScreenContent() {
    val context = LocalContext.current
    val progressViewModel: ProgressViewModel = hiltViewModel()
    val uiState by progressViewModel.uiState.collectAsState()

    // Handle messages (Ad Reward, Withdraw)
    val message = uiState.adRewardMessage ?: uiState.withdrawResultMessage
    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            if (uiState.adRewardMessage != null) progressViewModel.onRewardMessageConsumed()
            if (uiState.withdrawResultMessage != null) progressViewModel.onWithdrawMessageConsumed()
        }
    }
    // Handle errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            progressViewModel.onErrorConsumed()
        }
    }

    if (uiState.isLoading && uiState.progressData == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
             CircularProgressIndicator()
         }
    } else if (uiState.progressData != null) {
        ProgressScreen(
            username = uiState.progressData.username,
            points = uiState.progressData.points,
            level = uiState.progressData.level,
            progress = uiState.progressData.progressToNextLevel,
            isAdAvailable = uiState.progressData.isAdAvailable,
            isWithdrawEnabled = uiState.progressData.isWithdrawEnabled,
            isLoading = uiState.actionInProgress, // Use action in progress for button states
            onWatchAdClick = progressViewModel::watchAd,
            onWithdrawClick = progressViewModel::withdraw
        )
    } else {
         Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error loading progress.")
         }
    }
}

@Composable
fun UserProfileScreenContent() {
    val context = LocalContext.current
    val userProfileViewModel: UserProfileViewModel = hiltViewModel()
    val uiState by userProfileViewModel.uiState.collectAsState()

    // Handle purchase result message
    LaunchedEffect(uiState.premiumPurchaseResult) {
        when (uiState.premiumPurchaseResult) {
            true -> Toast.makeText(context, "Premium Activated!", Toast.LENGTH_SHORT).show()
            false -> Toast.makeText(context, "Premium Purchase Failed", Toast.LENGTH_SHORT).show()
            null -> { /* Do nothing */ }
        }
        if (uiState.premiumPurchaseResult != null) {
            userProfileViewModel.onPremiumPurchaseResultConsumed()
        }
    }
    // Handle general errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            userProfileViewModel.onErrorConsumed()
        }
    }

    // Show loading or content based on state
    if (uiState.isLoading && uiState.user == null) {
         Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
             CircularProgressIndicator()
         }
    } else if (uiState.user != null) {
        UserProfileScreen(
            username = uiState.user.username,
            points = uiState.user.points,
            level = uiState.user.level,
            isPremium = uiState.user.premiumStatus,
            onPurchasePremiumClick = userProfileViewModel::purchasePremium
        )
    } else {
         Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error loading profile.")
         }
    }
}

@Composable
fun CompanyProfileScreenContent() {
    val context = LocalContext.current
    val companyProfileViewModel: CompanyProfileViewModel = hiltViewModel()
    val uiState by companyProfileViewModel.uiState.collectAsState()

    // Handle error display
     LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            companyProfileViewModel.onErrorConsumed()
        }
    }

     if (uiState.isLoading && uiState.company == null) {
         Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
             CircularProgressIndicator()
         }
     } else if (uiState.company != null) {
        CompanyProfileScreen(
            companyName = uiState.company.name,
            companyType = uiState.company.type,
            companyPoints = uiState.company.points,
            employees = uiState.employees, // Pass the EmployeeInfo list directly
            onAddPhotoClick = companyProfileViewModel::addPhoto,
            onSetHoursClick = companyProfileViewModel::setHours,
            onAddPriceListClick = companyProfileViewModel::addPriceList,
            onEmployeeClick = { employee ->
                companyProfileViewModel.onEmployeeSelected(employee)
            }
        )
     } else {
         Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error loading company profile.")
         }
     }
} 