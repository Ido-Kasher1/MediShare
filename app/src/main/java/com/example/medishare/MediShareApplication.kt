package com.example.medishare

import android.app.Application
import com.example.medishare.data.local.AppDatabase

class MediShareApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDatabase.getInstance(this)
    }
}
