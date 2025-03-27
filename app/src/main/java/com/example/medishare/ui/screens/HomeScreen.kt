package com.example.medishare.ui.screens
import ClickablePostText
import android.util.Log
import com.example.medishare.ui.components.FilePreview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.LazyPagingItems
import com.example.medishare.data.local.PostEntity
import com.example.medishare.ui.components.WordExplanationDialog
import com.example.medishare.ui.viewmodels.PostViewModel
import com.example.medishare.ui.viewmodels.PostViewModelFactory
import com.example.medishare.ui.viewmodels.PostsState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.example.medishare.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


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
    val TAG = "HomeScreen"
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
            onRefresh = { viewModel.refreshPosts() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ){
            when (refreshState) {
                is PostsState.Error -> {
                    val error = (refreshState as PostsState.Error).message
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                        style = TextStyle(textDirection =  TextDirection.Content)
                    )
                }
                else -> {
                    var searchQuery by remember { mutableStateOf("") }

                    Column(modifier = Modifier.fillMaxSize()) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search posts...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )

                        PostList(
                            posts = posts,
                            searchQuery = searchQuery,
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
@Composable
fun PostList(
    posts: LazyPagingItems<PostEntity>,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    val filteredPosts = remember(posts.itemSnapshotList.items, searchQuery) {
        posts.itemSnapshotList.items.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) { items(count = filteredPosts.size, key = { index -> filteredPosts[index]?.id ?: index }) { index ->
        val post = filteredPosts[index]
        post?.let {
            PostCard(post = it)
        }
    }
    }
}
@Composable
fun PostCard(post: PostEntity) {
    var selectedWord by remember { mutableStateOf<String?>(null) }
    var explanation by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    textDirection = TextDirection.Content
                ),
            )

            if (!post.imageUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                FilePreview(
                    fileUrl = post.imageUrl,
                    fileName = post.fileName ?: ""
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            ClickablePostText(post.description) { word ->
                selectedWord = word
                explanation = "טוען הסבר..."
                showDialog = true
            }
        }
    }

    LaunchedEffect(selectedWord) {
        selectedWord?.let { word ->
            withContext(Dispatchers.IO) {
                Log.d("HomeScreen", "Searching for explanation of $word")
                val result = fetchWikiSummary(word)
                withContext(Dispatchers.Main) {
                    explanation = result
                }
            }
        }
    }

    if (showDialog && explanation != null && selectedWord != null) {
        WordExplanationDialog(
            word = selectedWord!!,
            explanation = explanation!!,
            onDismiss = { showDialog = false }
        )
    }
}


