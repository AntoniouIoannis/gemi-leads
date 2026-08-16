package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.PipelineStatus

@Entity(
    tableName = "leads",
    indices = [
        Index(value = ["gemiNumber"], unique = true),
        Index(value = ["afm"]),
        Index(value = ["primaryKad"]),
        Index(value = ["region"]),
        Index(value = ["isSaved"]),
        Index(value = ["pipelineStatus"])
    ]
)
data class LeadEntity(
    @PrimaryKey
    val gemiNumber: String,
    val afm: String,
    val companyName: String,
    val tradeName: String = "",
    val legalForm: String, // ΙΚΕ, ΑΕ, ΟΕ, ΕΕ, Ατομική, ΕΠΕ
    val registrationDate: String, // YYYY-MM-DD
    val primaryKad: String,
    val kadDescription: String,
    val secondaryKads: String = "", // Comma-separated
    val sector: String, // HORECA, TOURISM, etc.
    val region: String,
    val prefecture: String = "",
    val municipality: String = "",
    val address: String = "",
    val postalCode: String = "",
    val chamberName: String = "",
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    val initialCapital: String = "",
    val administrators: String = "",
    
    // User CRM & Pipeline State
    val isSaved: Boolean = false,
    val savedTimestamp: Long = 0L,
    val pipelineStatus: PipelineStatus = PipelineStatus.NEW,
    val userNotes: String = "",
    val lastContactedDate: String = "",
    val customTags: String = "", // comma-separated tags
    
    // Calculated Relevance Match (populated dynamically or cached)
    val matchScore: Int = 0,
    val matchReasons: String = "", // comma-separated e.g. "ΚΑΔ Εστίασης,Περιοχή Αττική"
    val ingestionTimestamp: Long = System.currentTimeMillis()
)
