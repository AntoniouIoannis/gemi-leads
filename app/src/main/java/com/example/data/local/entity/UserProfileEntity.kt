package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val companyName: String = "",
    val ownGemiNumber: String = "",
    val businessType: String = "", // e.g. "Προμηθευτής Εξοπλισμού Εστίασης"
    val targetSectors: String = "HORECA,TOURISM", // Comma-separated category IDs
    val targetKads: String = "56.10,56.30,56.21,55.10,55.20", // Comma-separated KAD prefixes
    val isPanHellenic: Boolean = false,
    val targetRegions: String = "Αττική,Κεντρική Μακεδονία,Κρήτη", // Comma-separated regions
    val targetPrefectures: String = "", // Specific prefectures
    val targetLegalForms: String = "ΙΚΕ,ΑΕ,ΟΕ,ΕΕ,ΕΠΕ,Ατομική",
    val clientGoal: String = "Physical venues & New business setups",
    val dailyNotificationEnabled: Boolean = true,
    val notificationTime: String = "08:30",
    val isOnboardingCompleted: Boolean = false,
    val lastSyncTimestamp: Long = 0L
)
