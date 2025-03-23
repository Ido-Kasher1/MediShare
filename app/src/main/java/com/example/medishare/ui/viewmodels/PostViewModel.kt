package com.example.medishare.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medishare.data.PostRepository
import com.example.medishare.models.Post
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostViewModel : ViewModel() {
    private val repository = PostRepository()

    private val _postsState = MutableStateFlow<PostsState>(PostsState.Loading)
    val postsState: StateFlow<PostsState> = _postsState

    private val _userPostsState = MutableStateFlow<PostsState>(PostsState.Loading)
    val userPostsState: StateFlow<PostsState> = _userPostsState

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            try {
                repository.getAllPosts().collect { posts ->
                    _postsState.value = PostsState.Success(posts)
                }
            } catch (e: Exception) {
                _postsState.value = PostsState.Error(e.message ?: "Failed to load posts")
            }
        }
    }

    fun loadUserPosts(userId: String) {
        viewModelScope.launch {
            try {
                repository.getUserPosts(userId).collect { posts ->
                    _userPostsState.value = PostsState.Success(posts)
                }
            } catch (e: Exception) {
                _userPostsState.value = PostsState.Error(e.message ?: "Failed to load user posts")
            }
        }
    }

    fun createPost(title: String, description: String, imageUri: Uri?, location: GeoPoint? = null) {
        viewModelScope.launch {
            try {
                _postsState.value = PostsState.Loading
                val imageUrl = imageUri?.let { uri ->
                    // Convert Uri to ByteArray and upload
                    // This is a placeholder - you'll need to implement the actual Uri to ByteArray conversion
                    repository.uploadImage(ByteArray(0))
                }
                repository.createPost(title, description, imageUrl, location)
                loadPosts() // Reload posts after creation
            } catch (e: Exception) {
                _postsState.value = PostsState.Error(e.message ?: "Failed to create post")
            }
        }
    }

    fun updatePost(post: Post) {
        viewModelScope.launch {
            try {
                repository.updatePost(post)
                loadPosts() // Reload posts after update
            } catch (e: Exception) {
                _postsState.value = PostsState.Error(e.message ?: "Failed to update post")
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                repository.deletePost(postId)
                loadPosts() // Reload posts after deletion
            } catch (e: Exception) {
                _postsState.value = PostsState.Error(e.message ?: "Failed to delete post")
            }
        }
    }
}

sealed class PostsState {
    object Loading : PostsState()
    data class Success(val posts: List<Post>) : PostsState()
    data class Error(val message: String) : PostsState()
}
