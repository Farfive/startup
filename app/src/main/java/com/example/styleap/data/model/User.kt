package com.example.styleap.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

// User data class for Firestore
@IgnoreExtraProperties // Good practice for Firestore data classes
data class User(
    @DocumentId // Map Firestore document ID to this field
    var uid: String = "", // Renamed from id, matches FirebaseAuth UID
    var username: String = "",
    var email: String = "", // Often useful to store email
    var points: Int = 0,
    var level: Int = 0,
    var premiumStatus: Boolean = false, // Changed from isPremium for consistency
    var userType: UserType = UserType.CUSTOMER // Add userType with a default
) {
    // Add a no-argument constructor for Firestore deserialization
    constructor() : this("", "", "", 0, 0, false, UserType.CUSTOMER)
}

// Enum for UserType (assuming it's defined elsewhere or create here)
enum class UserType {
    CUSTOMER, STYLIST, ADMIN // Example types
} 