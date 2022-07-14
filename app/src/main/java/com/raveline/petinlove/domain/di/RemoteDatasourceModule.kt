package com.raveline.petinlove.domain.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RemoteDatasourceModule {

    @Provides
    @Singleton
    fun provideAuthenticationModule(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun providesDatabaseReferenceModule(): FirebaseFirestore = FirebaseFirestore.getInstance()
}