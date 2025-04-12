package com.example.dream.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Provide Firestore instance for the whole app
object FirebaseModule {

    @Provides
    @Singleton // Ensure only one instance of Firestore is created
    fun provideFirestoreInstance(): FirebaseFirestore {
        return Firebase.firestore
        // Optional: Add settings like persistence, cache size here if needed
        // val settings = firestoreSettings { isPersistenceEnabled = true }
        // firestore.firestoreSettings = settings
    }

    // Add this provider for FirebaseAuth
    @Provides
    @Singleton
    fun provideFirebaseAuthInstance(): FirebaseAuth {
        return Firebase.auth
    }
} 