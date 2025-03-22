package com.example.pixelpeppers.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val auth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login to PixelPeppers", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        Log.d("FirebaseAuth", "Trying to sign in...")
                        val result = auth.signInWithEmailAndPassword(email, password).await()
                        Log.d("FirebaseAuth", "Sign-in successful: ${result.user?.uid}")

                        val user = result.user
                        user?.let {
                            val userData = mapOf(
                                "id" to it.uid,
                                "email" to it.email,
                                "onboardingComplete" to false
                            )
                            FirebaseFirestore.getInstance().collection("users")
                                .document(it.uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    Log.d("Firestore", "User saved in Firestore")
                                }.addOnFailureListener { e ->
                                    Log.e("Firestore", "Error saving user: ${e.message}")
                                }
                        }

                        onLoginSuccess()

                    } catch (signInError: Exception) {
                        Log.w("FirebaseAuth", "Sign-in failed: ${signInError.message}")
                        try {
                            val registerResult = auth.createUserWithEmailAndPassword(email, password).await()
                            Log.d("FirebaseAuth", "Registration successful: ${registerResult.user?.uid}")

                            // Save new user to Firestore
                            val user = registerResult.user
                            user?.let {
                                val userData = mapOf(
                                    "id" to it.uid,
                                    "email" to it.email,
                                    "onboardingComplete" to false
                                )
                                FirebaseFirestore.getInstance().collection("users")
                                    .document(it.uid)
                                    .set(userData)
                                    .addOnSuccessListener {
                                        Log.d("Firestore", "New user saved in Firestore")
                                    }.addOnFailureListener { e ->
                                        Log.e("Firestore", "Error saving new user: ${e.message}")
                                    }
                            }

                            onLoginSuccess()

                        } catch (signUpError: Exception) {
                            error = signUpError.localizedMessage
                            Log.e("FirebaseAuth", "Registration failed: ${signUpError.message}")
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login / Register")
        }


        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
