package com.example.medishare.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.GeoPoint
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
    
    @ServerTimestamp
    @PropertyName("timestamp")
    val timestamp: Date = Date(),
    
    @get:Exclude
    @set:Exclude
    @PropertyName("location")
    var location: GeoPoint? = null
)