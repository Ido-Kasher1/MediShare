package com.example.medishare.ui.components

import android.net.Uri
import androidx.compose.material3.Button
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.medishare.models.Post

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostDialog(
    post: Post,
    onDismiss: () -> Unit,
    onConfirm: (Post, Uri?) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(post.title) }
    var description by remember { mutableStateOf(post.description) }
    var newImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) newImageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("עריכת פוסט") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("כותרת") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("תיאור") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))

                val imageToShow = newImageUri ?: post.imageUrl?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }
                imageToShow?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = "Post Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Inside
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(onClick = { launcher.launch("image/*") }) {
                    Text("בחר תמונה חדשה")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(post.copy(title = title, description = description), newImageUri)
                    onDismiss()
                }
            ) {
                Text("שמור")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ביטול")
            }
        }
    )
}

