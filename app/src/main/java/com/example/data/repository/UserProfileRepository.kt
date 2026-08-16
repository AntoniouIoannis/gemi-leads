package com.example.data.repository

import com.example.data.local.dao.UserProfileDao
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

class UserProfileRepository(private val userProfileDao: UserProfileDao) {

    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getUserProfile()

    suspend fun getProfileOnce(): UserProfileEntity? = userProfileDao.getUserProfileOnce()

    suspend fun saveProfile(profile: UserProfileEntity) {
        userProfileDao.saveUserProfile(profile)
    }

    suspend fun updateTargetPreferences(
        sectors: String,
        kads: String,
        isPanHellenic: Boolean,
        regions: String
    ) {
        val current = userProfileDao.getUserProfileOnce() ?: UserProfileEntity()
        userProfileDao.saveUserProfile(
            current.copy(
                targetSectors = sectors,
                targetKads = kads,
                isPanHellenic = isPanHellenic,
                targetRegions = regions
            )
        )
    }

    suspend fun completeOnboarding(
        companyName: String,
        ownGemi: String,
        businessType: String,
        sectors: String,
        kads: String,
        isPanHellenic: Boolean,
        regions: String,
        clientGoal: String
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
    }
}
