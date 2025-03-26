package com.example.medishare.models

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val profileImageUrl: String = ""
)