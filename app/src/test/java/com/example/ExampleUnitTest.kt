package com.example

import com.example.data.local.entity.LeadEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.model.PipelineStatus
import com.example.data.sync.GemiSyncEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testLeadScoringWithProfile() {
    val syncEngine = GemiSyncEngine()
    val profile = UserProfileEntity(
      targetSectors = "HORECA,TOURISM",
      targetKads = "56.10,55.10",
      isPanHellenic = true,
      targetRegions = "Αττική"
    )

    val lead = LeadEntity(
      gemiNumber = "123456789000",
      afm = "800000000",
      companyName = "TEST TAVERNA",
      legalForm = "ΙΚΕ",
      registrationDate = "2026-09-01",
      primaryKad = "56.10",
      kadDescription = "Υπηρεσίες εστιατορίου",
      region = "Αττική",
      sector = "HORECA",
      pipelineStatus = PipelineStatus.NEW
    )

    val scored = syncEngine.scoreLead(lead, profile)
    assertTrue("Match score should be high for matching KAD and region", scored.matchScore >= 80)
    assertTrue(scored.matchReasons.contains("56.10"))
  }
}


