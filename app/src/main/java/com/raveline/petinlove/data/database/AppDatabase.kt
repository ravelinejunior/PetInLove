package com.raveline.petinlove.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.raveline.petinlove.data.database.converter.Converter
import com.raveline.petinlove.data.database.dao.CommentDao
import com.raveline.petinlove.data.database.dao.PostDao
import com.raveline.petinlove.data.database.dao.UserDao
import com.raveline.petinlove.data.model.CommentModel
import com.raveline.petinlove.data.model.PostModel
import com.raveline.petinlove.data.model.UserModel

@Database(
    entities = [CommentModel::class,PostModel::class,UserModel::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converter::class)
abstract class AppDatabase:RoomDatabase() {
    abstract fun commentDao():CommentDao
    abstract fun postDao():PostDao
    abstract fun userDao():UserDao
}