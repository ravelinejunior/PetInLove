package com.raveline.petinlove.data.database.dao

import androidx.room.*
import com.raveline.petinlove.data.model.UserModel
import kotlinx.coroutines.flow.Flow

@Dao
sealed interface UserDao {
    @Query("SELECT * FROM USER_TABLE ORDER BY userName")
    fun getLocalUsers(): Flow<List<UserModel>>

    @Query("SELECT * FROM USER_TABLE WHERE userName LIKE '%' || :search  || '%' ")
    fun getSearchUsers(search: String): Flow<List<UserModel>>

    @Query("SELECT * FROM USER_TABLE WHERE uid == :id")
    fun getCurrentLocalUser(id: String): Flow<UserModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(userModel: UserModel)

    @Query("DELETE FROM USER_TABLE WHERE uid == :id")
    suspend fun deleteCurrentLocalUser(id: String)

    @Query("DELETE FROM USER_TABLE")
    suspend fun deleteUsers()

    @Update
    suspend fun updateUser(user: UserModel)
}