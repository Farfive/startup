package com.example.dream.utils

object Constants {
    // Rewards
    const val DAILY_LOGIN_REWARD = 12 // Coins awarded for daily login
    const val WEEKLY_LOGIN_REWARD = 60 // Additional coins for logging in 7 consecutive days
    const val WATCH_AD_REWARD = 1 // Coins awarded for watching an ad
    const val REFERRAL_REWARD = 100 // Coins awarded for a successful referral (NEW/Adjusted)

    // Redemption
    const val REDEEM_THRESHOLD = 1000 // Minimum coins needed to redeem
    const val REDEEM_VALUE_USD = 4.5 // USD value for redeeming the threshold amount

    // Ad Revenue (Example - Replace with your actual expected value)
    const val AD_REVENUE_PER_VIEW = 0.01 // Estimated USD revenue per ad view

    // Firestore Collections
    const val USERS_COLLECTION = "users"
    const val TRANSACTIONS_COLLECTION = "transactions"
    const val REFERRALS_COLLECTION = "referrals" // (Potentially New) Collection to track successful referrals

    // Transaction Types
    const val TRANSACTION_TYPE_LOGIN = "LOGIN"
    const val TRANSACTION_TYPE_AD_WATCH = "AD_WATCH"
    const val TRANSACTION_TYPE_REFERRAL = "REFERRAL" // (Potentially New or ensure it exists)
    const val TRANSACTION_TYPE_REDEEM = "REDEEM"

    // User Fields (For clarity - these would be fields within user documents in Firestore)
    const val FIELD_REFERRAL_CODE = "referralCode"
    const val FIELD_REFERRED_BY = "referredBy" // UID of the user who referred this user
} 