package com.example

import com.example.data.export.GemiCsvExporter
import com.example.data.local.entity.LeadEntity
import com.example.data.model.PipelineStatus
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests validating CSV formatting, escaping, and export functionality.
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun gemiCsvExporter_generatesProperHeadersAndContent() {
        val testLeads = listOf(
            LeadEntity(
                gemiNumber = "123456789000",
                afm = "998877665",
                companyName = "ΕΛΛΗΝΙΚΗ ΕΣΤΙΑΣΗ Ι.Κ.Ε.",
                tradeName = "Greek Bites",
                legalForm = "ΙΚΕ",
                registrationDate = "2026-03-01",
                primaryKad = "56.10",
                kadDescription = "Δραστηριότητες υπηρεσιών εστιατορίων και κινητών μονάδων εστίασης",
                sector = "HORECA",
                region = "Αττική",
                municipality = "Αθηναίων",
                address = "Πανεπιστημίου 10",
                phone = "2101234567",
                email = "info@greekbites.gr",
                website = "https://greekbites.gr",
                matchScore = 95,
                matchReasons = "ΚΑΔ Εστίασης,Περιοχή Αττική",
                pipelineStatus = PipelineStatus.NEW,
                isSaved = true,
                userNotes = "Σημείωση για επικοινωνία"
            )
        )

        val csvString = GemiCsvExporter.buildCsvString(testLeads)

        assertTrue(csvString.contains("Αριθμός ΓΕΜΗ"))
        assertTrue(csvString.contains("123456789000"))
        assertTrue(csvString.contains("ΕΛΛΗΝΙΚΗ ΕΣΤΙΑΣΗ Ι.Κ.Ε."))
        assertTrue(csvString.contains("Greek Bites"))
        assertTrue(csvString.contains("95%"))
        assertTrue(csvString.contains("ΝΑΙ"))
        assertTrue(csvString.contains("Σημείωση για επικοινωνία"))
    }
}

