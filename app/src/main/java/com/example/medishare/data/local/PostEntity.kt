package com.example.medishare.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.medishare.models.Post
import java.util.Date

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val fileName: String? = null,
    val timestamp: Date,
    val latitude: Double?,
    val longitude: Double?
) {
    fun toPost(): Post = Post(
        id = id,
        userId = userId,
        title = title,
        description = description,
        imageUrl = imageUrl,
        timestamp = timestamp,
        fileName = fileName?: "",
    )

    companion object {
        fun fromPost(post: Post): PostEntity = PostEntity(
            id = post.id,
            userId = post.userId,
            title = post.title,
            description = post.description,
            imageUrl = post.imageUrl,
            fileName = post.fileName,
            timestamp = post.timestamp,
            latitude = null,  // TODO: Add location support
            longitude = null
        )
    }
}
