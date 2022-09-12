package com.raveline.petinlove.data.database.dao

import androidx.room.*
import com.raveline.petinlove.data.model.CommentModel
import kotlinx.coroutines.flow.Flow

@Dao
sealed interface CommentDao {
    @Query("SELECT * FROM COMMENT_TABLE WHERE idPost == :postId")
    fun getLocalCommentsById(postId: String): Flow<List<CommentModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocalComment(comment: CommentModel)

    @Delete
    suspend fun deleteLocalComment(comment: CommentModel)

    @Delete
    suspend fun deleteLocalComments(comments: List<CommentModel>)
}