package com.example.data.remote

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Provides safe, crash-proof access to Firebase services.
 * When google-services.json is not configured or Firebase is unavailable,
 * methods return null instead of throwing unhandled IllegalStateExceptions.
 */
object FirebaseSafeAccess {
    private const val TAG = "FirebaseSafeAccess"

    fun getFirestore(): FirebaseFirestore? {
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            Log.d(TAG, "FirebaseFirestore unavailable (running offline-first): ${e.message}")
            null
        }
    }

    fun getAuth(): FirebaseAuth? {
        return try {
            FirebaseAuth.getInstance()
        } catch (e: Throwable) {
            Log.d(TAG, "FirebaseAuth unavailable (running offline-first): ${e.message}")
            null
        }
    }

    fun getMessaging(): FirebaseMessaging? {
        return try {
            FirebaseMessaging.getInstance()
        } catch (e: Throwable) {
            Log.d(TAG, "FirebaseMessaging unavailable (running offline-first): ${e.message}")
            null
        }
    }
}
