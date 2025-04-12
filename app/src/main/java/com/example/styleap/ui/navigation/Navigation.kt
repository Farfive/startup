package com.example.styleap.ui.navigation

import android.widget.Toast // For showing errors/messages temporarily
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.styleap.ui.compose.* // Import your screen composables
import com.example.styleap.ui.compose.MainDashboardScreen // Import the new dashboard screen
import com.example.styleap.ui.viewmodel.* // Import ViewModels
import com.example.styleap.data.model.EmployeeInfo // Should be using this now
import com.google.firebase.auth.FirebaseAuth // Import FirebaseAuth

// Define navigation routes
object AppDestinations {
    const val LOGIN_ROUTE = "login"
    const val REGISTRATION_ROUTE = "registration"
    const val MAIN_DASHBOARD_ROUTE = "mainDashboard" // Host for bottom nav

    // Routes within the Main Dashboard (Bottom Navigation)
    const val DASHBOARD_HOME_ROUTE = "home" // Optional: A dedicated home/feed
    const val DASHBOARD_PROGRESS_ROUTE = "progress"
    const val DASHBOARD_USER_PROFILE_ROUTE = "userProfile"
    const val DASHBOARD_COMPANY_PROFILE_ROUTE = "companyProfile"

    // Keep original routes if they can be navigated to directly from outside the dashboard
    // (Though typically profile/progress would only be inside the dashboard)
    const val USER_PROFILE_ROUTE = "userProfileStandalone"
    const val COMPANY_PROFILE_ROUTE = "companyProfileStandalone"
    const val PROGRESS_ROUTE = "progressStandalone"
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    // Determine start destination based on current auth state
    val startDestination = remember {
        if (FirebaseAuth.getInstance().currentUser != null) {
            AppDestinations.MAIN_DASHBOARD_ROUTE
        } else {
            AppDestinations.LOGIN_ROUTE
        }
    }

    val context = LocalContext.current // For showing Toasts

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(AppDestinations.LOGIN_ROUTE) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            val uiState by loginViewModel.uiState.collectAsState()

            // Handle navigation trigger
            LaunchedEffect(uiState.loginSuccessEvent) {
                if (uiState.loginSuccessEvent) {
                    navController.navigate(AppDestinations.MAIN_DASHBOARD_ROUTE) {
                        popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                    }
                    loginViewModel.onLoginSuccessEventConsumed() // Reset the event
                }
            }

            // Handle error display (e.g., Toast)
            LaunchedEffect(uiState.error) {
                uiState.error?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    loginViewModel.onErrorConsumed() // Reset error
                }
            }

            LoginScreen(
                isLoading = uiState.isLoading,
                onLoginClick = loginViewModel::login, // Pass VM function directly
                onRegisterClick = {
                    navController.navigate(AppDestinations.REGISTRATION_ROUTE)
                }
            )
        }

        composable(AppDestinations.REGISTRATION_ROUTE) {
            val registrationViewModel: RegistrationViewModel = hiltViewModel()
            val uiState by registrationViewModel.uiState.collectAsState()

            // Handle navigation trigger
            LaunchedEffect(uiState.registrationSuccessEvent) {
                if (uiState.registrationSuccessEvent) {
                    navController.navigate(AppDestinations.LOGIN_ROUTE) {
                         popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true } // Go back to login
                         launchSingleTop = true
                    }
                    registrationViewModel.onRegistrationSuccessEventConsumed()
                }
            }

             // Handle error display
            LaunchedEffect(uiState.error) {
                uiState.error?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show() // Use Long for reg errors
                    registrationViewModel.onErrorConsumed()
                }
            }

            RegistrationScreen(
                isLoading = uiState.isLoading,
                onRegisterClick = registrationViewModel::register,
                onLoginClick = {
                    navController.popBackStack() // Go back to login
                }
            )
        }

        // Standalone User Profile (if needed, otherwise remove)
        /* composable(AppDestinations.USER_PROFILE_ROUTE) {
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
                    isPremium = uiState.user.isPremium,
                    onPurchasePremiumClick = userProfileViewModel::purchasePremium
                )
            } else {
                 // Handle case where user is null after loading (shouldn't happen ideally)
                 Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error loading profile.")
                 }
            }
        } */

        // Standalone Company Profile (if needed, otherwise remove)
        /* composable(AppDestinations.COMPANY_PROFILE_ROUTE) {
            val companyProfileViewModel: CompanyProfileViewModel = hiltViewModel()
            val uiState by companyProfileViewModel.uiState.collectAsState()

            // Handle error display
             LaunchedEffect(uiState.error) {
                uiState.error?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    companyProfileViewModel.onErrorConsumed()
                }
            }

             if (uiState.isLoading) {
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
                        // Directly pass the EmployeeInfo object
                        companyProfileViewModel.onEmployeeSelected(employee)
                    }
                )
             } else {
                 Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error loading company profile.")
                 }
             }
        } */

        // Standalone Progress Screen (if needed, otherwise remove)
        /* composable(AppDestinations.PROGRESS_ROUTE) {
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

            if (uiState.isLoading) {
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
        } */

        composable(AppDestinations.MAIN_DASHBOARD_ROUTE) {
            val mainDashboardViewModel: MainDashboardViewModel = hiltViewModel()
            // Navigate to the new MainDashboardScreen which handles its own Scaffold and nested NavHost
            MainDashboardScreen(navController = navController) {
                 // Logout Action: Call ViewModel and navigate back to login
                 mainDashboardViewModel.logout()
                 navController.navigate(AppDestinations.LOGIN_ROUTE) {
                     popUpTo(navController.graph.startDestinationId) { inclusive = true }
                     launchSingleTop = true
                 }
             }
        }
    }
} 