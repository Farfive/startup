package com.example.styleap.util

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor() {
    private var rewardedAd: RewardedAd? = null
    private var onRewardedAdLoadedCallback: (() -> Unit)? = null
    private var onRewardedAdClosedCallback: (() -> Unit)? = null
    private var onUserEarnedRewardCallback: (() -> Unit)? = null

    fun initialize(context: Context) {
        MobileAds.initialize(context)
        loadRewardedAd(context)
    }

    private fun loadRewardedAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        
        RewardedAd.load(
            context,
            "ca-app-pub-3940256099942544/5224354917", // Test ad unit ID
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Timber.d("Rewarded ad loaded successfully")
                    rewardedAd = ad
                    setupRewardedAdCallbacks()
                    onRewardedAdLoadedCallback?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Timber.e(loadAdError, "Failed to load rewarded ad")
                    rewardedAd = null
                }
            }
        )
    }

    private fun setupRewardedAdCallbacks() {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Timber.d("Rewarded ad dismissed")
                onRewardedAdClosedCallback?.invoke()
                // Reload the ad for next time
                rewardedAd = null
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Timber.e(adError, "Failed to show rewarded ad")
                rewardedAd = null
            }

            override fun onAdShowedFullScreenContent() {
                Timber.d("Rewarded ad showed")
            }
        }
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardedAdLoaded: () -> Unit = {},
        onRewardedAdClosed: () -> Unit = {},
        onUserEarnedReward: () -> Unit = {}
    ) {
        onRewardedAdLoadedCallback = onRewardedAdLoaded
        onRewardedAdClosedCallback = onRewardedAdClosed
        onUserEarnedRewardCallback = onUserEarnedReward

        if (rewardedAd != null) {
            rewardedAd?.show(activity) { rewardItem ->
                Timber.d("User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onUserEarnedRewardCallback?.invoke()
            }
        } else {
            Timber.d("Rewarded ad not ready yet")
            onRewardedAdLoadedCallback?.invoke()
        }
    }

    fun isRewardedAdLoaded(): Boolean = rewardedAd != null
} 