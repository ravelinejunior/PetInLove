package com.raveline.petinlove.domain.di

import android.content.Context
import androidx.room.Room
import com.raveline.petinlove.data.database.AppDatabase
import com.raveline.petinlove.data.database.dao.CommentDao
import com.raveline.petinlove.data.database.dao.PostDao
import com.raveline.petinlove.data.database.dao.UserDao
import com.raveline.petinlove.domain.utils.appLocalDatabaseName
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LocalDatasourceModule {

    @Provides
    @Singleton
    fun providesAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            appLocalDatabaseName
        ).enableMultiInstanceInvalidation()
            .allowMainThreadQueries()
            .fallbackToDestructiveMigrationOnDowngrade()
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun providesCommentDao(database: AppDatabase): CommentDao = database.commentDao()

    @Provides
    @Singleton
    fun providesPostDao(database: AppDatabase): PostDao = database.postDao()

    @Provides
    @Singleton
    fun providesUserDao(database: AppDatabase): UserDao = database.userDao()

}