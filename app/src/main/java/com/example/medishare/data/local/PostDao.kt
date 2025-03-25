package com.example.medishare.data.local

import androidx.paging.PagingSource
import androidx.room.*

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPosts(): PagingSource<Int, PostEntity>

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserPosts(userId: String): PagingSource<Int, PostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Query("DELETE FROM posts")
    suspend fun clearAllPosts()

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: String)

//    @Transaction
//    suspend fun refreshPosts(posts: List<PostEntity>) {
//        insertPosts(posts)
//    }
}
