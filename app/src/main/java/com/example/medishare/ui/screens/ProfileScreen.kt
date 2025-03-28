package com.example.medishare.ui.screens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import com.example.medishare.ui.components.EditPostDialog
import com.example.medishare.ui.components.EditProfileDialog
import com.example.medishare.ui.components.FilePreview
import com.example.medishare.ui.viewmodels.AuthViewModel
import com.example.medishare.ui.viewmodels.PostViewModel
import com.example.medishare.ui.viewmodels.PostViewModelFactory
import com.example.medishare.ui.viewmodels.PostsState
import com.example.medishare.utils.compressImage
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val postViewModel: PostViewModel = viewModel(factory = PostViewModelFactory(context))

    val currentUser = FirebaseAuth.getInstance().currentUser
    val refreshState by postViewModel.refreshState.collectAsState()
    val isRefreshing = refreshState is PostsState.Loading
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

    var editingPost by remember { mutableStateOf<Post?>(null) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val userProfile by authViewModel.userProfile
    val userPosts = currentUser?.uid?.let { uid ->
        postViewModel.getUserPosts(uid).collectAsLazyPagingItems()
    }

    LaunchedEffect(Unit) {
        authViewModel.loadUserProfile()
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
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sign out")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = modifier.padding(padding)) {
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = {
                    postViewModel.refreshPosts()
                    authViewModel.loadUserProfile()
                }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (userProfile != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            userProfile!!.profileImageUrl.takeIf { it.isNotBlank() }?.let { imageUrl ->
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Profile Image",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Text(
                                text = userProfile!!.displayName,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = userProfile!!.email,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { showEditProfileDialog = true }) {
                                Text("ערוך פרופיל")
                            }
                        }
                    }

                    if (userPosts != null) {
                        var searchQuery by remember { mutableStateOf("") }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search posts...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )

                        UserPostsList(
                            posts = userPosts,
                            onEditPost = { post -> editingPost = post },
                            searchQuery = searchQuery,
                            onDeletePost = { post -> postViewModel.deletePost(post) }
                        )
                    }
                }
            }

            editingPost?.let { post ->
                EditPostDialog(
                    post = post,
                    onDismiss = { editingPost = null },
                    onConfirm = { updatedPost, newImageUri ->
                        postViewModel.updatePost(updatedPost, newImageUri)
                        editingPost = null
                    }
                )
            }

            if (showEditProfileDialog && userProfile != null) {
                EditProfileDialog(
                    currentName = userProfile!!.displayName,
                    onDismiss = { showEditProfileDialog = false },
                    onSave = { newName, imageUri ->
                        val bytes = imageUri?.let { compressImage(context, it) }
                        authViewModel.updateProfile(newName, bytes)
                        showEditProfileDialog = false
                    }
                )
            }
        }
    }
}


@Composable
fun UserPostsList(
    posts: LazyPagingItems<PostEntity>,
    onEditPost: (Post) -> Unit,
    searchQuery: String,
    onDeletePost: (Post) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredPosts = remember(posts.itemSnapshotList.items, searchQuery) {
        posts.itemSnapshotList.items.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
        }
    }
    LazyColumn(modifier = modifier) {
        items(
            count = filteredPosts.size,
            key = { index -> filteredPosts[index]?.id ?: index }
        ) { index ->
            filteredPosts[index]?.let { post ->
                UserPostCard(
                    post = post,
                    onEditClick = onEditPost,
                    onDeleteClick = onDeletePost
                )
            }
        }
    }
}

@Composable
private fun UserPostCard(
    post: PostEntity,
    onEditClick: (Post) -> Unit,
    onDeleteClick: (Post) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                onEditClick(post.toPost())
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                onDeleteClick(post.toPost())
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = post.description,
                style = MaterialTheme.typography.bodyMedium
            )
            if (!post.imageUrl.isNullOrBlank())
            {
                Spacer(modifier = Modifier.height(8.dp))
                FilePreview(fileUrl = post.imageUrl, fileName = post.fileName ?: "")
            }
        }
    }
}


