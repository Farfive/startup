package com.example.styleap.di

import com.example.styleap.domain.repository.AuthRepository
import com.example.styleap.domain.repository.CompanyRepository
import com.example.styleap.domain.repository.ProgressRepository
import com.example.styleap.domain.repository.UserRepository
import com.example.styleap.data.repository.*
import com.example.styleap.data.repository.FirestoreCompanyRepository
import com.example.styleap.data.repository.FirestoreProgressRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Install in the ApplicationComponent
abstract class RepositoryModule {

    // Use @Binds to tell Hilt which implementation to use for an interface
    @Binds
    @Singleton // Match the scope of the implementation
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: FirestoreUserRepository): UserRepository

    @Binds
    @Singleton
    abstract fun bindCompanyRepository(impl: FirestoreCompanyRepository): CompanyRepository

    @Binds
    @Singleton
    abstract fun bindProgressRepository(impl: FirestoreProgressRepository): ProgressRepository

    // Add bindings for other repositories here
}

// You can also use @Provides for classes you don't own (e.g., Retrofit instance, Room DB)
/*
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit { ... }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService { ... }
}
*/ 