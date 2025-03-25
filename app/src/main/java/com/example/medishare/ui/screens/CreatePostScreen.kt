package com.example.medishare.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.medishare.ui.viewmodels.PostViewModel
import com.example.medishare.ui.viewmodels.PostViewModelFactory
import com.example.medishare.ui.viewmodels.PostsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onPostCreated: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: PostViewModel = viewModel(
        factory = PostViewModelFactory(context)
    )

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var isCreatingPost by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        selectedFileName = uri?.lastPathSegment?.substringAfterLast('/')
    }

    val refreshState by viewModel.refreshState.collectAsState()

    LaunchedEffect(refreshState) {
        if (isCreatingPost && refreshState !is PostsState.Loading) {
            isCreatingPost = false
            if (refreshState !is PostsState.Error) {
                onPostCreated()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedButton(
                onClick = { filePicker.launch("*/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attach file",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(if (selectedImageUri != null) "Change File" else "Add File")
            }

            selectedImageUri?.let {
                Column {
                    AsyncImage(
                        model = it,
                        contentDescription = "Selected file preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Inside
                    )
                    selectedFileName?.let { fileName ->
                        Text(
                            text = "Selected file: $fileName",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            if (refreshState is PostsState.Error) {
                Text(
                    text = (refreshState as PostsState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    isCreatingPost = true
                    viewModel.createPost(title, description, selectedImageUri)
                },
                enabled = title.isNotBlank() && description.isNotBlank() && !isCreatingPost,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Post")
            }

            if (isCreatingPost) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
