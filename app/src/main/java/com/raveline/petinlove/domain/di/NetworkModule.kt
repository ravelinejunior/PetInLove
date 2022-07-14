package com.raveline.petinlove.domain.di

import com.raveline.petinlove.data.listener.NetworkListeners
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun provideNetworkListener(): NetworkListeners = NetworkListeners()
}