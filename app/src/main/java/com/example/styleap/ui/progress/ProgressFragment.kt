package com.example.styleap.ui.progress

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.styleap.R
import com.example.styleap.databinding.FragmentProgressBinding
import com.example.styleap.util.AdManager
import com.example.styleap.util.Resource
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ProgressFragment : Fragment() {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProgressViewModel by viewModels()
    
    @Inject
    lateinit var adManager: AdManager

    // Jobs for cancellable coroutines
    private var progressSimulationJob: Job? = null
    private var navigationJob: Job? = null

    // Define constants for magic numbers
    companion object {
        private const val AD_INTERVAL_MS = 30000L // 30 seconds
        private const val NAVIGATION_DELAY_MS = 300000L // 5 minutes
        private const val PROGRESS_INCREMENT = 16
        private const val POINTS_INCREMENT = 10
        private const val ANIMATION_DURATION = 300L
        private const val REWARDED_AD_POINTS = 50 // Points earned for watching a rewarded ad
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
        setupProgressBar()
        setupAdButton()
        startSimulationAndNavigation()
    }

    private fun setupProgressBar() {
        binding.progressBar.setIndicatorColor(
            ContextCompat.getColor(requireContext(), R.color.progress_color)
        )
    }

    private fun setupAdButton() {
        binding.buttonWatchAd.setOnClickListener {
            showRewardedAd()
        }
    }

    private fun showRewardedAd() {
        activity?.let { activity ->
            adManager.showRewardedAd(
                activity = activity,
                onRewardedAdLoaded = {
                    // Ad is ready to be shown
                    binding.buttonWatchAd.isEnabled = true
                },
                onRewardedAdClosed = {
                    // Ad was closed, reload for next time
                    binding.buttonWatchAd.isEnabled = false
                    adManager.initialize(requireContext())
                },
                onUserEarnedReward = {
                    // User completed watching the ad
                    viewModel.addPointsFromAd(REWARDED_AD_POINTS)
                    showSuccessMessage(getString(R.string.ad_reward_success_message))
                }
            )
        }
    }

    private fun setupObservers() {
        // Observe User Points
        viewModel.userPoints.observe(viewLifecycleOwner, Observer { points ->
            binding.textViewPoints.text = getString(R.string.progress_points_format, points)
            // Update withdraw button state based on points
            binding.buttonWithdraw.isEnabled = viewModel.canWithdraw()
            // Animate points update
            animatePointsUpdate(points)
        })

        // Observe User Level
        viewModel.userLevel.observe(viewLifecycleOwner, Observer { level ->
            binding.textViewUserLevel.text = getString(R.string.progress_level_format, level)
            // Animate level update
            animateLevelUpdate(level)
        })

        // Observe Progress Value
        viewModel.progressValue.observe(viewLifecycleOwner, Observer { progress ->
            binding.progressBar.progress = progress
            // Animate progress update
            animateProgressUpdate(progress)
        })

        // Observe Withdrawal State
        viewModel.withdrawalState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.buttonWithdraw.isEnabled = false
                    binding.loadingIndicator.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.buttonWithdraw.isEnabled = true
                    binding.loadingIndicator.visibility = View.GONE
                    showSuccessMessage(getString(R.string.withdraw_success_message))
                }
                is Resource.Error -> {
                    binding.buttonWithdraw.isEnabled = true
                    binding.loadingIndicator.visibility = View.GONE
                    showErrorMessage(state.message ?: getString(R.string.withdraw_error_generic))
                    viewModel.resetWithdrawalState()
                }
                else -> {
                    binding.buttonWithdraw.isEnabled = viewModel.canWithdraw()
                    binding.loadingIndicator.visibility = View.GONE
                }
            }
        })
        
        // Observe overall UI State from ViewModel (for initial load)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when(state) {
                    is ProgressUiState.Loading -> {
                        binding.loadingIndicator.visibility = View.VISIBLE
                    }
                    is ProgressUiState.Error -> {
                        binding.loadingIndicator.visibility = View.GONE
                        showErrorMessage(state.message)
                    }
                    is ProgressUiState.Success -> {
                         binding.loadingIndicator.visibility = View.GONE
                    }
                    is ProgressUiState.Initial -> { /* Do nothing initially */ }
                }
            }
        }

        // Observe Ad Button State
        viewModel.isAdButtonEnabled.observe(viewLifecycleOwner, Observer { isEnabled ->
            binding.buttonWatchAd.isEnabled = isEnabled
        })
    }

    private fun animatePointsUpdate(points: Int) {
        val scaleX = ObjectAnimator.ofFloat(binding.textViewPoints, "scaleX", 1f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.textViewPoints, "scaleY", 1f, 1.2f, 1f)
        
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = ANIMATION_DURATION
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun animateLevelUpdate(level: Int) {
        val scaleX = ObjectAnimator.ofFloat(binding.textViewUserLevel, "scaleX", 1f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.textViewUserLevel, "scaleY", 1f, 1.2f, 1f)
        
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = ANIMATION_DURATION
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun animateProgressUpdate(progress: Int) {
        val progressAnimator = ObjectAnimator.ofInt(binding.progressBar, "progress", progress)
        progressAnimator.duration = ANIMATION_DURATION
        progressAnimator.interpolator = AccelerateDecelerateInterpolator()
        progressAnimator.start()
    }

    private fun showSuccessMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showErrorMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .show()
    }

    private fun setupClickListeners() {
        binding.buttonWithdraw.setOnClickListener {
            viewModel.attemptWithdrawal()
        }
    }

    private fun startSimulationAndNavigation() {
        // Cancel any existing jobs first
        progressSimulationJob?.cancel()
        navigationJob?.cancel()

        // Start new jobs using the viewLifecycleOwner.lifecycleScope
        progressSimulationJob = viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                delay(AD_INTERVAL_MS)
                Timber.d("Simulating Ad Display")

                // Update progress and points via ViewModel
                viewModel.incrementProgress(PROGRESS_INCREMENT)
                viewModel.incrementPoints(POINTS_INCREMENT)
            }
        }

        // Navigation based on progress or user action
        navigationJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(NAVIGATION_DELAY_MS)
            if (isAdded) {
                try {
                    // Use the proper action to navigate to user profile
                    findNavController().navigate(R.id.action_progressFragment_to_userProfileFragment)
                } catch (e: IllegalStateException) {
                    Timber.e(e, "Navigation failed")
                } catch (e: IllegalArgumentException) {
                    Timber.e(e, "Navigation action not found")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Cancel coroutines when the view is destroyed
        progressSimulationJob?.cancel()
        navigationJob?.cancel()
        _binding = null
    }
}
