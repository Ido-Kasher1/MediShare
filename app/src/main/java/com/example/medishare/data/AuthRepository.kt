package com.example.medishare.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signUp(email: String, password: String): Result<FirebaseUser?> {
        return try{
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Log.d("FirebaseAuth", "Sign-up successful: ${result.user?.uid}")
            return Result.success(result.user)
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "Sign-up failed", e)
            Result.failure(e)
        }


    }

    suspend fun signIn(email: String, password: String): Result<FirebaseUser?> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            return Result.success(result.user)
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "Sign-in failed", e)
            Result.failure(e)
        }

    }

    fun signOut() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}
