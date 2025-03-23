package com.example.medishare.data

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

class PostRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getAllPosts(): Flow<List<Post>> = flow {
        val snapshot = firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()
        emit(snapshot.documents.mapNotNull { it.toObject(Post::class.java) })
    }

    fun getUserPosts(userId: String): Flow<List<Post>> = flow {
        val snapshot = firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()
        emit(snapshot.documents.mapNotNull { it.toObject(Post::class.java) })
    }

    suspend fun createPost(title: String, description: String, imageUrl: String?, location: GeoPoint? = null): Post {
        val currentUser = auth.currentUser ?: throw IllegalStateException("User not logged in")
        val postId = UUID.randomUUID().toString()
        
        val postData = hashMapOf(
            "userId" to currentUser.uid,
            "title" to title,
            "description" to description,
            "imageUrl" to (imageUrl ?: ""),
            "timestamp" to com.google.firebase.Timestamp.now()
        )
        
        try {
            firestore.collection("posts").document(postId).set(postData).await()
        } catch (e: Exception) {
            // If post creation fails and we have an image URL, try to delete the image
            if (!imageUrl.isNullOrEmpty()) {
                try {
                    val imageRef = storage.getReferenceFromUrl(imageUrl)
                    imageRef.delete().await()
                } catch (deleteError: Exception) {
                    // Ignore delete errors
                }
            }
            throw e
        }
        
        return Post(
            id = postId,
            userId = currentUser.uid,
            title = title,
            description = description,
            imageUrl = imageUrl ?: ""
        )
    }

    suspend fun updatePost(post: Post) {
        firestore.collection("posts").document(post.id).set(post).await()
    }

    suspend fun deletePost(postId: String) {
        firestore.collection("posts").document(postId).delete().await()
    }

    suspend fun uploadImage(imageBytes: ByteArray): String {
        val fileName = "post_images/${UUID.randomUUID()}.jpg"
        val imageRef = storage.reference.child(fileName)
        
        return try {
            // Upload the image
            imageRef.putBytes(imageBytes).await()
            
            // Get the download URL
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            // If upload fails, try to delete the file if it was created
            try {
                imageRef.delete().await()
            } catch (deleteError: Exception) {
                // Ignore delete errors
            }
            throw e
        }
    }
}
