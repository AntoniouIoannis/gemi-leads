package com.example.data.model

import com.example.data.local.entity.LeadEntity
import com.example.data.local.entity.UserProfileEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Firestore Collection Constants defined in the GEMI Lead Generator specifications.
 */
object FirestoreCollections {
    const val USERS = "users"
    const val BUSINESS_PROFILES = "business_profiles"
    const val GEMI_DAILY_LEADS = "gemi_daily_leads"
}

// Convenient typealiases matching specification names
typealias User = FirestoreUser
typealias BusinessProfile = FirestoreBusinessProfile
typealias GemiLead = FirestoreGemiDailyLead

/**
 * 1. 'users' Collection Document Definition
 * Document ID: {userId} (Firebase Auth UID)
 *
 * Represents an authenticated user account within the platform.
 */
@JsonClass(generateAdapter = true)
data class FirestoreUser(
    @Json(name = "userId")
    val userId: String = "",

    @Json(name = "email")
    val email: String = "",

    @Json(name = "displayName")
    val displayName: String = "",

    @Json(name = "fcmToken")
    val fcmToken: String? = null,

    @Json(name = "photoUrl")
    val photoUrl: String? = null,

    @Json(name = "status")
    val status: String = "ACTIVE", // "ACTIVE", "SUSPENDED", "PENDING"

    @Json(name = "createdAt")
    val createdAt: String = "", // ISO-8601 UTC timestamp e.g. "2026-08-15T08:00:00Z"

    @Json(name = "lastLoginAt")
    val lastLoginAt: String = "" // ISO-8601 UTC timestamp e.g. "2026-08-15T12:30:00Z"
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "email" to email,
        "displayName" to displayName,
        "fcmToken" to fcmToken,
        "photoUrl" to photoUrl,
        "status" to status,
        "createdAt" to createdAt,
        "lastLoginAt" to lastLoginAt
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): FirestoreUser {
            return FirestoreUser(
                userId = (map["userId"] as? String) ?: id,
                email = (map["email"] as? String).orEmpty(),
                displayName = (map["displayName"] as? String).orEmpty(),
                fcmToken = map["fcmToken"] as? String,
                photoUrl = map["photoUrl"] as? String,
                status = (map["status"] as? String) ?: "ACTIVE",
                createdAt = (map["createdAt"] as? String).orEmpty(),
                lastLoginAt = (map["lastLoginAt"] as? String).orEmpty()
            )
        }
    }
}

/**
 * 2. 'business_profiles' Collection Document Definition
 * Document ID: {userId} (1:1 mapping with the authenticated user)
 *
 * Stores the user's business profile, KAD targeting rules, geographic preferences,
 * and alert configurations for the automated GEMI matching engine.
 */
@JsonClass(generateAdapter = true)
data class FirestoreBusinessProfile(
    @Json(name = "userId")
    val userId: String = "",

    @Json(name = "companyName")
    val companyName: String = "",

    @Json(name = "ownGemiNumber")
    val ownGemiNumber: String = "",

    @Json(name = "businessType")
    val businessType: String = "",

    @Json(name = "targetSectors")
    val targetSectors: List<String> = emptyList(), // e.g. ["HORECA", "TOURISM", "RETAIL"]

    @Json(name = "targetKads")
    val targetKads: List<String> = emptyList(), // e.g. ["56.10", "56.30", "56.21", "55.10"]

    @Json(name = "isPanHellenic")
    val isPanHellenic: Boolean = false,

    @Json(name = "targetRegions")
    val targetRegions: List<String> = emptyList(), // e.g. ["Αττική", "Κρήτη", "Κεντρική Μακεδονία"]

    @Json(name = "targetPrefectures")
    val targetPrefectures: List<String> = emptyList(),

    @Json(name = "targetLegalForms")
    val targetLegalForms: List<String> = listOf("ΙΚΕ", "ΑΕ", "ΟΕ", "ΕΕ", "ΕΠΕ", "Ατομική"),

    @Json(name = "notificationTime")
    val notificationTime: String = "08:30", // Format "HH:mm"

    @Json(name = "dailySummaryEnabled")
    val dailySummaryEnabled: Boolean = true,

    @Json(name = "emailAlerts")
    val emailAlerts: Boolean = true,

    @Json(name = "pushNotifications")
    val pushNotifications: Boolean = true,

    @Json(name = "clientTargetGoal")
    val clientTargetGoal: String = "",

    @Json(name = "updatedAt")
    val updatedAt: String = "", // ISO-8601 UTC timestamp

    @Json(name = "createdAt")
    val createdAt: String = "" // ISO-8601 UTC timestamp
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "companyName" to companyName,
        "ownGemiNumber" to ownGemiNumber,
        "businessType" to businessType,
        "targetSectors" to targetSectors,
        "targetKads" to targetKads,
        "isPanHellenic" to isPanHellenic,
        "targetRegions" to targetRegions,
        "targetPrefectures" to targetPrefectures,
        "targetLegalForms" to targetLegalForms,
        "notificationTime" to notificationTime,
        "dailySummaryEnabled" to dailySummaryEnabled,
        "emailAlerts" to emailAlerts,
        "pushNotifications" to pushNotifications,
        "clientTargetGoal" to clientTargetGoal,
        "updatedAt" to updatedAt,
        "createdAt" to createdAt
    )

    fun toLocalEntity(): UserProfileEntity {
        return UserProfileEntity(
            id = 1,
            companyName = companyName,
            ownGemiNumber = ownGemiNumber,
            businessType = businessType,
            targetSectors = targetSectors.joinToString(","),
            targetKads = targetKads.joinToString(","),
            isPanHellenic = isPanHellenic,
            targetRegions = targetRegions.joinToString(","),
            targetPrefectures = targetPrefectures.joinToString(","),
            targetLegalForms = targetLegalForms.joinToString(","),
            clientGoal = clientTargetGoal.ifBlank { "Physical venues & New business setups" },
            dailyNotificationEnabled = dailySummaryEnabled,
            notificationTime = notificationTime,
            isOnboardingCompleted = true,
            lastSyncTimestamp = System.currentTimeMillis()
        )
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(id: String, map: Map<String, Any?>): FirestoreBusinessProfile {
            return FirestoreBusinessProfile(
                userId = (map["userId"] as? String) ?: id,
                companyName = (map["companyName"] as? String).orEmpty(),
                ownGemiNumber = (map["ownGemiNumber"] as? String).orEmpty(),
                businessType = (map["businessType"] as? String).orEmpty(),
                targetSectors = (map["targetSectors"] as? List<String>) ?: emptyList(),
                targetKads = (map["targetKads"] as? List<String>) ?: emptyList(),
                isPanHellenic = (map["isPanHellenic"] as? Boolean) ?: false,
                targetRegions = (map["targetRegions"] as? List<String>) ?: emptyList(),
                targetPrefectures = (map["targetPrefectures"] as? List<String>) ?: emptyList(),
                targetLegalForms = (map["targetLegalForms"] as? List<String>) ?: listOf("ΙΚΕ", "ΑΕ", "ΟΕ", "ΕΕ"),
                notificationTime = (map["notificationTime"] as? String) ?: "08:30",
                dailySummaryEnabled = (map["dailySummaryEnabled"] as? Boolean) ?: true,
                emailAlerts = (map["emailAlerts"] as? Boolean) ?: true,
                pushNotifications = (map["pushNotifications"] as? Boolean) ?: true,
                clientTargetGoal = (map["clientTargetGoal"] as? String).orEmpty(),
                updatedAt = (map["updatedAt"] as? String).orEmpty(),
                createdAt = (map["createdAt"] as? String).orEmpty()
            )
        }

        fun fromLocalEntity(entity: UserProfileEntity, userId: String = "usr_current"): FirestoreBusinessProfile {
            return FirestoreBusinessProfile(
                userId = userId,
                companyName = entity.companyName,
                ownGemiNumber = entity.ownGemiNumber,
                businessType = entity.businessType,
                targetSectors = entity.targetSectors.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                targetKads = entity.targetKads.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                isPanHellenic = entity.isPanHellenic,
                targetRegions = entity.targetRegions.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                targetPrefectures = entity.targetPrefectures.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                targetLegalForms = entity.targetLegalForms.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                clientTargetGoal = entity.clientGoal,
                notificationTime = entity.notificationTime,
                dailySummaryEnabled = entity.dailyNotificationEnabled,
                updatedAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }.format(java.util.Date(entity.lastSyncTimestamp.takeIf { it > 0L } ?: System.currentTimeMillis()))
            )
        }
    }
}

/**
 * 3. 'gemi_daily_leads' Collection Document Definition
 * Document ID: {gemiNumber} (12-digit Greek Commercial Registry number e.g. "179428301000")
 *
 * Ingested daily by the Cloud Function worker from the official GEMI Open Data API.
 * Contains all official registry fields, classification codes, geographical info,
 * and contact data for new business incorporations.
 */
@JsonClass(generateAdapter = true)
data class FirestoreGemiDailyLead(
    @Json(name = "gemiNumber")
    val gemiNumber: String = "",

    @Json(name = "afm")
    val afm: String = "",

    @Json(name = "companyName")
    val companyName: String = "",

    @Json(name = "tradeName")
    val tradeName: String = "",

    @Json(name = "legalForm")
    val legalForm: String = "ΙΚΕ", // "ΙΚΕ", "ΑΕ", "ΟΕ", "ΕΕ", "ΕΠΕ", "Ατομική"

    @Json(name = "registrationDate")
    val registrationDate: String = "", // ISO Date "YYYY-MM-DD" e.g. "2026-08-15"

    @Json(name = "primaryKad")
    val primaryKad: String = "", // e.g. "56.10"

    @Json(name = "kadDescription")
    val kadDescription: String = "",

    @Json(name = "secondaryKads")
    val secondaryKads: List<String> = emptyList(), // e.g. ["56.30", "56.21"]

    @Json(name = "sector")
    val sector: String = "", // e.g. "HORECA", "TOURISM", "RETAIL", "TECH"

    @Json(name = "region")
    val region: String = "", // e.g. "Αττική", "Κρήτη", "Κεντρική Μακεδονία"

    @Json(name = "prefecture")
    val prefecture: String = "", // e.g. "Νότιος Τομέας Αθηνών"

    @Json(name = "municipality")
    val municipality: String = "", // e.g. "Δήμος Γλυφάδας"

    @Json(name = "address")
    val address: String = "", // e.g. "Λεωφόρος Ποσειδώνος 48"

    @Json(name = "postalCode")
    val postalCode: String = "", // e.g. "16675"

    @Json(name = "chamberName")
    val chamberName: String = "", // e.g. "Επαγγελματικό Επιμελητήριο Αθηνών"

    @Json(name = "phone")
    val phone: String = "",

    @Json(name = "email")
    val email: String = "",

    @Json(name = "website")
    val website: String = "",

    @Json(name = "initialCapital")
    val initialCapital: String = "",

    @Json(name = "administrators")
    val administrators: List<String> = emptyList(),

    @Json(name = "ingestedAt")
    val ingestedAt: String = "", // ISO-8601 UTC timestamp

    @Json(name = "source")
    val source: String = "GEMI_OPEN_DATA_API"
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "gemiNumber" to gemiNumber,
        "afm" to afm,
        "companyName" to companyName,
        "tradeName" to tradeName,
        "legalForm" to legalForm,
        "registrationDate" to registrationDate,
        "primaryKad" to primaryKad,
        "kadDescription" to kadDescription,
        "secondaryKads" to secondaryKads,
        "sector" to sector,
        "region" to region,
        "prefecture" to prefecture,
        "municipality" to municipality,
        "address" to address,
        "postalCode" to postalCode,
        "chamberName" to chamberName,
        "phone" to phone,
        "email" to email,
        "website" to website,
        "initialCapital" to initialCapital,
        "administrators" to administrators,
        "ingestedAt" to ingestedAt,
        "source" to source
    )

    fun toLocalEntity(matchScore: Int = 0, matchReasons: String = ""): LeadEntity {
        return LeadEntity(
            gemiNumber = gemiNumber,
            afm = afm,
            companyName = companyName,
            tradeName = tradeName,
            legalForm = legalForm,
            registrationDate = registrationDate,
            primaryKad = primaryKad,
            kadDescription = kadDescription,
            secondaryKads = secondaryKads.joinToString(","),
            sector = sector,
            region = region,
            prefecture = prefecture,
            municipality = municipality,
            address = address,
            postalCode = postalCode,
            chamberName = chamberName,
            phone = phone,
            email = email,
            website = website,
            initialCapital = initialCapital,
            administrators = administrators.joinToString(", "),
            isSaved = false,
            savedTimestamp = 0L,
            pipelineStatus = PipelineStatus.NEW,
            userNotes = "",
            lastContactedDate = "",
            customTags = "",
            matchScore = matchScore,
            matchReasons = matchReasons,
            ingestionTimestamp = System.currentTimeMillis()
        )
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(id: String, map: Map<String, Any?>): FirestoreGemiDailyLead {
            return FirestoreGemiDailyLead(
                gemiNumber = (map["gemiNumber"] as? String) ?: id,
                afm = (map["afm"] as? String).orEmpty(),
                companyName = (map["companyName"] as? String).orEmpty(),
                tradeName = (map["tradeName"] as? String).orEmpty(),
                legalForm = (map["legalForm"] as? String) ?: "ΙΚΕ",
                registrationDate = (map["registrationDate"] as? String).orEmpty(),
                primaryKad = (map["primaryKad"] as? String).orEmpty(),
                kadDescription = (map["kadDescription"] as? String).orEmpty(),
                secondaryKads = (map["secondaryKads"] as? List<String>) ?: emptyList(),
                sector = (map["sector"] as? String).orEmpty(),
                region = (map["region"] as? String).orEmpty(),
                prefecture = (map["prefecture"] as? String).orEmpty(),
                municipality = (map["municipality"] as? String).orEmpty(),
                address = (map["address"] as? String).orEmpty(),
                postalCode = (map["postalCode"] as? String).orEmpty(),
                chamberName = (map["chamberName"] as? String).orEmpty(),
                phone = (map["phone"] as? String).orEmpty(),
                email = (map["email"] as? String).orEmpty(),
                website = (map["website"] as? String).orEmpty(),
                initialCapital = (map["initialCapital"]?.toString()).orEmpty(),
                administrators = (map["administrators"] as? List<String>) ?: emptyList(),
                ingestedAt = (map["ingestedAt"] as? String).orEmpty(),
                source = (map["source"] as? String) ?: "GEMI_OPEN_DATA_API"
            )
        }

        fun fromLocalEntity(entity: LeadEntity): FirestoreGemiDailyLead {
            return FirestoreGemiDailyLead(
                gemiNumber = entity.gemiNumber,
                afm = entity.afm,
                companyName = entity.companyName,
                tradeName = entity.tradeName,
                legalForm = entity.legalForm,
                registrationDate = entity.registrationDate,
                primaryKad = entity.primaryKad,
                kadDescription = entity.kadDescription,
                secondaryKads = entity.secondaryKads.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                sector = entity.sector,
                region = entity.region,
                prefecture = entity.prefecture,
                municipality = entity.municipality,
                address = entity.address,
                postalCode = entity.postalCode,
                chamberName = entity.chamberName,
                phone = entity.phone,
                email = entity.email,
                website = entity.website,
                initialCapital = entity.initialCapital,
                administrators = entity.administrators.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                ingestedAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }.format(java.util.Date(entity.ingestionTimestamp)),
                source = "GEMI_OPEN_DATA_API"
            )
        }
    }
}
