package com.example.medishare.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Post(
    @DocumentId
    val id: String = "",
    
    @PropertyName("userId")
    val userId: String = "",
    
    @PropertyName("title")
    val title: String = "",
    
    @PropertyName("description")
    val description: String = "",
    
    @PropertyName("imageUrl")
    val imageUrl: String = "",

    @PropertyName("fileName")
    val fileName: String = "",
    
    @ServerTimestamp
    @PropertyName("timestamp")
    val timestamp: Date = Date(),

)