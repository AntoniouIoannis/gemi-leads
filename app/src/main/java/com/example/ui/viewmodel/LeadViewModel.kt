package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.LeadEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.model.PipelineStatus
import com.example.data.notification.GemiNotificationManager
import com.example.data.repository.LeadRepository
import com.example.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private fun stripGreekAccents(str: String): String {
    return str
        .replace('ά', 'α').replace('έ', 'ε').replace('ή', 'η').replace('ί', 'ι')
        .replace('ό', 'ο').replace('ύ', 'υ').replace('ώ', 'ω').replace('ϊ', 'ι')
        .replace('ϋ', 'υ').replace('ΐ', 'ι').replace('ΰ', 'υ')
        .replace('Ά', 'Α').replace('Έ', 'Ε').replace('Ή', 'Η').replace('Ί', 'Ι')
        .replace('Ό', 'Ο').replace('Ύ', 'Υ').replace('Ώ', 'Ω')
        .trim()
}

class LeadViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val userProfileRepo = UserProfileRepository(db.userProfileDao())
    private val leadRepo = LeadRepository(db.leadDao())

    private val defaultInitialProfile = UserProfileEntity(
        id = 1,
        companyName = "Hellas Commercial Equipment Ltd",
        ownGemiNumber = "154892010000",
        businessType = "Προμηθευτής Επαγγελματικού Εξοπλισμού Εστίασης & POS",
        targetSectors = "HORECA,TOURISM,RETAIL",
        targetKads = "56.10,56.30,56.21,55.10,55.20,46.90,47.11",
        isPanHellenic = true,
        targetRegions = "Αττική,Κεντρική Μακεδονία,Κρήτη,Νότιο Αιγαίο",
        clientGoal = "Νέα καταστήματα εστίασης, καφέ-μπαρ, ξενοδοχεία και εμπορικά καταστήματα",
        isOnboardingCompleted = true,
        lastSyncTimestamp = System.currentTimeMillis()
    )

    val userProfile: StateFlow<UserProfileEntity?> = userProfileRepo.userProfile
        .stateIn(viewModelScope, SharingStarted.Eagerly, defaultInitialProfile)

    val allLeadsRaw: StateFlow<List<LeadEntity>> = leadRepo.allLeads
        .stateIn(viewModelScope, SharingStarted.Eagerly, com.example.data.seed.GemiSeedData.INITIAL_LEADS)

    val savedLeads: StateFlow<List<LeadEntity>> = leadRepo.savedLeads
        .stateIn(viewModelScope, SharingStarted.Eagerly, com.example.data.seed.GemiSeedData.INITIAL_LEADS.filter { it.isSaved })

    // Search and Filters for All Feed
    val searchQuery = MutableStateFlow("")
    val selectedLegalFormFilter = MutableStateFlow<String?>(null)
    val selectedRegionFilter = MutableStateFlow<String?>(null)
    val selectedSectorFilter = MutableStateFlow<String?>(null)

    // Filtered All Leads with Greek accent-insensitive search
    val filteredAllLeads: StateFlow<List<LeadEntity>> = combine(
        allLeadsRaw,
        searchQuery,
        selectedLegalFormFilter,
        selectedRegionFilter,
        selectedSectorFilter
    ) { leads, query, legalForm, region, sector ->
        val qNorm = stripGreekAccents(query)
        leads.filter { lead ->
            val matchesQuery = qNorm.isBlank() ||
                stripGreekAccents(lead.companyName).contains(qNorm, ignoreCase = true) ||
                stripGreekAccents(lead.tradeName).contains(qNorm, ignoreCase = true) ||
                lead.gemiNumber.contains(qNorm, ignoreCase = true) ||
                lead.afm.contains(qNorm, ignoreCase = true) ||
                stripGreekAccents(lead.kadDescription).contains(qNorm, ignoreCase = true) ||
                stripGreekAccents(lead.municipality).contains(qNorm, ignoreCase = true) ||
                stripGreekAccents(lead.region).contains(qNorm, ignoreCase = true)

            val matchesLegalForm = legalForm == null || lead.legalForm.equals(legalForm, ignoreCase = true)
            val matchesRegion = region == null || lead.region.contains(region, ignoreCase = true)
            val matchesSector = sector == null || lead.sector.equals(sector, ignoreCase = true)

            matchesQuery && matchesLegalForm && matchesRegion && matchesSector
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Matched Leads (Sorted by match score with fallback)
    val matchedLeads: StateFlow<List<LeadEntity>> = allLeadsRaw.combine(searchQuery) { leads, query ->
        val qNorm = stripGreekAccents(query)
        val filtered = leads.filter { lead ->
            qNorm.isBlank() ||
                stripGreekAccents(lead.companyName).contains(qNorm, ignoreCase = true) ||
                stripGreekAccents(lead.tradeName).contains(qNorm, ignoreCase = true) ||
                stripGreekAccents(lead.kadDescription).contains(qNorm, ignoreCase = true) ||
                stripGreekAccents(lead.region).contains(qNorm, ignoreCase = true)
        }
        val highMatches = filtered.filter { it.matchScore >= 45 }.sortedByDescending { it.matchScore }
        if (highMatches.isNotEmpty()) highMatches else filtered.sortedByDescending { it.matchScore }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pipeline Leads
    val pipelineLeads: StateFlow<List<LeadEntity>> = allLeadsRaw.map { list ->
        list.filter { it.pipelineStatus != PipelineStatus.NEW || it.isSaved }
            .sortedByDescending { it.savedTimestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Navigation & Dialog States
    val selectedTab = MutableStateFlow(0) // 0: Matches, 1: Live Feed, 2: Pipeline CRM, 3: Settings
    val selectedLeadDetail = MutableStateFlow<LeadEntity?>(null)
    val isSyncing = MutableStateFlow(false)
    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()
    val showBackendArchitectureModal = MutableStateFlow(false)

    init {
        // Initial sync to ensure seed/live data is immediately populated
        viewModelScope.launch {
            try {
                val profile = userProfileRepo.ensureDefaultProfile()
                leadRepo.ensureSeeded(profile)
                leadRepo.syncLeads(profile)
            } catch (e: Throwable) {
                // Ensure local seed data is always intact
            }
        }
    }

    fun completeOnboarding(
        companyName: String,
        ownGemi: String,
        businessType: String,
        sectors: String,
        kads: String,
        isPanHellenic: Boolean,
        regions: String,
        clientGoal: String
    ) {
        viewModelScope.launch {
            userProfileRepo.completeOnboarding(
                companyName = companyName,
                ownGemi = ownGemi,
                businessType = businessType,
                sectors = sectors,
                kads = kads,
                isPanHellenic = isPanHellenic,
                regions = regions,
                clientGoal = clientGoal
            )
            val updated = userProfileRepo.getProfileOnce()
            leadRepo.rescoreAll(updated)
            leadRepo.syncLeads(updated)
        }
    }

    fun updateTargetPreferences(
        sectors: String,
        kads: String,
        isPanHellenic: Boolean,
        regions: String
    ) {
        viewModelScope.launch {
            userProfileRepo.updateTargetPreferences(sectors, kads, isPanHellenic, regions)
            val updated = userProfileRepo.getProfileOnce()
            leadRepo.rescoreAll(updated)
        }
    }

    fun toggleSaveLead(lead: LeadEntity) {
        viewModelScope.launch {
            leadRepo.toggleSaved(lead.gemiNumber, lead.isSaved)
            // Update modal state if open
            if (selectedLeadDetail.value?.gemiNumber == lead.gemiNumber) {
                selectedLeadDetail.value = lead.copy(isSaved = !lead.isSaved)
            }
        }
    }

    fun updateLeadStatus(lead: LeadEntity, status: PipelineStatus, contactedDate: String = "") {
        viewModelScope.launch {
            leadRepo.updateStatus(lead.gemiNumber, status, contactedDate)
            if (selectedLeadDetail.value?.gemiNumber == lead.gemiNumber) {
                selectedLeadDetail.value = lead.copy(pipelineStatus = status, lastContactedDate = contactedDate)
            }
        }
    }

    fun updateLeadNotes(lead: LeadEntity, notes: String) {
        viewModelScope.launch {
            leadRepo.updateNotes(lead.gemiNumber, notes)
            if (selectedLeadDetail.value?.gemiNumber == lead.gemiNumber) {
                selectedLeadDetail.value = lead.copy(userNotes = notes)
            }
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            isSyncing.value = true
            _syncMessage.value = "Σύνδεση με Open Data Γ.Ε.ΜΗ. (όριο 8 req/min)..."
            val profile = userProfileRepo.getProfileOnce()
            val result = leadRepo.syncLeads(profile)
            _syncMessage.value = result.message
            isSyncing.value = false

            // If new matching leads arrived, trigger daily notification
            if (result.success && result.newLeadsCount > 0) {
                val matches = allLeadsRaw.value.filter { it.matchScore >= 50 }
                if (matches.isNotEmpty()) {
                    GemiNotificationManager.showDailyLeadMatchNotification(
                        context = getApplication(),
                        matchingLeads = matches,
                        userProfile = profile
                    )
                }
            }
        }
    }

    fun triggerTestNotification() {
        viewModelScope.launch {
            val profile = userProfileRepo.getProfileOnce()
            val matches = matchedLeads.value.ifEmpty { allLeadsRaw.value.take(3) }
            if (matches.isNotEmpty()) {
                GemiNotificationManager.showDailyLeadMatchNotification(
                    context = getApplication(),
                    matchingLeads = matches,
                    userProfile = profile
                )
                _syncMessage.value = "Εστάλη δοκιμαστική Push ειδοποίηση με ${matches.size} Leads!"
            } else {
                _syncMessage.value = "Δεν βρέθηκαν διαθέσιμα Leads για δοκιμαστική ειδοποίηση."
            }
        }
    }

    fun findAndSelectLeadByGemiNumber(gemiNumber: String) {
        viewModelScope.launch {
            val lead = allLeadsRaw.value.firstOrNull { it.gemiNumber == gemiNumber }
                ?: leadRepo.allLeads.firstOrNull()?.firstOrNull { it.gemiNumber == gemiNumber }
            if (lead != null) {
                selectedLeadDetail.value = lead
            }
        }
    }

    fun clearSyncMessage() {
        _syncMessage.value = null
    }

    fun exportCsv(leads: List<LeadEntity>): String {
        return leadRepo.exportLeadsToCsv(leads)
    }

    fun selectLeadForDetail(lead: LeadEntity?) {
        selectedLeadDetail.value = lead
    }

    fun clearFilters() {
        searchQuery.value = ""
        selectedLegalFormFilter.value = null
        selectedRegionFilter.value = null
        selectedSectorFilter.value = null
    }
}
