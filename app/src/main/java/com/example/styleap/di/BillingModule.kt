package com.example.styleap.di

import com.example.styleap.data.UserRepository
import com.example.styleap.util.BillingClientWrapper
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {
    
    @Provides
    @Singleton
    fun provideBillingClientWrapper(
        userRepository: UserRepository,
        auth: FirebaseAuth
    ): BillingClientWrapper {
        return BillingClientWrapper(userRepository, auth)
    }
} 