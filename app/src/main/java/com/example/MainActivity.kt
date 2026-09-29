package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.data.notification.GemiNotificationManager
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.LeadViewModel

class MainActivity : ComponentActivity() {
  private val leadViewModel: LeadViewModel by viewModels()

  private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission(),
  ) { isGranted: Boolean ->
    if (isGranted) {
      GemiNotificationManager.syncFcmTokenWithFirestore()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // 1. Initialize FCM Notification Channels
    try {
      GemiNotificationManager.createNotificationChannels(this)
    } catch (e: Throwable) {
      // Ignored if notification service unavailable
    }

    // 2. Safely sync FCM Token in background without blocking UI
    try {
      GemiNotificationManager.syncFcmTokenWithFirestore()
    } catch (e: Throwable) {
      // Offline mode
    }

    // 3. Handle push notification deep-link intent
    try {
      handleNotificationIntent(intent)
    } catch (e: Throwable) {
      // Intent parsing fallback
    }

    setContent {
      MyApplicationTheme {
        MainAppScreen(viewModel = leadViewModel)
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleNotificationIntent(intent)
  }

  private fun handleNotificationIntent(intent: Intent?) {
    if (intent == null) return
    val tab = intent.getIntExtra(GemiNotificationManager.EXTRA_NAVIGATE_TAB, -1)
    if (tab >= 0) {
      leadViewModel.selectedTab.value = tab
    }
    val gemiNumber = intent.getStringExtra(GemiNotificationManager.EXTRA_GEMI_NUMBER)
    if (!gemiNumber.isNullOrBlank()) {
      leadViewModel.findAndSelectLeadByGemiNumber(gemiNumber)
    }
  }
}

