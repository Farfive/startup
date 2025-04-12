package com.example.styleap.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.styleap.R
import com.example.styleap.data.AuthRepository
import com.example.styleap.databinding.ActivityMainBinding
import com.example.styleap.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var authRepository: AuthRepository // Inject repository to check auth state

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Authentication Check --- START
        if (authRepository.getCurrentUser() == null) {
            // Not logged in, redirect to LoginActivity
            navigateToLogin()
            return // Important: return to prevent rest of onCreate execution for this instance
        }
        // --- Authentication Check --- END

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupNavigation()
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        
        // Connect bottom navigation with navigation controller
        binding.bottomNavigationView.setupWithNavController(navController)

        // Handle custom profile navigation *after* setting up with NavController
        // Get user type from intent or shared preferences
        val userType = intent.getStringExtra("USER_TYPE") ?: "Individual"

        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_profile -> {
                    // Navigate to appropriate profile based on user type
                    when (userType) {
                        "Company" -> navController.navigate(R.id.companyProfileFragment)
                        else -> navController.navigate(R.id.userProfileFragment)
                    }
                    true // Consume the event
                }
                R.id.navigation_progress -> {
                    navController.navigate(R.id.progressFragment)
                    true // Consume the event
                }
                else -> {
                    // Fall back to default behavior
                    false
                }
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Finish MainActivity so user cannot press back to get here
    }

    // Inflate options menu for logout
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    // Handle menu item selection (logout)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                authRepository.logoutUser() // Call the logout method from AuthRepository
                navigateToLogin()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
