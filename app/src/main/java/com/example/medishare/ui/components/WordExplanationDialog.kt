package com.example.medishare.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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