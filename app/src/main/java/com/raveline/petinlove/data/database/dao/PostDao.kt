package com.raveline.petinlove.data.database.dao

import androidx.room.*
import com.raveline.petinlove.data.model.PostModel
import kotlinx.coroutines.flow.Flow

@Dao
sealed interface PostDao{
    @Query("SELECT * FROM POST_TABLE ORDER BY dateCreated DESC")
    fun getLocalPosts():Flow<List<PostModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocalPost(post: PostModel)

    @Delete
    suspend fun deleteLocalPost(post: PostModel)

    @Delete
    suspend fun deleteLocalPosts(posts: List<PostModel>)

}