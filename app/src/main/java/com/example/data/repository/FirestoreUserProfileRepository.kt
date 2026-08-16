package com.example.data.repository

import com.example.data.local.dao.UserProfileDao
import com.example.data.local.entity.UserProfileEntity
import com.example.data.model.BusinessProfile
import com.example.data.model.FirestoreBusinessProfile
import com.example.data.model.FirestoreCollections
import com.example.data.model.FirestoreUser
import com.example.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Aggregated data model linking an authenticated User to their specific BusinessProfile.
 */
data class UserAccountWithBusinessProfile(
    val user: User,
    val businessProfile: BusinessProfile?
)

/**
 * Interface defining complete CRUD and linking operations for 'users' and 'business_profiles' collections.
 */
interface IFirestoreUserProfileRepository {
    // --- 'users' Collection Operations ---
    fun getUserFlow(userId: String): Flow<User?>
    suspend fun fetchUser(userId: String): Result<User?>
    suspend fun createOrUpdateUser(user: User): Result<Unit>
    suspend fun updateUserFcmToken(userId: String, token: String): Result<Unit>
    suspend fun updateLastLogin(userId: String): Result<Unit>
    suspend fun deleteUser(userId: String): Result<Unit>

    // --- 'business_profiles' Collection Operations ---
    fun getBusinessProfileFlow(userId: String): Flow<BusinessProfile?>
    suspend fun fetchBusinessProfile(userId: String): Result<BusinessProfile?>
    suspend fun createOrUpdateBusinessProfile(profile: BusinessProfile): Result<Unit>
    suspend fun updateTargetPreferences(
        userId: String,
        sectors: List<String>,
        kads: List<String>,
        isPanHellenic: Boolean,
        regions: List<String>,
        prefectures: List<String> = emptyList(),
        legalForms: List<String> = listOf("ΙΚΕ", "ΑΕ", "ΟΕ", "ΕΕ", "ΕΠΕ", "Ατομική")
    ): Result<Unit>
    suspend fun updateNotificationSettings(
        userId: String,
        notificationTime: String,
        dailySummaryEnabled: Boolean,
        emailAlerts: Boolean,
        pushNotifications: Boolean
    ): Result<Unit>
    suspend fun deleteBusinessProfile(userId: String): Result<Unit>

    // --- Linking User & Business Configuration ---
    fun getUserWithBusinessProfileFlow(userId: String): Flow<UserAccountWithBusinessProfile?>
    suspend fun fetchUserWithBusinessProfile(userId: String): Result<UserAccountWithBusinessProfile?>
    suspend fun linkUserToBusinessProfile(userId: String, profile: BusinessProfile): Result<Unit>
    suspend fun initializeNewUserAccount(
        userId: String,
        email: String,
        displayName: String,
        photoUrl: String? = null,
        initialProfile: BusinessProfile? = null
    ): Result<UserAccountWithBusinessProfile>

    // --- Room Local Database Synchronization ---
    suspend fun syncRemoteProfileToLocalDb(userId: String, userProfileDao: UserProfileDao): Result<UserProfileEntity?>
    suspend fun syncLocalProfileToFirestore(userId: String, localEntity: UserProfileEntity): Result<Unit>
}

/**
 * Production implementation of UserProfileRepository utilizing Cloud Firestore.
 */
class FirestoreUserProfileRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : IFirestoreUserProfileRepository {

    private val usersCollection = firestore.collection(FirestoreCollections.USERS)
    private val profilesCollection = firestore.collection(FirestoreCollections.BUSINESS_PROFILES)

    private fun getCurrentIsoTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())
    }

    // ==========================================
    // 1. 'users' Collection CRUD Operations
    // ==========================================

    override fun getUserFlow(userId: String): Flow<User?> = callbackFlow {
        if (userId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val docRef = usersCollection.document(userId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists() && snapshot.data != null) {
                trySend(FirestoreUser.fromMap(snapshot.id, snapshot.data!!))
            } else {
                trySend(null)
            }
        }

        awaitClose { listener.remove() }
    }

    override suspend fun fetchUser(userId: String): Result<User?> {
        return try {
            if (userId.isBlank()) return Result.success(null)
            val doc = usersCollection.document(userId).get().await()
            if (doc.exists() && doc.data != null) {
                Result.success(FirestoreUser.fromMap(doc.id, doc.data!!))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createOrUpdateUser(user: User): Result<Unit> {
        return try {
            val userMap = user.toMap().toMutableMap()
            if (user.createdAt.isBlank()) {
                userMap["createdAt"] = getCurrentIsoTimestamp()
            }
            userMap["lastLoginAt"] = getCurrentIsoTimestamp()

            usersCollection.document(user.userId)
                .set(userMap, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserFcmToken(userId: String, token: String): Result<Unit> {
        return try {
            usersCollection.document(userId)
                .update("fcmToken", token)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateLastLogin(userId: String): Result<Unit> {
        return try {
            usersCollection.document(userId)
                .update("lastLoginAt", getCurrentIsoTimestamp())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            // Delete user doc and linked business profile in a batch transaction
            val batch = firestore.batch()
            batch.delete(usersCollection.document(userId))
            batch.delete(profilesCollection.document(userId))
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 2. 'business_profiles' CRUD Operations
    // ==========================================

    override fun getBusinessProfileFlow(userId: String): Flow<BusinessProfile?> = callbackFlow {
        if (userId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val docRef = profilesCollection.document(userId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists() && snapshot.data != null) {
                trySend(FirestoreBusinessProfile.fromMap(snapshot.id, snapshot.data!!))
            } else {
                trySend(null)
            }
        }

        awaitClose { listener.remove() }
    }

    override suspend fun fetchBusinessProfile(userId: String): Result<BusinessProfile?> {
        return try {
            if (userId.isBlank()) return Result.success(null)
            val doc = profilesCollection.document(userId).get().await()
            if (doc.exists() && doc.data != null) {
                Result.success(FirestoreBusinessProfile.fromMap(doc.id, doc.data!!))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createOrUpdateBusinessProfile(profile: BusinessProfile): Result<Unit> {
        return try {
            val now = getCurrentIsoTimestamp()
            val profileMap = profile.toMap().toMutableMap()
            if (profile.createdAt.isBlank()) {
                profileMap["createdAt"] = now
            }
            profileMap["updatedAt"] = now

            profilesCollection.document(profile.userId)
                .set(profileMap, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTargetPreferences(
        userId: String,
        sectors: List<String>,
        kads: List<String>,
        isPanHellenic: Boolean,
        regions: List<String>,
        prefectures: List<String>,
        legalForms: List<String>
    ): Result<Unit> {
        return try {
            val updates = mapOf(
                "targetSectors" to sectors,
                "targetKads" to kads,
                "isPanHellenic" to isPanHellenic,
                "targetRegions" to regions,
                "targetPrefectures" to prefectures,
                "targetLegalForms" to legalForms,
                "updatedAt" to getCurrentIsoTimestamp()
            )

            profilesCollection.document(userId)
                .set(updates, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNotificationSettings(
        userId: String,
        notificationTime: String,
        dailySummaryEnabled: Boolean,
        emailAlerts: Boolean,
        pushNotifications: Boolean
    ): Result<Unit> {
        return try {
            val updates = mapOf(
                "notificationTime" to notificationTime,
                "dailySummaryEnabled" to dailySummaryEnabled,
                "emailAlerts" to emailAlerts,
                "pushNotifications" to pushNotifications,
                "updatedAt" to getCurrentIsoTimestamp()
            )

            profilesCollection.document(userId)
                .set(updates, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteBusinessProfile(userId: String): Result<Unit> {
        return try {
            profilesCollection.document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 3. User Linking & Joined Account Resolution
    // ==========================================

    override fun getUserWithBusinessProfileFlow(userId: String): Flow<UserAccountWithBusinessProfile?> {
        return combine(
            getUserFlow(userId),
            getBusinessProfileFlow(userId)
        ) { user, businessProfile ->
            if (user != null) {
                UserAccountWithBusinessProfile(user = user, businessProfile = businessProfile)
            } else {
                null
            }
        }
    }

    override suspend fun fetchUserWithBusinessProfile(userId: String): Result<UserAccountWithBusinessProfile?> {
        return try {
            val userResult = fetchUser(userId)
            val user = userResult.getOrNull()
            if (user == null) {
                Result.success(null)
            } else {
                val profileResult = fetchBusinessProfile(userId)
                val businessProfile = profileResult.getOrNull()
                Result.success(UserAccountWithBusinessProfile(user = user, businessProfile = businessProfile))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun linkUserToBusinessProfile(userId: String, profile: BusinessProfile): Result<Unit> {
        return try {
            // Ensure the profile explicitly uses the matching userId
            val linkedProfile = profile.copy(userId = userId)
            createOrUpdateBusinessProfile(linkedProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun initializeNewUserAccount(
        userId: String,
        email: String,
        displayName: String,
        photoUrl: String?,
        initialProfile: BusinessProfile?
    ): Result<UserAccountWithBusinessProfile> {
        return try {
            val now = getCurrentIsoTimestamp()
            val user = FirestoreUser(
                userId = userId,
                email = email,
                displayName = displayName,
                photoUrl = photoUrl,
                status = "ACTIVE",
                createdAt = now,
                lastLoginAt = now
            )

            val profile = (initialProfile ?: FirestoreBusinessProfile()).copy(
                userId = userId,
                createdAt = now,
                updatedAt = now
            )

            val batch = firestore.batch()
            batch.set(usersCollection.document(userId), user.toMap(), SetOptions.merge())
            batch.set(profilesCollection.document(userId), profile.toMap(), SetOptions.merge())
            batch.commit().await()

            Result.success(UserAccountWithBusinessProfile(user = user, businessProfile = profile))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 4. Room Local Database Synchronization
    // ==========================================

    override suspend fun syncRemoteProfileToLocalDb(
        userId: String,
        userProfileDao: UserProfileDao
    ): Result<UserProfileEntity?> {
        return try {
            val profile = fetchBusinessProfile(userId).getOrNull()
            if (profile != null) {
                val localEntity = profile.toLocalEntity()
                userProfileDao.saveUserProfile(localEntity)
                Result.success(localEntity)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncLocalProfileToFirestore(
        userId: String,
        localEntity: UserProfileEntity
    ): Result<Unit> {
        return try {
            val remoteProfile = FirestoreBusinessProfile.fromLocalEntity(localEntity, userId = userId)
            createOrUpdateBusinessProfile(remoteProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
