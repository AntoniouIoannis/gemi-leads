package com.example.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.entity.LeadEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Robust CSV export utility for GEMI leads supporting standard Android file access,
 * FileProvider sharing, Storage Access Framework (SAF), and Greek UTF-8 BOM encoding for Excel.
 */
object GemiCsvExporter {

    // UTF-8 Byte Order Mark (BOM) ensuring Greek characters render flawlessly in Excel / LibreOffice
    private val UTF8_BOM = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())

    /**
     * Generates a well-formatted CSV string with full lead metadata and escaped quotes.
     */
    fun buildCsvString(leads: List<LeadEntity>): String {
        val sb = StringBuilder()

        // Headers in Greek
        val headers = listOf(
            "Αριθμός ΓΕΜΗ",
            "ΑΦΜ",
            "Επωνυμία",
            "Διακριτικός Τίτλος",
            "Νομική Μορφή",
            "Κύριος ΚΑΔ",
            "Περιγραφή ΚΑΔ",
            "Κλάδος",
            "Ημερομηνία Σύστασης",
            "Περιφέρεια",
            "Δήμος",
            "Διεύθυνση",
            "Τηλέφωνο",
            "Email",
            "Ιστοσελίδα",
            "Match Score (%)",
            "Αιτίες Συνάφειας",
            "Κατάσταση CRM",
            "Ημερομηνία Επαφής",
            "Αποθηκευμένο",
            "Σημειώσεις Χρήστη"
        )

        sb.append(headers.joinToString(separator = ",", postfix = "\r\n") { escapeCsv(it) })

        leads.forEach { lead ->
            val row = listOf(
                lead.gemiNumber,
                lead.afm,
                lead.companyName,
                lead.tradeName,
                lead.legalForm,
                lead.primaryKad,
                lead.kadDescription,
                lead.sector,
                lead.registrationDate,
                lead.region,
                lead.municipality,
                lead.address,
                lead.phone,
                lead.email,
                lead.website,
                "${lead.matchScore}%",
                lead.matchReasons,
                lead.pipelineStatus.labelGr,
                lead.lastContactedDate,
                if (lead.isSaved) "ΝΑΙ" else "ΟΧΙ",
                lead.userNotes
            )
            sb.append(row.joinToString(separator = ",", postfix = "\r\n") { escapeCsv(it) })
        }

        return sb.toString()
    }

    /**
     * Writes CSV data directly to a file uri opened via ContentResolver (SAF).
     */
    fun writeCsvToUri(context: Context, destinationUri: Uri, leads: List<LeadEntity>): Result<Int> {
        return try {
            val csvContent = buildCsvString(leads)
            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                // Write UTF-8 BOM
                outputStream.write(UTF8_BOM)
                OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { writer ->
                    writer.write(csvContent)
                    writer.flush()
                }
            } ?: throw IllegalStateException("Αδυναμία εγγραφής στο επιλεγμένο αρχείο")
            Result.success(leads.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generates a temporary cached CSV file and provides a content:// URI via FileProvider
     * for standard Android sharing / export to other apps (Drive, Gmail, WhatsApp, Files, etc.).
     */
    fun exportToCacheFile(context: Context, leads: List<LeadEntity>, prefix: String = "gemi_leads"): Result<Uri> {
        return try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(exportDir, "${prefix}_${timestamp}.csv")

            FileOutputStream(file).use { fos ->
                fos.write(UTF8_BOM)
                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                    writer.write(buildCsvString(leads))
                    writer.flush()
                }
            }

            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, file)
            Result.success(contentUri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Launches standard Android Share Sheet to export or save the generated CSV file.
     */
    fun shareCsvFile(
        context: Context,
        leads: List<LeadEntity>,
        chooserTitle: String = "Εξαγωγή Leads σε CSV",
        filePrefix: String = "gemi_leads"
    ): Result<Unit> {
        if (leads.isEmpty()) {
            return Result.failure(IllegalArgumentException("Δεν υπάρχουν διαθέσιμα leads προς εξαγωγή"))
        }

        return exportToCacheFile(context, leads, filePrefix).map { uri ->
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Εξαγωγή Leads Γ.Ε.ΜΗ. (${leads.size} επιχειρήσεις)")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Εξαγωγή ${leads.size} νέων επιχειρήσεων Γ.Ε.ΜΗ. σε μορφή CSV (UTF-8 με Ελληνική κωδικοποίηση)."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(sendIntent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooserIntent)
        }
    }

    /**
     * Generates standard formatted filename for Storage Access Framework picker.
     */
    fun getDefaultCsvFilename(prefix: String = "gemi_leads"): String {
        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        return "${prefix}_${dateStr}.csv"
    }

    private fun escapeCsv(value: String): String {
        val cleanValue = value.replace("\r", " ").replace("\n", " ")
        return if (cleanValue.contains(",") || cleanValue.contains("\"") || cleanValue.contains(";")) {
            "\"" + cleanValue.replace("\"", "\"\"") + "\""
        } else {
            "\"$cleanValue\""
        }
    }
}
