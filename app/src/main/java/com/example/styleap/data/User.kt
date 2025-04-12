package com.example.styleap.data

import com.google.firebase.firestore.PropertyName

// Add default values for fields expected from Firestore
// Use @PropertyName if your Kotlin property names don't match Firestore field names
data class User(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("email") @set:PropertyName("email") var email: String = "",
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("points") @set:PropertyName("points") var points: Int = 0,
    @get:PropertyName("level") @set:PropertyName("level") var level: Int = 1,
    @get:PropertyName("totalPointsEarned") @set:PropertyName("totalPointsEarned") var totalPointsEarned: Int = 0,
    @get:PropertyName("totalWithdrawals") @set:PropertyName("totalWithdrawals") var totalWithdrawals: Int = 0,
    @get:PropertyName("companyId") @set:PropertyName("companyId") var companyId: String? = null,
    @get:PropertyName("isPremium") @set:PropertyName("isPremium") var isPremium: Boolean = false,
    @get:PropertyName("premiumExpiryDate") @set:PropertyName("premiumExpiryDate") var premiumExpiryDate: Long? = null,
    @get:PropertyName("userType") @set:PropertyName("userType") var userType: UserType? = UserType.Individual
) {
    // Add a no-argument constructor required by Firestore for deserialization
    constructor() : this("", "", "", 0, 1, 0, 0, null, false, null, UserType.Individual)
} 