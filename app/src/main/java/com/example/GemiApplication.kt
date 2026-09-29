package com.example

import android.app.Application
import android.util.Log
import com.example.data.remote.FirebaseSafeAccess
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class GemiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:181913875881:android:gemileadsgrb2b")
                    .setApiKey("AIzaSyDummyKeyForLocalPreviewModeOnly")
                    .setProjectId("gemileads-preview")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("GemiApplication", "FirebaseApp initialized with fallback config")
            }
        } catch (e: Throwable) {
            Log.w("GemiApplication", "Firebase initialization skipped (offline mode): ${e.message}")
        }
    }
}
