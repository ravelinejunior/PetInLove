package com.raveline.petinlove.domain.di

import android.content.Context
import android.content.SharedPreferences
import com.raveline.petinlove.domain.utils.GLOBAL_SHARED_PREFS_KEY
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun providesSharedPref(@ApplicationContext context: Context):SharedPreferences =
        context.getSharedPreferences(GLOBAL_SHARED_PREFS_KEY,Context.MODE_PRIVATE)
}