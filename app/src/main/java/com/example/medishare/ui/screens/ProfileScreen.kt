package com.example.medishare.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.LazyPagingItems
import com.example.medishare.data.local.PostEntity
import com.example.medishare.models.Post
import com.example.medishare.ui.viewmodels.AuthViewModel
import com.example.medishare.ui.viewmodels.PostViewModel
import com.example.medishare.ui.viewmodels.PostViewModelFactory
import com.example.medishare.ui.viewmodels.PostsState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val postViewModel: PostViewModel = viewModel(
        factory = PostViewModelFactory(context)
    )

    val currentUser = FirebaseAuth.getInstance().currentUser
    val refreshState by postViewModel.refreshState.collectAsState()
    val isRefreshing = refreshState is PostsState.Loading
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)


    val userPosts = currentUser?.uid?.let { uid ->
        postViewModel.getUserPosts(uid).collectAsLazyPagingItems()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { authViewModel.signOut() }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // User Info Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Email: ${currentUser?.email ?: "Not available"}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Posts Section
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = { postViewModel.refreshPosts() }
            ) {
                when (refreshState) {
                    is PostsState.Error -> {
                        val error = (refreshState as PostsState.Error).message
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    else -> {
                        userPosts?.let { posts ->
                            UserPostsList(
                                posts = posts,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserPostsList(
    posts: LazyPagingItems<PostEntity>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(count = posts.itemCount, key = { index -> posts[index]?.id ?: index }) { index ->
            val post = posts[index]
            post?.let {
                UserPostCard(post = it)
            }
        }
    }
}

@Composable
private fun UserPostCard(post: PostEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = post.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
