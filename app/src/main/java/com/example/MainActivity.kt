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
    ActivityResultContracts.RequestPermission()
  ) { isGranted: Boolean ->
    if (isGranted) {
      GemiNotificationManager.syncFcmTokenWithFirestore()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // 1. Initialize FCM Notification Channels
    GemiNotificationManager.createNotificationChannels(this)

    // 2. Request Notification Permission on Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED
      ) {
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      } else {
        GemiNotificationManager.syncFcmTokenWithFirestore()
      }
    } else {
      GemiNotificationManager.syncFcmTokenWithFirestore()
    }

    // 3. Handle push notification deep-link intent
    handleNotificationIntent(intent)

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

