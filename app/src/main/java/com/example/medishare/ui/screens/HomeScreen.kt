package com.example.medishare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.LazyPagingItems
import coil.compose.AsyncImage
import com.example.medishare.data.local.PostEntity
import com.example.medishare.models.Post
import com.example.medishare.ui.viewmodels.PostViewModel
import com.example.medishare.ui.viewmodels.PostViewModelFactory
import com.example.medishare.ui.viewmodels.PostsState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCreatePost: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: PostViewModel = viewModel(
        factory = PostViewModelFactory(context)
    )

    val refreshState by viewModel.refreshState.collectAsState()
    val posts = viewModel.posts.collectAsLazyPagingItems()
    val isRefreshing = refreshState is PostsState.Loading
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MediShare") },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreatePost) {
                Icon(Icons.Default.Add, contentDescription = "Create Post")
            }
        },
        modifier = modifier
    ) { padding ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.refreshPosts() }
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
                    PostList(
                        posts = posts,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    )
                }
            }
        }
    }
}

@Composable
fun PostList(
    posts: LazyPagingItems<PostEntity>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) { items(count = posts.itemCount, key = { index -> posts[index]?.id ?: index }) { index ->
        val post = posts[index]
        post?.let {
            PostCard(post = it)
        }
    }
    }
}
@Composable
private fun PostCard(post: PostEntity) {
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
            post.imageUrl?.let { url ->
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = url,
                    contentDescription = "Post attachment",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Inside
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = post.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
