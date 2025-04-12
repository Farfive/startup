package com.example.styleap.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.styleap.R
import com.example.styleap.databinding.FragmentUserProfileBinding
import com.example.styleap.util.BillingClientWrapper
import com.example.styleap.util.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UserProfileFragment : Fragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: UserProfileViewModel by viewModels()
    
    @Inject
    lateinit var billingClientWrapper: BillingClientWrapper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get arguments if passed from registration or other screens
        arguments?.let { args ->
            args.getString("username")?.let { username ->
                viewModel.setUsername(username)
            }
            args.getInt("points", 0).let { points ->
                viewModel.setPoints(points)
            }
        }
        
        setupObservers()
        setupClickListeners()
        loadUserData()
    }
    
    private fun setupObservers() {
        viewModel.userData.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Show loading state - could add a progress bar in the layout
                    binding.textViewUsername.text = getString(R.string.loading)
                }
                is Resource.Success -> {
                    val user = resource.data
                    binding.textViewUsername.text = user.name
                    
                    // If you want to add points and level data to the User model later,
                    // you can update these fields accordingly
                    binding.textViewPoints.text = getString(R.string.points_placeholder, user.points)
                    binding.textViewUserLevel.text = getString(R.string.level_placeholder, user.level)
                }
                is Resource.Error -> {
                    showErrorMessage(resource.message ?: getString(R.string.withdraw_error_generic))
                }
            }
        })

        // Observe premium status
        lifecycleScope.launch {
            billingClientWrapper.premiumStatus.collect { isPremium ->
                updatePremiumUI(isPremium)
            }
        }
    }

    private fun updatePremiumUI(isPremium: Boolean) {
        if (isPremium) {
            binding.buttonPurchasePremium.isEnabled = false
            binding.buttonPurchasePremium.text = getString(R.string.premium_active)
            showSuccessMessage(getString(R.string.premium_purchase_success))
        } else {
            binding.buttonPurchasePremium.isEnabled = true
            binding.buttonPurchasePremium.text = getString(R.string.purchase_premium_button)
        }
    }
    
    private fun setupClickListeners() {
        binding.buttonPurchasePremium.setOnClickListener {
            activity?.let { activity ->
                billingClientWrapper.launchBillingFlow(activity, "premium_subscription")
            }
        }
    }
    
    private fun loadUserData() {
        // Load user data from repository
        viewModel.loadUserData()
    }

    private fun showSuccessMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showErrorMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
