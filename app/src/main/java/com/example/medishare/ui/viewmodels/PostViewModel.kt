package com.example.medishare.ui.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.medishare.data.PostRepository
import com.example.medishare.data.local.PostEntity
import com.example.medishare.models.Post
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

private const val TAG = "PostViewModel"

sealed class PostsState {
    object Initial : PostsState()
    object Loading : PostsState()
    data class Success(val posts: List<Post>) : PostsState()
    data class Error(val message: String) : PostsState()
}

class PostViewModel(context: Context) : ViewModel() {
    private val postContext = context.applicationContext

    private val repository = PostRepository(postContext)

    private val _refreshState = MutableStateFlow<PostsState>(PostsState.Initial)
    val refreshState: StateFlow<PostsState> = _refreshState

    val posts: Flow<PagingData<PostEntity>> = repository.getAllPosts()
        .cachedIn(viewModelScope)

    fun getUserPosts(userId: String): Flow<PagingData<PostEntity>> =
        repository.getUserPosts(userId).cachedIn(viewModelScope)

    fun refreshPosts() {
        viewModelScope.launch {
            _refreshState.value = PostsState.Loading
            try {
                repository.refreshPosts()
                _refreshState.value = PostsState.Success(emptyList()) // Empty list since we're using paging
            } catch (e: Exception) {
                _refreshState.value = PostsState.Error(e.message ?: "Failed to refresh posts")
            }
        }
    }

    fun createPost(title: String, description: String, imageUri: Uri?, location: GeoPoint? = null) {
        viewModelScope.launch {
            try {
                val fileName = imageUri?.lastPathSegment?.substringAfterLast('/')
                val imageUrl = imageUri?.let { uri ->
                    val imageBytes = compressImage(uri)
                    repository.uploadImage(imageBytes)
                }
                repository.createPost(title, description, imageUrl, fileName, location)
                refreshPosts() // Refresh the posts after creating a new one
            } catch (e: Exception) {
                Log.e(TAG, "Error creating post", e)
                throw e
            }
        }
    }

    private fun compressImage(uri: Uri): ByteArray {
        val inputStream = postContext.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return outputStream.toByteArray()
    }

    fun updatePost(post: Post) {
        viewModelScope.launch {
            try {
                repository.updatePost(post)
                refreshPosts()
            } catch (e: Exception) {
                _refreshState.value = PostsState.Error(e.message ?: "Failed to update post")
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                repository.deletePost(postId)
                refreshPosts()
            } catch (e: Exception) {
                _refreshState.value = PostsState.Error(e.message ?: "Failed to delete post")
            }
        }
    }
}
