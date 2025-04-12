package com.example.styleap.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.styleap.databinding.ActivityLoginBinding
import com.example.styleap.ui.registration.RegistrationActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private val TAG = "LoginActivity"
    
    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        checkCurrentUser()
    }
    
    private fun checkCurrentUser() {
        // Check if user is already signed in
        firebaseAuth.currentUser?.let {
            // User is already signed in, navigate to main flow
            navigateToRegistration()
        }
    }
    
    private fun setupUI() {
        // Set content descriptions for accessibility
        binding.editTextEmail.contentDescription = "Email input field"
        binding.editTextPassword.contentDescription = "Password input field"
        binding.buttonLogin.contentDescription = "Login button"
        
        binding.buttonLogin.setOnClickListener {
            if (validateInputs()) {
                performLogin()
            }
        }
        
        binding.textViewSignUp.setOnClickListener {
            navigateToRegistration()
        }
    }
    
    private fun validateInputs(): Boolean {
        var isValid = true
        
        // Validate email
        val email = binding.editTextEmail.text.toString().trim()
        if (email.isEmpty()) {
            binding.textInputLayoutEmail.error = "Email cannot be empty"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textInputLayoutEmail.error = "Please enter a valid email address"
            isValid = false
        } else {
            binding.textInputLayoutEmail.error = null
        }
        
        // Validate password
        val password = binding.editTextPassword.text.toString()
        if (password.isEmpty()) {
            binding.textInputLayoutPassword.error = "Password cannot be empty"
            isValid = false
        } else if (password.length < 6) {
            binding.textInputLayoutPassword.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            binding.textInputLayoutPassword.error = null
        }
        
        return isValid
    }
    
    private fun performLogin() {
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString()
        
        // Show loading indicator
        binding.buttonLogin.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        // Authenticate with Firebase
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                // Hide loading indicator
                binding.buttonLogin.isEnabled = true
                binding.progressBar.visibility = android.view.View.GONE
                
                if (task.isSuccessful) {
                    // Sign in success
                    Log.d(TAG, "Login successful with email: $email")
                    navigateToRegistration()
                } else {
                    // If sign in fails, display a message to the user
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
    
    private fun navigateToRegistration() {
        val intent = Intent(this, RegistrationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }
}