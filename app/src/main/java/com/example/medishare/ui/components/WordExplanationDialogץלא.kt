package com.example.medishare.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun WordExplanationDialog(
    word: String,
    explanation: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("סגור")
            }
        },
        title = { Text(text = word) },
        text = { Text(text = explanation) }
    )
}
