package com.example.styleap.ui.registration

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.styleap.R
import com.example.styleap.data.RegistrationParams
import com.example.styleap.data.UserType
import com.example.styleap.databinding.ActivityRegistrationBinding
import com.example.styleap.ui.login.LoginActivity
import com.example.styleap.ui.main.MainActivity
import com.example.styleap.util.Resource
import dagger.hilt.android.AndroidEntryPoint

/**
 * RegistrationActivity handles user registration flow including:
 * - User type selection (Individual or Company)
 * - Company-specific information for company accounts
 * - Creating user accounts in Firebase Authentication
 * - Storing user profile data in Firestore
 */

@AndroidEntryPoint
class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private val viewModel: RegistrationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.registerButton.setOnClickListener {
            handleRegistrationAttempt()
        }

        binding.loginLink.setOnClickListener {
            // Navigate back to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Finish this activity so user can't press back to get here
        }
    }

    private fun observeViewModel() {
        viewModel.registrationState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.registerButton.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.registerButton.isEnabled = true
                    Toast.makeText(this, "Registration Successful!", Toast.LENGTH_LONG).show()
                    // Navigate to MainActivity or LoginActivity (recommend login first)
                    // Firebase Auth usually automatically signs the user in after registration via Cloud Func
                    // If so, navigate to Main. Otherwise, navigate to Login.
                    // For now, let's assume auto-login and go to Main
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity() // Finish this and previous activities (like Login if coming from there)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.registerButton.isEnabled = true
                    Toast.makeText(this, "Registration Failed: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun handleRegistrationAttempt() {
        val name = binding.nameInput.text.toString().trim()
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString()
        val selectedUserTypeId = binding.userTypeRadioGroup.checkedRadioButtonId

        // --- Basic Client-Side Validation ---
        binding.nameLayout.error = null
        binding.emailLayout.error = null
        binding.passwordLayout.error = null

        if (name.isEmpty()) {
            binding.nameLayout.error = "Name cannot be empty"
            return
        }
        if (email.isEmpty()) {
            binding.emailLayout.error = "Email cannot be empty"
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "Invalid email format"
            return
        }
        if (password.isEmpty()) {
            binding.passwordLayout.error = "Password cannot be empty"
            return
        }
        if (password.length < 6) {
            binding.passwordLayout.error = "Password must be at least 6 characters"
            return
        }
        if (selectedUserTypeId == -1) {
            Toast.makeText(this, "Please select a user type", Toast.LENGTH_SHORT).show()
            return
        }

        val userType = when (selectedUserTypeId) {
            R.id.companyAdminRadioButton -> UserType.COMPANY_ADMIN
            else -> UserType.INDIVIDUAL // Default to Individual
        }

        val params = RegistrationParams(name, email, password, userType)
        viewModel.attemptRegistration(params)
    }
}