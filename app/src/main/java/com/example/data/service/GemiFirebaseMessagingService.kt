package com.example.data.service

import com.example.data.notification.GemiNotificationManager
import com.example.data.repository.FirestoreUserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Firebase Cloud Messaging Service handling real-time push events and device token rotation.
 */
class GemiFirebaseMessagingService : FirebaseMessagingService() {

    private val userProfileRepo = FirestoreUserProfileRepository()

    /**
     * Called whenever a new FCM token is generated or rotated for the app instance.
     * Persists the token to the user's Firestore 'users/{userId}' document.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        if (!currentUid.isNullOrBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                userProfileRepo.updateUserFcmToken(currentUid, token)
            }
        }
    }

    /**
     * Called when an incoming FCM message is received while app is in foreground or background with data payload.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // 1. Extract Notification / Data payload fields
        val notificationTitle = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "🎯 Νέο Lead Γ.Ε.ΜΗ. διαθέσιμο"

        val notificationBody = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "Εντοπίστηκε νέα επιχείρηση που ταιριάζει με τα κριτήρια αναζήτησής σας."

        val gemiNumber = remoteMessage.data["gemiNumber"]
        val tabIndex = remoteMessage.data["tab"]?.toIntOrNull() ?: 0

        // 2. Dispatch local system notification with deep-link navigation
        GemiNotificationManager.showRemoteFcmNotification(
            context = applicationContext,
            title = notificationTitle,
            body = notificationBody,
            gemiNumber = gemiNumber,
            tabIndex = tabIndex
        )
    }
}
