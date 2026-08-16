package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.LeadEntity
import com.example.data.model.PipelineStatus
import com.example.ui.components.LegalFormBadge
import com.example.ui.components.MatchBadge
import com.example.ui.components.PipelineStatusChip
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalOnBackground
import com.example.ui.theme.NaturalOutline
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.NaturalSecondary
import com.example.ui.theme.NaturalSurfaceHighlight
import com.example.ui.theme.NaturalSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LeadDetailBottomSheet(
    lead: LeadEntity,
    onDismiss: () -> Unit,
    onToggleSave: (LeadEntity) -> Unit,
    onUpdateStatus: (LeadEntity, PipelineStatus) -> Unit,
    onUpdateNotes: (LeadEntity, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var notesText by remember(lead.gemiNumber) { mutableStateOf(lead.userNotes) }
    var selectedStatus by remember(lead.gemiNumber) { mutableStateOf(lead.pipelineStatus) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("lead_detail_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar: Legal Form, Save, Close
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LegalFormBadge(legalForm = lead.legalForm)
                    if (lead.matchScore > 0) {
                        MatchBadge(score = lead.matchScore)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onToggleSave(lead) },
                        modifier = Modifier.testTag("detail_save_btn")
                    ) {
                        Icon(
                            imageVector = if (lead.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save Lead",
                            tint = if (lead.isSaved) NaturalPrimary else NaturalSecondary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("detail_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = NaturalSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Company & Trade Name
            Text(
                text = lead.companyName,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NaturalOnBackground,
                lineHeight = 26.sp
            )

            if (lead.tradeName.isNotBlank()) {
                Text(
                    text = "«${lead.tradeName}»",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NaturalPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contact & Action Row (Call, Maps, Email, Web, Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (lead.phone.isNotBlank()) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${lead.phone}"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f).testTag("action_call_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NaturalPrimary)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Κλήση", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                if (lead.address.isNotBlank() || lead.municipality.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            val query = "${lead.address} ${lead.municipality} ${lead.region} Greece"
                            val uri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        },
                        modifier = Modifier.weight(1f).testTag("action_map_btn"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE0DBCF)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NaturalSecondary)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = NaturalSecondary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Χάρτης", fontSize = 13.sp, color = NaturalSecondary)
                    }
                }

                if (lead.email.isNotBlank()) {
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${lead.email}"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.testTag("action_email_btn")
                    ) {
                        Icon(Icons.Default.Email, contentDescription = "Email", tint = NaturalPrimary)
                    }
                }

                IconButton(
                    onClick = {
                        val shareText = "Νέα Εταιρεία ΓΕΜΗ:\nΕπωνυμία: ${lead.companyName}\nΓΕΜΗ: ${lead.gemiNumber}\nΑΦΜ: ${lead.afm}\nΚΑΔ: ${lead.primaryKad} (${lead.kadDescription})\nΠεριοχή: ${lead.region}, ${lead.municipality}\nΤηλ: ${lead.phone}"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Κοινοποίηση"))
                    },
                    modifier = Modifier.testTag("action_share_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = NaturalSecondary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Official Commercial Registry Data (ΓΕΜΗ) Card
            Text(
                text = "Επίσημα Στοιχεία Μητρώου Γ.Ε.ΜΗ.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NaturalSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F2EB)),
                border = BorderStroke(1.dp, Color(0xFFE0DBCF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DetailRow(label = "Αριθμός Γ.Ε.ΜΗ.", value = lead.gemiNumber)
                    DetailRow(label = "Α.Φ.Μ.", value = lead.afm)
                    DetailRow(label = "Ημερομηνία Σύστασης", value = lead.registrationDate)
                    DetailRow(label = "Νομική Μορφή", value = lead.legalForm)
                    if (lead.initialCapital.isNotBlank()) {
                        DetailRow(label = "Αρχικό Κεφάλαιο", value = lead.initialCapital)
                    }
                    if (lead.administrators.isNotBlank()) {
                        DetailRow(label = "Νόμιμοι Εκπρόσωποι / Διαχειριστές", value = lead.administrators)
                    }
                    if (lead.chamberName.isNotBlank()) {
                        DetailRow(label = "Επιμελητήριο", value = lead.chamberName)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Activity & KAD Codes Card
            Text(
                text = "Επιχειρηματική Δραστηριότητα & ΚΑΔ",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NaturalSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F2EB)),
                border = BorderStroke(1.dp, Color(0xFFE0DBCF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DetailRow(label = "Κύριος ΚΑΔ", value = "${lead.primaryKad} - ${lead.kadDescription}")
                    if (lead.secondaryKads.isNotBlank()) {
                        DetailRow(label = "Δευτερεύοντες ΚΑΔ", value = lead.secondaryKads)
                    }
                    DetailRow(label = "Κλάδος", value = lead.sector)
                    DetailRow(label = "Έδρα / Περιφέρεια", value = "${lead.region}${if (lead.prefecture.isNotBlank()) " (${lead.prefecture})" else ""}")
                    if (lead.address.isNotBlank()) {
                        DetailRow(label = "Διεύθυνση Έδρας", value = "${lead.address}, ${lead.postalCode} ${lead.municipality}")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Pipeline Status Selector
            Text(
                text = "Κατάσταση Pipeline & CRM",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NaturalSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PipelineStatus.entries.forEach { status ->
                    val isSelected = status == selectedStatus
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedStatus = status
                            onUpdateStatus(lead, status)
                        },
                        label = { Text(status.labelGr, fontSize = 12.sp) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NaturalPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFFF5F2EB),
                            labelColor = NaturalOnBackground
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color(0xFFE0DBCF),
                            selectedBorderColor = NaturalPrimary
                        ),
                        modifier = Modifier.testTag("status_chip_${status.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Follow-up Notes
            Text(
                text = "Σημειώσεις Follow-up (Ιδιωτικό)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NaturalSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = notesText,
                onValueChange = {
                    notesText = it
                    onUpdateNotes(lead, it)
                },
                placeholder = { Text("π.χ. Έγινε τηλεφωνική επικοινωνία με τον κ. Παπαδόπουλο, ενδιαφέρεται για προσφορά εξοπλισμού...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("lead_notes_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NaturalPrimary,
                    unfocusedBorderColor = Color(0xFFE0DBCF)
                )
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = NaturalSecondary
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = NaturalOnBackground
        )
    }
}

