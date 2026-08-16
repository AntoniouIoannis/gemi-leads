package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.LeadEntity
import com.example.data.model.PipelineStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface LeadDao {
    @Query("SELECT * FROM leads ORDER BY registrationDate DESC, gemiNumber DESC")
    fun getAllLeads(): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads WHERE isSaved = 1 ORDER BY savedTimestamp DESC")
    fun getSavedLeads(): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads WHERE pipelineStatus != :excludeStatus ORDER BY registrationDate DESC")
    fun getPipelineLeads(excludeStatus: PipelineStatus = PipelineStatus.NEW): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads WHERE gemiNumber = :gemiNumber LIMIT 1")
    suspend fun getLeadByGemi(gemiNumber: String): LeadEntity?

    @Query("""
        SELECT * FROM leads 
        WHERE (companyName LIKE '%' || :query || '%' 
               OR tradeName LIKE '%' || :query || '%' 
               OR gemiNumber LIKE '%' || :query || '%' 
               OR afm LIKE '%' || :query || '%'
               OR kadDescription LIKE '%' || :query || '%'
               OR municipality LIKE '%' || :query || '%'
               OR region LIKE '%' || :query || '%')
        ORDER BY registrationDate DESC
    """)
    fun searchLeads(query: String): Flow<List<LeadEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLeads(leads: List<LeadEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLead(lead: LeadEntity)

    @Update
    suspend fun updateLead(lead: LeadEntity)

    @Query("UPDATE leads SET isSaved = :isSaved, savedTimestamp = :timestamp WHERE gemiNumber = :gemiNumber")
    suspend fun updateSavedStatus(gemiNumber: String, isSaved: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE leads SET pipelineStatus = :status, lastContactedDate = :contactedDate WHERE gemiNumber = :gemiNumber")
    suspend fun updatePipelineStatus(gemiNumber: String, status: PipelineStatus, contactedDate: String = "")

    @Query("UPDATE leads SET userNotes = :notes WHERE gemiNumber = :gemiNumber")
    suspend fun updateNotes(gemiNumber: String, notes: String)

    @Query("SELECT COUNT(*) FROM leads")
    fun getLeadCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM leads WHERE isSaved = 1")
    fun getSavedCount(): Flow<Int>
}
