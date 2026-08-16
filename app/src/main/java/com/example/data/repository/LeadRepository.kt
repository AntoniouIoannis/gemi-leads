package com.example.data.repository

import com.example.data.local.dao.LeadDao
import com.example.data.local.entity.LeadEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.model.GemiLead
import com.example.data.model.PipelineStatus
import com.example.data.sync.GemiSyncEngine
import com.example.data.sync.SyncResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class LeadRepository(
    private val leadDao: LeadDao,
    private val syncEngine: GemiSyncEngine = GemiSyncEngine(),
    val firestoreRepo: IFirestoreLeadRepository = FirestoreLeadRepository()
) {
    val allLeads: Flow<List<LeadEntity>> = leadDao.getAllLeads()
    val savedLeads: Flow<List<LeadEntity>> = leadDao.getSavedLeads()
    val pipelineLeads: Flow<List<LeadEntity>> = leadDao.getPipelineLeads()
    val totalLeadsCount: Flow<Int> = leadDao.getLeadCount()
    val savedCount: Flow<Int> = leadDao.getSavedCount()

    fun getMatchedLeads(profileFlow: Flow<UserProfileEntity?>): Flow<List<LeadEntity>> {
        return allLeads.map { list ->
            list.filter { it.matchScore >= 50 }
                .sortedByDescending { it.matchScore }
        }
    }

    fun searchLeads(query: String): Flow<List<LeadEntity>> {
        return leadDao.searchLeads(query)
    }

    /**
     * Real-time stream of 'All Greece' leads directly from Firestore.
     */
    fun getFirestoreAllGreeceFeed(filter: LeadFeedFilter = LeadFeedFilter()): Flow<List<GemiLead>> {
        return firestoreRepo.getAllGreeceLeadsFlow(filter)
    }

    /**
     * Real-time stream of 'My Matches' leads directly from Firestore.
     */
    fun getFirestoreMyMatchesFeed(profile: UserProfileEntity, minScore: Int = 45): Flow<List<MatchedGemiLead>> {
        val businessProfile = com.example.data.model.FirestoreBusinessProfile.fromLocalEntity(profile)
        return firestoreRepo.getMyMatchesFlow(businessProfile, minScore)
    }

    suspend fun toggleSaved(gemiNumber: String, isCurrentlySaved: Boolean) {
        leadDao.updateSavedStatus(gemiNumber, !isCurrentlySaved)
    }

    suspend fun updateStatus(gemiNumber: String, status: PipelineStatus, contactedDate: String = "") {
        leadDao.updatePipelineStatus(gemiNumber, status, contactedDate)
    }

    suspend fun updateNotes(gemiNumber: String, notes: String) {
        leadDao.updateNotes(gemiNumber, notes)
    }

    suspend fun syncLeads(profile: UserProfileEntity?): SyncResult {
        // Attempt cloud Firestore synchronization first
        val firestoreSyncResult = firestoreRepo.syncFirestoreToLocalDb(leadDao, profile)
        if (firestoreSyncResult.success && firestoreSyncResult.newLeadsCount > 0) {
            return firestoreSyncResult
        }

        // Fallback or augment with GEMI Open Data API ingestion
        val current = leadDao.getAllLeads().firstOrNull() ?: emptyList()
        val (incoming, result) = syncEngine.fetchLeadsFromGemi(current, profile)
        if (incoming.isNotEmpty()) {
            leadDao.insertLeads(incoming)
        }
        // Rescore existing leads based on updated profile
        current.forEach { lead ->
            val updated = syncEngine.scoreLead(lead, profile)
            if (updated.matchScore != lead.matchScore || updated.matchReasons != lead.matchReasons) {
                leadDao.updateLead(updated)
            }
        }
        return result
    }

    suspend fun rescoreAll(profile: UserProfileEntity?) {
        val current = leadDao.getAllLeads().firstOrNull() ?: emptyList()
        current.forEach { lead ->
            val updated = syncEngine.scoreLead(lead, profile)
            leadDao.updateLead(updated)
        }
    }

    fun exportLeadsToCsv(leads: List<LeadEntity>): String {
        return com.example.data.export.GemiCsvExporter.buildCsvString(leads)
    }
}
