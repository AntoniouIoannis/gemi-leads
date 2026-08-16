package com.example.data.repository

import com.example.data.local.dao.UserProfileDao
import com.example.data.local.entity.UserProfileEntity
import com.example.data.model.BusinessProfile
import com.example.data.model.FirestoreBusinessProfile
import com.example.data.model.FirestoreUser
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

class UserProfileRepository(
    private val userProfileDao: UserProfileDao,
    val firestoreRepo: IFirestoreUserProfileRepository = FirestoreUserProfileRepository(),
    val authRepo: IAuthRepository = FirebaseAuthRepository(userProfileRepo = firestoreRepo)
) {

    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getUserProfile()

    suspend fun getProfileOnce(): UserProfileEntity? = userProfileDao.getUserProfileOnce()

    suspend fun saveProfile(profile: UserProfileEntity, currentUserId: String? = null) {
        userProfileDao.saveUserProfile(profile)
        if (!currentUserId.isNullOrBlank()) {
            firestoreRepo.syncLocalProfileToFirestore(currentUserId, profile)
        }
    }

    suspend fun updateTargetPreferences(
        sectors: String,
        kads: String,
        isPanHellenic: Boolean,
        regions: String,
        currentUserId: String? = null
    ) {
        val current = userProfileDao.getUserProfileOnce() ?: UserProfileEntity()
        val updated = current.copy(
            targetSectors = sectors,
            targetKads = kads,
            isPanHellenic = isPanHellenic,
            targetRegions = regions
        )
        userProfileDao.saveUserProfile(updated)

        if (!currentUserId.isNullOrBlank()) {
            firestoreRepo.syncLocalProfileToFirestore(currentUserId, updated)
        }
    }

    suspend fun completeOnboarding(
        companyName: String,
        ownGemi: String,
        businessType: String,
        sectors: String,
        kads: String,
        isPanHellenic: Boolean,
        regions: String,
        clientGoal: String,
        currentUserId: String? = null,
        userEmail: String? = null
    ) {
        val profile = UserProfileEntity(
            id = 1,
            companyName = companyName,
            ownGemiNumber = ownGemi,
            businessType = businessType,
            targetSectors = sectors,
            targetKads = kads,
            isPanHellenic = isPanHellenic,
            targetRegions = regions,
            clientGoal = clientGoal,
            isOnboardingCompleted = true,
            lastSyncTimestamp = System.currentTimeMillis()
        )
        userProfileDao.saveUserProfile(profile)

        if (!currentUserId.isNullOrBlank()) {
            val businessProfile = FirestoreBusinessProfile.fromLocalEntity(profile, userId = currentUserId)
            if (!userEmail.isNullOrBlank()) {
                firestoreRepo.initializeNewUserAccount(
                    userId = currentUserId,
                    email = userEmail,
                    displayName = companyName,
                    initialProfile = businessProfile
                )
            } else {
                firestoreRepo.createOrUpdateBusinessProfile(businessProfile)
            }
        }
    }

    // --- Direct Firestore Collection Helpers ---

    fun getRemoteUserFlow(userId: String): Flow<User?> = firestoreRepo.getUserFlow(userId)

    fun getRemoteBusinessProfileFlow(userId: String): Flow<BusinessProfile?> =
        firestoreRepo.getBusinessProfileFlow(userId)

    fun getUserWithBusinessProfileFlow(userId: String): Flow<UserAccountWithBusinessProfile?> =
        firestoreRepo.getUserWithBusinessProfileFlow(userId)

    suspend fun fetchRemoteUserWithBusinessProfile(userId: String): Result<UserAccountWithBusinessProfile?> =
        firestoreRepo.fetchUserWithBusinessProfile(userId)

    suspend fun syncRemoteProfileToLocal(userId: String): Result<UserProfileEntity?> =
        firestoreRepo.syncRemoteProfileToLocalDb(userId, userProfileDao)
}
