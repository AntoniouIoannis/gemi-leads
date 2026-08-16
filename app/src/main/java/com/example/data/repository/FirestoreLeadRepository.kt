package com.example.data.repository

import com.example.data.local.dao.LeadDao
import com.example.data.local.entity.LeadEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.model.BusinessProfile
import com.example.data.model.FirestoreBusinessProfile
import com.example.data.model.FirestoreCollections
import com.example.data.model.FirestoreGemiDailyLead
import com.example.data.model.GemiLead
import com.example.data.sync.SyncResult
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Filter parameters for querying the 'All Greece' feed.
 */
data class LeadFeedFilter(
    val query: String = "",
    val region: String? = null,
    val sector: String? = null,
    val legalForm: String? = null,
    val primaryKad: String? = null,
    val registrationDateFrom: String? = null,
    val registrationDateTo: String? = null,
    val limit: Long = 100
)

/**
 * Match evaluation result with detailed scoring breakdown.
 */
data class MatchEvaluation(
    val score: Int,
    val isPrimaryKadMatch: Boolean,
    val isSecondaryKadMatch: Boolean,
    val isRegionMatch: Boolean,
    val isSectorMatch: Boolean,
    val isLegalFormMatch: Boolean,
    val reasons: List<String>
) {
    val formattedReasons: String
        get() = reasons.joinToString(" • ")
}

/**
 * Result wrapper for GemiLead with evaluated matching score.
 */
data class MatchedGemiLead(
    val lead: GemiLead,
    val evaluation: MatchEvaluation
)

/**
 * Repository interface for Firestore-based GEMI Lead operations.
 */
interface IFirestoreLeadRepository {
    // All Greece Feed
    fun getAllGreeceLeadsFlow(filter: LeadFeedFilter = LeadFeedFilter()): Flow<List<GemiLead>>
    suspend fun fetchAllGreeceLeads(filter: LeadFeedFilter = LeadFeedFilter()): Result<List<GemiLead>>

    // My Matches Feed
    fun getMyMatchesFlow(profile: BusinessProfile, minScore: Int = 45): Flow<List<MatchedGemiLead>>
    suspend fun fetchMyMatches(profile: BusinessProfile, minScore: Int = 45): Result<List<MatchedGemiLead>>

    // Targeted Queries
    fun getLeadsByRegionFlow(region: String): Flow<List<GemiLead>>
    fun getLeadsByKadFlow(kadCodes: List<String>): Flow<List<GemiLead>>
    fun getLeadsByDateFlow(date: String): Flow<List<GemiLead>>
    suspend fun getLeadByGemiNumber(gemiNumber: String): Result<GemiLead?>

    // Sync & Persistence
    suspend fun syncFirestoreToLocalDb(leadDao: LeadDao, userProfile: UserProfileEntity?): SyncResult
    suspend fun publishLeadToFirestore(lead: GemiLead): Result<Unit>
    suspend fun publishBatchLeadsToFirestore(leads: List<GemiLead>): Result<Int>

    // Match Evaluation Algorithm
    fun evaluateLeadMatch(lead: GemiLead, profile: BusinessProfile): MatchEvaluation
}

/**
 * Production Repository implementation interacting with Cloud Firestore.
 */
class FirestoreLeadRepository() : IFirestoreLeadRepository {

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val leadsCollection by lazy { firestore?.collection(FirestoreCollections.GEMI_DAILY_LEADS) }
    private val profilesCollection by lazy { firestore?.collection(FirestoreCollections.BUSINESS_PROFILES) }

    /**
     * Real-time stream of all Greece leads matching the specified filters.
     */
    override fun getAllGreeceLeadsFlow(filter: LeadFeedFilter): Flow<List<GemiLead>> = callbackFlow {
        if (leadsCollection == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        var query: Query = leadsCollection!!

        // Server-side Firestore indexed filtering where possible
        if (!filter.region.isNullOrBlank() && filter.region != "Όλη η Ελλάδα") {
            query = query.whereEqualTo("region", filter.region)
        }
        if (!filter.sector.isNullOrBlank() && filter.sector != "Όλοι οι Κλάδοι") {
            query = query.whereEqualTo("sector", filter.sector)
        }
        if (!filter.legalForm.isNullOrBlank() && filter.legalForm != "Όλες") {
            query = query.whereEqualTo("legalForm", filter.legalForm)
        }
        if (!filter.primaryKad.isNullOrBlank()) {
            query = query.whereEqualTo("primaryKad", filter.primaryKad)
        }
        if (!filter.registrationDateFrom.isNullOrBlank()) {
            query = query.whereGreaterThanOrEqualTo("registrationDate", filter.registrationDateFrom)
        }

        // Limit results
        query = query.limit(filter.limit)

        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val leads = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { data -> FirestoreGemiDailyLead.fromMap(doc.id, data) }
                }

                // Client-side text search filtering
                val filtered = if (filter.query.isNotBlank()) {
                    val q = filter.query.trim().lowercase()
                    leads.filter { lead ->
                        lead.companyName.lowercase().contains(q) ||
                            lead.tradeName.lowercase().contains(q) ||
                            lead.gemiNumber.contains(q) ||
                            lead.afm.contains(q) ||
                            lead.kadDescription.lowercase().contains(q) ||
                            lead.municipality.lowercase().contains(q) ||
                            lead.region.lowercase().contains(q)
                    }
                } else {
                    leads
                }

                trySend(filtered)
            }
        }

        awaitClose { listenerRegistration.remove() }
    }

    /**
     * One-shot fetch of All Greece leads with filtering.
     */
    override suspend fun fetchAllGreeceLeads(filter: LeadFeedFilter): Result<List<GemiLead>> {
        return try {
            if (leadsCollection == null) return Result.success(emptyList())
            var query: Query = leadsCollection!!

            if (!filter.region.isNullOrBlank() && filter.region != "Όλη η Ελλάδα") {
                query = query.whereEqualTo("region", filter.region)
            }
            if (!filter.sector.isNullOrBlank() && filter.sector != "Όλοι οι Κλάδοι") {
                query = query.whereEqualTo("sector", filter.sector)
            }
            if (!filter.legalForm.isNullOrBlank() && filter.legalForm != "Όλες") {
                query = query.whereEqualTo("legalForm", filter.legalForm)
            }
            if (!filter.primaryKad.isNullOrBlank()) {
                query = query.whereEqualTo("primaryKad", filter.primaryKad)
            }

            query = query.limit(filter.limit)

            val snapshot = query.get().await()
            val leads = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { data -> FirestoreGemiDailyLead.fromMap(doc.id, data) }
            }

            val filtered = if (filter.query.isNotBlank()) {
                val q = filter.query.trim().lowercase()
                leads.filter { lead ->
                    lead.companyName.lowercase().contains(q) ||
                        lead.tradeName.lowercase().contains(q) ||
                        lead.gemiNumber.contains(q) ||
                        lead.afm.contains(q) ||
                        lead.kadDescription.lowercase().contains(q) ||
                        lead.municipality.lowercase().contains(q) ||
                        lead.region.lowercase().contains(q)
                }
            } else {
                leads
            }

            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Real-time stream for 'My Matches' feed, filtered and scored dynamically against the user's BusinessProfile.
     */
    override fun getMyMatchesFlow(profile: BusinessProfile, minScore: Int): Flow<List<MatchedGemiLead>> {
        return getAllGreeceLeadsFlow(LeadFeedFilter(limit = 200))
            .map { leads ->
                leads.map { lead ->
                    val evaluation = evaluateLeadMatch(lead, profile)
                    MatchedGemiLead(lead, evaluation)
                }
                .filter { it.evaluation.score >= minScore }
                .sortedByDescending { it.evaluation.score }
            }
    }

    /**
     * One-shot fetch for 'My Matches' feed.
     */
    override suspend fun fetchMyMatches(profile: BusinessProfile, minScore: Int): Result<List<MatchedGemiLead>> {
        return try {
            val allLeadsResult = fetchAllGreeceLeads(LeadFeedFilter(limit = 200))
            allLeadsResult.map { leads ->
                leads.map { lead ->
                    val evaluation = evaluateLeadMatch(lead, profile)
                    MatchedGemiLead(lead, evaluation)
                }
                .filter { it.evaluation.score >= minScore }
                .sortedByDescending { it.evaluation.score }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Queries leads by specific Region name.
     */
    override fun getLeadsByRegionFlow(region: String): Flow<List<GemiLead>> {
        return getAllGreeceLeadsFlow(LeadFeedFilter(region = region))
    }

    /**
     * Queries leads matching any of the specified KAD codes.
     */
    override fun getLeadsByKadFlow(kadCodes: List<String>): Flow<List<GemiLead>> = callbackFlow {
        if (kadCodes.isEmpty() || leadsCollection == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        // Firestore supports 'whereIn' for up to 10 items
        val chunks = kadCodes.take(10)
        val query = leadsCollection!!.whereIn("primaryKad", chunks)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.documents?.mapNotNull { doc ->
                doc.data?.let { FirestoreGemiDailyLead.fromMap(doc.id, it) }
            } ?: emptyList()
            trySend(items)
        }

        awaitClose { registration.remove() }
    }

    /**
     * Queries leads for a specific registration date (YYYY-MM-DD).
     */
    override fun getLeadsByDateFlow(date: String): Flow<List<GemiLead>> = callbackFlow {
        if (leadsCollection == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val query = leadsCollection!!.whereEqualTo("registrationDate", date)
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.documents?.mapNotNull { doc ->
                doc.data?.let { FirestoreGemiDailyLead.fromMap(doc.id, it) }
            } ?: emptyList()
            trySend(items)
        }
        awaitClose { registration.remove() }
    }

    /**
     * Retrieves single company lead document by GEMI number.
     */
    override suspend fun getLeadByGemiNumber(gemiNumber: String): Result<GemiLead?> {
        return try {
            if (leadsCollection == null) return Result.success(null)
            val doc = leadsCollection!!.document(gemiNumber).get().await()
            if (doc.exists() && doc.data != null) {
                Result.success(FirestoreGemiDailyLead.fromMap(doc.id, doc.data!!))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Evaluates lead relevance against user's BusinessProfile using the weighted algorithm:
     * - Primary KAD match: +40 pts
     * - Secondary KAD match: +20 pts
     * - Regional match (or Pan-Hellenic): +25 pts
     * - Sector match: +15 pts
     * - Legal Form match: +10 pts
     */
    override fun evaluateLeadMatch(lead: GemiLead, profile: BusinessProfile): MatchEvaluation {
        var score = 0
        val reasons = mutableListOf<String>()

        val userKads = profile.targetKads.map { it.trim() }.filter { it.isNotEmpty() }
        val userSectors = profile.targetSectors.map { it.trim().uppercase() }.filter { it.isNotEmpty() }
        val userRegions = profile.targetRegions.map { it.trim() }.filter { it.isNotEmpty() }
        val userLegalForms = profile.targetLegalForms.map { it.trim().uppercase() }.filter { it.isNotEmpty() }

        // 1. Primary KAD Match (+40)
        val isPrimaryKadMatch = userKads.any { kad ->
            lead.primaryKad.startsWith(kad) || kad.startsWith(lead.primaryKad)
        }
        if (isPrimaryKadMatch) {
            score += 40
            reasons.add("Κύριος ΚΑΔ ${lead.primaryKad} (+40)")
        }

        // 2. Secondary KAD Match (+20)
        val isSecondaryKadMatch = lead.secondaryKads.any { secKad ->
            userKads.any { targetKad -> secKad.startsWith(targetKad) || targetKad.startsWith(secKad) }
        }
        if (isSecondaryKadMatch && !isPrimaryKadMatch) {
            score += 20
            reasons.add("Δευτερεύων ΚΑΔ (+20)")
        }

        // 3. Geographic / Region Match (+25)
        val isRegionMatch = profile.isPanHellenic || userRegions.any { reg ->
            lead.region.contains(reg, ignoreCase = true) || reg.contains(lead.region, ignoreCase = true)
        }
        if (isRegionMatch) {
            score += 25
            if (profile.isPanHellenic) {
                reasons.add("Πανελλαδική Κάλυψη (+25)")
            } else {
                reasons.add("Περιφέρεια ${lead.region} (+25)")
            }
        }

        // 4. Sector Match (+15)
        val isSectorMatch = userSectors.any { sec ->
            lead.sector.uppercase().contains(sec) || sec.contains(lead.sector.uppercase())
        }
        if (isSectorMatch) {
            score += 15
            reasons.add("Κλάδος ${lead.sector} (+15)")
        }

        // 5. Legal Form Match (+10)
        val isLegalFormMatch = userLegalForms.any { form ->
            lead.legalForm.uppercase().contains(form)
        }
        if (isLegalFormMatch) {
            score += 10
            reasons.add("Νομική Μορφή ${lead.legalForm} (+10)")
        }

        val finalScore = score.coerceIn(0, 100)
        return MatchEvaluation(
            score = finalScore,
            isPrimaryKadMatch = isPrimaryKadMatch,
            isSecondaryKadMatch = isSecondaryKadMatch,
            isRegionMatch = isRegionMatch,
            isSectorMatch = isSectorMatch,
            isLegalFormMatch = isLegalFormMatch,
            reasons = reasons
        )
    }

    /**
     * Syncs latest leads from Firestore down into Room local cache for fast offline access.
     */
    override suspend fun syncFirestoreToLocalDb(
        leadDao: LeadDao,
        userProfile: UserProfileEntity?
    ): SyncResult {
        return try {
            if (leadsCollection == null) {
                return SyncResult(success = false, newLeadsCount = 0, totalLeadsCount = 0, message = "Firestore not initialized")
            }
            val snapshot = leadsCollection!!
                .orderBy("ingestedAt", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .await()

            val businessProfile = userProfile?.let {
                FirestoreBusinessProfile.fromLocalEntity(it)
            } ?: FirestoreBusinessProfile()

            var newLeadsCount = 0
            val entitiesToInsert = mutableListOf<LeadEntity>()

            for (doc in snapshot.documents) {
                val data = doc.data ?: continue
                val remoteLead = FirestoreGemiDailyLead.fromMap(doc.id, data)
                val evaluation = evaluateLeadMatch(remoteLead, businessProfile)

                val entity = remoteLead.toLocalEntity(
                    matchScore = evaluation.score,
                    matchReasons = evaluation.formattedReasons
                )
                entitiesToInsert.add(entity)
                newLeadsCount++
            }

            if (entitiesToInsert.isNotEmpty()) {
                leadDao.insertLeads(entitiesToInsert)
            }

            val total = leadDao.getLeadCountSync()
            SyncResult(
                success = true,
                newLeadsCount = newLeadsCount,
                totalLeadsCount = total,
                message = "Συγχρονίστηκαν $newLeadsCount νέες επιχειρήσεις από το Cloud Firestore"
            )
        } catch (e: Exception) {
            SyncResult(
                success = false,
                newLeadsCount = 0,
                totalLeadsCount = 0,
                message = "Αποτυχία συγχρονισμού Firestore: ${e.localizedMessage ?: "Σφάλμα δικτύου"}"
            )
        }
    }

    /**
     * Publishes a single GemiLead to Firestore.
     */
    override suspend fun publishLeadToFirestore(lead: GemiLead): Result<Unit> {
        return try {
            if (leadsCollection == null) return Result.failure(Exception("Firestore not initialized"))
            leadsCollection!!.document(lead.gemiNumber).set(lead.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Batch uploads leads into Firestore.
     */
    override suspend fun publishBatchLeadsToFirestore(leads: List<GemiLead>): Result<Int> {
        return try {
            if (firestore == null || leadsCollection == null) return Result.failure(Exception("Firestore not initialized"))
            var count = 0
            val batch = firestore!!.batch()
            for (lead in leads) {
                val ref = leadsCollection!!.document(lead.gemiNumber)
                batch.set(ref, lead.toMap())
                count++
            }
            batch.commit().await()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
