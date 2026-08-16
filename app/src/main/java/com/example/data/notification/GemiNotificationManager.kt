package com.example.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.entity.LeadEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.model.GemiLead
import com.example.data.repository.FirestoreUserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Manages notification channels, FCM token synchronization, and daily lead push alerts.
 */
object GemiNotificationManager {

    const val CHANNEL_DAILY_MATCHES = "gemi_daily_matches"
    const val CHANNEL_LEAD_ALERTS = "gemi_lead_alerts"

    const val EXTRA_GEMI_NUMBER = "extra_gemi_number"
    const val EXTRA_NAVIGATE_TAB = "extra_navigate_tab" // 0: Matches, 1: Live Feed, 2: CRM

    private const val NOTIFICATION_ID_DAILY_DIGEST = 1001
    private const val NOTIFICATION_ID_BASE_LEAD = 2000

    /**
     * Initializes Android notification channels on Android 8.0+ (Oreo).
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 1. Channel for Daily Morning Leads Digest
            val dailyMatchesChannel = NotificationChannel(
                CHANNEL_DAILY_MATCHES,
                "Ημερήσιες Αντιστοιχίσεις Leads (Daily Matches)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Ειδοποιήσεις για νέες επιχειρήσεις Γ.Ε.ΜΗ. που ταιριάζουν με το προφίλ σας"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
                setShowBadge(true)
            }

            // 2. Channel for Real-time High Match Lead Alerts
            val leadAlertsChannel = NotificationChannel(
                CHANNEL_LEAD_ALERTS,
                "Ειδοποιήσεις Νέων Εγγραφών Γ.Ε.ΜΗ.",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Άμεσες ειδοποιήσεις για νεοσύστατες εταιρείες στους στοχευμένους ΚΑΔ"
                enableLights(true)
                lightColor = Color.GREEN
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(dailyMatchesChannel)
            notificationManager.createNotificationChannel(leadAlertsChannel)
        }
    }

    /**
     * Synchronizes the active Firebase Cloud Messaging (FCM) registration token
     * with the user's Firestore 'users/{userId}' document and subscribes to standard topics.
     */
    fun syncFcmTokenWithFirestore(userId: String? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                val currentUid = userId ?: FirebaseAuth.getInstance().currentUser?.uid

                if (!currentUid.isNullOrBlank() && token.isNotBlank()) {
                    val userProfileRepo = FirestoreUserProfileRepository()
                    userProfileRepo.updateUserFcmToken(currentUid, token)
                }

                // Subscribe to universal broadcast topic for daily morning GEMI syncs
                FirebaseMessaging.getInstance().subscribeToTopic("gemi_daily_ingestion_updates").await()
            } catch (e: Exception) {
                // Token fetch / subscription failed gracefully (e.g. offline or unconfigured services)
            }
        }
    }

    /**
     * Dispatches a rich push notification for newly discovered leads matching the user's preferences.
     */
    fun showDailyLeadMatchNotification(
        context: Context,
        matchingLeads: List<LeadEntity>,
        userProfile: UserProfileEntity?
    ) {
        if (matchingLeads.isEmpty()) return

        // Permission check on Android 13+ (API 33)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val topMatch = matchingLeads.maxByOrNull { it.matchScore } ?: matchingLeads.first()
        val count = matchingLeads.size

        val title = if (count == 1) {
            "🎯 Νέα Αντιστοίχιση Lead (${topMatch.matchScore}% Match)"
        } else {
            "🎯 $count Νέες Αντιστοιχίσεις Leads Σήμερα!"
        }

        val sectorInfo = if (topMatch.sector.isNotBlank()) "[${topMatch.sector}] " else ""
        val contentText = if (count == 1) {
            "$sectorInfo${topMatch.companyName} (${topMatch.region})"
        } else {
            "Κορυφαία: $sectorInfo${topMatch.companyName} (${topMatch.matchScore}% Match). Πατήστε για προβολή."
        }

        // Tap Intent: Open MainActivity in "My Matches" tab (tab index 0)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAVIGATE_TAB, 0)
            putExtra(EXTRA_GEMI_NUMBER, topMatch.gemiNumber)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_DAILY_DIGEST,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Expandable Inbox Style showing top 4 leads
        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(title)
            .setSummaryText("${matchingLeads.size} νέες επιχειρήσεις")

        matchingLeads.take(4).forEach { lead ->
            inboxStyle.addLine("• [${lead.matchScore}%] ${lead.companyName} (${lead.legalForm}, ${lead.region})")
        }

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_DAILY_MATCHES)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(inboxStyle)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(NOTIFICATION_ID_DAILY_DIGEST, notificationBuilder.build())
        } catch (e: SecurityException) {
            // Missing notification permission
        }
    }

    /**
     * Dispatches notification received directly from FCM remote payload.
     */
    fun showRemoteFcmNotification(
        context: Context,
        title: String,
        body: String,
        gemiNumber: String? = null,
        tabIndex: Int = 0
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAVIGATE_TAB, tabIndex)
            gemiNumber?.let { putExtra(EXTRA_GEMI_NUMBER, it) }
        }

        val notificationId = gemiNumber?.hashCode() ?: System.currentTimeMillis().toInt()

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_DAILY_MATCHES)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(notificationId, notificationBuilder.build())
        } catch (e: SecurityException) {
            // Permission rejected
        }
    }
}
