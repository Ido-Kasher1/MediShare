package com.example.medishare.data

import android.util.Log
import com.example.medishare.models.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signUp(
        email: String,
        password: String,
        displayName: String,
        profileImageBytes: ByteArray?
    ): Result<FirebaseUser?> {
        return try{
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user!!

            val imageUrl = profileImageBytes?.let { bytes ->
                val ref = com.google.firebase.storage.FirebaseStorage.getInstance()
                    .reference.child("profile_images/${user.uid}.jpg")

                ref.putBytes(bytes).await()
                ref.downloadUrl.await().toString()
            } ?: ""

            val userProfile = com.example.medishare.models.UserProfile(
                uid = user.uid,
                email = user.email ?: "",
                displayName = displayName,
                profileImageUrl = imageUrl
            )

            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .set(userProfile)
                .await()
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

    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .await()

            snapshot.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserProfile(uid: String, displayName: String, imageBytes: ByteArray?): Boolean {
        return try {
            var imageUrl: String? = null
            if (imageBytes != null) {
                val ref = FirebaseStorage.getInstance().reference.child("profile_images/$uid.jpg")
                ref.putBytes(imageBytes).await()
                imageUrl = ref.downloadUrl.await().toString()
            }

            val updates = mutableMapOf<String, Any>("displayName" to displayName)
            imageUrl?.let { updates["profileImageUrl"] = it }

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .update(updates)
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

}
