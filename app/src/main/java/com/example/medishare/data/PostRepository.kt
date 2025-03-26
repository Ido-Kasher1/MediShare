package com.example.medishare.data

import android.content.Context
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.medishare.data.local.AppDatabase
import com.example.medishare.data.local.PostEntity
import com.example.medishare.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

private const val TAG = "PostRepository"
private const val PAGE_SIZE = 20

class PostRepository(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val postDao = AppDatabase.getInstance(context).postDao()

    fun getAllPosts(): Flow<PagingData<PostEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false,
                maxSize = PAGE_SIZE * 3
            )
        ) {
            postDao.getAllPosts()
        }.flow
    }

    fun getUserPosts(userId: String): Flow<PagingData<PostEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false,
                maxSize = PAGE_SIZE * 3
            )
        ) {
            postDao.getUserPosts(userId)
        }.flow
    }

    suspend fun refreshPosts(){
        try {
            val snapshot = firestore.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val posts = snapshot.documents.mapNotNull { it.toObject(Post::class.java) }
            val entities = posts.map { PostEntity.fromPost(it) }
            postDao.insertPosts(entities)
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing posts", e)
            throw e
        }
    }

    suspend fun uploadFile(imageBytes: ByteArray): String {
        // Extract file extension from bytes using magic numbers or default to bin
        val fileExtension = when {
            imageBytes.size >= 2 && imageBytes[0] == 0xFF.toByte() && imageBytes[1] == 0xD8.toByte() -> "jpg"
            imageBytes.size >= 4 && imageBytes[0] == 0x89.toByte() && imageBytes[1] == 0x50.toByte() -> "png"
            imageBytes.size >= 4 && imageBytes[0] == 0x47.toByte() && imageBytes[1] == 0x49.toByte() -> "gif"
            imageBytes.size >= 4 && imageBytes[0] == 0x25.toByte() && imageBytes[1] == 0x50.toByte() -> "pdf"
            imageBytes.size >= 4 && imageBytes.slice(0..3).toByteArray().contentEquals("PK\u0003\u0004".toByteArray()) -> "zip"
            else -> "bin"
        }
        
        val fileName = "${UUID.randomUUID()}.$fileExtension"
        val storageRef = storage.reference.child("images/$fileName")

        return try {
            storageRef.putBytes(imageBytes).await()
            val downloadUrl = storageRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image", e)
            throw e
        }
    }

    suspend fun createPost(
        title: String, 
        description: String, 
        imageUrl: String?, 
        fileName: String? = null,
        location: GeoPoint? = null
    ): Post {
        Log.d(TAG, "Starting post creation in Firestore")
        val currentUser = auth.currentUser ?: throw IllegalStateException("User not logged in")
        val postId = UUID.randomUUID().toString()
        
        // Get the actual file name from the URL if it exists
        val actualFileName = if (imageUrl != null) {
            imageUrl.substringAfterLast("/").substringBefore("?")
        } else {
            fileName ?: ""
        }
        
        val postData = hashMapOf(
            "userId" to currentUser.uid,
            "title" to title,
            "description" to description,
            "imageUrl" to (imageUrl ?: ""),
            "fileName" to actualFileName,
            "timestamp" to com.google.firebase.Timestamp.now(),
            "location" to location
        )

        return try {
            firestore.collection("posts").document(postId)
                .set(postData)
                .await()
            
            val post = Post(
                id = postId,
                userId = currentUser.uid,
                title = title,
                description = description,
                imageUrl = imageUrl ?: "",
                fileName = actualFileName
            )

            postDao.insertPosts(listOf(PostEntity.fromPost(post)))
            
            Log.d(TAG, "Successfully created post in Firestore and local cache")
            post
        } catch (e: Exception) {
            Log.e(TAG, "Error creating post", e)
            throw e
        }
    }

    suspend fun updatePost(post: Post) {
        try {
            val postData = hashMapOf(
                "userId" to post.userId,
                "title" to post.title,
                "description" to post.description,
                "imageUrl" to post.imageUrl,
            )
            firestore.collection("posts").document(post.id)
                .update(postData as Map<String, Any>)
                .await()
            
            // Update local cache
            postDao.insertPosts(listOf(PostEntity.fromPost(post)))
            
            Log.d(TAG, "Successfully updated post in Firestore and local cache")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating post", e)
            throw e
        }
    }

    suspend fun deletePost(post: Post) {
        try {
            Log.d(TAG, "Trying to delete $post from Firestore and local cache")
            // Get post before deletion to get the file URL
            if (post.imageUrl.isNullOrBlank()) {
                try {
                    val ref = storage.getReferenceFromUrl(post.imageUrl)
                    ref.delete().await()
                    Log.d(TAG, "Successfully deleted file from storage")
                } catch (e: Exception) {
                    Log.e(TAG, "Error deleting file from storage", e)
                }
            }

            firestore.collection("posts").document(post.id).delete().await()

            postDao.deletePost(post.id)
            Log.d(TAG, "Successfully deleted post from Firestore and local cache")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting post", e)
            throw e
        }
    }
}
