package com.example.styleap.di

import com.example.styleap.util.AdManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdModule {
    
    @Provides
    @Singleton
    fun provideAdManager(): AdManager {
        return AdManager()
    }
} 