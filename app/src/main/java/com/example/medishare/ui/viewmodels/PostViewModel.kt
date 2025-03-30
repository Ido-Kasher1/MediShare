package com.example.medishare.ui.viewmodels

import android.content.Context
import android.graphics.Bitmap
import com.example.medishare.utils.compressImage
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
                Log.d(TAG, "Refreshing posts")
                repository.refreshPosts()
                _refreshState.value = PostsState.Success(emptyList())
            } catch (e: Exception) {
                _refreshState.value = PostsState.Error(e.message ?: "Failed to refresh posts")
            }
        }
    }
    private fun getFileNameFromUri(uri: Uri): String? {
        val returnCursor = postContext.contentResolver.query(uri, null, null, null, null)
        returnCursor?.use {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            return it.getString(nameIndex)
        }
        return uri.lastPathSegment?.substringAfterLast('/')
    }

    fun createPost(title: String, description: String, fileUri: Uri?, location: GeoPoint? = null) {
        viewModelScope.launch {
            try {
                var imageUrl: String? = null
                var fileName: String? = null

                fileUri?.let { uri ->
                    fileName = getFileNameFromUri(uri)
                    val inputStream = postContext.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()

                    if (bytes != null) {
                        val isImage = fileName?.lowercase()?.endsWith(".jpg") == true ||
                                fileName?.lowercase()?.endsWith(".jpeg") == true ||
                                fileName?.lowercase()?.endsWith(".png") == true

                        val finalBytes: ByteArray = (if (isImage) compressImage(postContext, uri) else bytes) as ByteArray

                        imageUrl = repository.uploadFile(finalBytes)
                    }
                }

                repository.createPost(
                    title = title,
                    description = description,
                    imageUrl = imageUrl,
                    fileName = fileName ?: ""
                )

                refreshPosts()

            } catch (e: Exception) {
                Log.e(TAG, "Error creating post", e)
                _refreshState.value = PostsState.Error("Failed to create post")
            }
        }
    }




    fun updatePost(post: Post, newImageUri: Uri? = null) {
        viewModelScope.launch {
            try {
                val updatedImageUrl = newImageUri?.let {
                    val imageBytes = compressImage(postContext, it)
                    repository.uploadFile((imageBytes))
                } ?: post.imageUrl
                val updatedPost = post.copy(imageUrl = updatedImageUrl)
                repository.updatePost(updatedPost)
                refreshPosts()
            } catch (e: Exception) {
                _refreshState.value = PostsState.Error(e.message ?: "Failed to update post")
            }
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            try {
                repository.deletePost(post)
                Log.d(TAG, "Post deleted successfully: ${post.id}")
                refreshPosts()
            } catch (e: Exception) {
                _refreshState.value = PostsState.Error(e.message ?: "Failed to delete post")
            }
        }
    }
}
