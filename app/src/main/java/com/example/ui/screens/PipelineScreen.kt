package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.LeadEntity
import com.example.data.model.PipelineStatus
import com.example.ui.components.LegalFormBadge
import com.example.ui.components.PipelineStatusChip
import com.example.ui.components.StatCard
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalOnBackground
import com.example.ui.theme.NaturalOutline
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.NaturalSecondary
import com.example.ui.theme.NaturalSurfaceHighlight
import com.example.ui.theme.NaturalSurfaceVariant
import com.example.ui.theme.StatusContacted
import com.example.ui.theme.StatusMeeting
import com.example.ui.theme.StatusWon

@Composable
fun PipelineScreen(
    pipelineLeads: List<LeadEntity>,
    onLeadClick: (LeadEntity) -> Unit,
    onToggleSave: (LeadEntity) -> Unit,
    onExportCsv: (List<LeadEntity>) -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilterStatus by remember { mutableStateOf<PipelineStatus?>(null) }
    var onlySaved by remember { mutableStateOf(false) }

    val displayedLeads = pipelineLeads.filter { lead ->
        val matchesStatus = selectedFilterStatus == null || lead.pipelineStatus == selectedFilterStatus
        val matchesSaved = !onlySaved || lead.isSaved
        matchesStatus && matchesSaved
    }

    val wonCount = pipelineLeads.count { it.pipelineStatus == PipelineStatus.DEAL_WON }
    val activeCount = pipelineLeads.count { it.pipelineStatus == PipelineStatus.CONTACTED || it.pipelineStatus == PipelineStatus.MEETING_SET || it.pipelineStatus == PipelineStatus.PROPOSAL_SENT }
    val savedCount = pipelineLeads.count { it.isSaved }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("pipeline_screen")
    ) {
        // Summary Stats Row with CSV export button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatCard(
                title = "Σε Εξέλιξη",
                value = "$activeCount",
                subtitle = "Επαφές & Ραντεβού",
                icon = Icons.Default.ViewKanban,
                accentColor = Color(0xFFB47836),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Κερδισμένα",
                value = "$wonCount",
                subtitle = "Deals Won",
                icon = Icons.Default.Handshake,
                accentColor = Color(0xFF487748),
                modifier = Modifier.weight(1f)
            )
        }

        // Export Actions Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${displayedLeads.size} CRM Leads (${selectedFilterStatus?.labelGr ?: if (onlySaved) "Αποθηκευμένα" else "Όλα"})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NaturalOnBackground
            )

            OutlinedButton(
                onClick = {
                    com.example.data.export.GemiCsvExporter.shareCsvFile(
                        context = context,
                        leads = displayedLeads,
                        chooserTitle = "Εξαγωγή CRM Pipeline Leads (${displayedLeads.size})",
                        filePrefix = "gemi_crm_pipeline"
                    )
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE0DBCF)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NaturalPrimary),
                modifier = Modifier.height(36.dp).testTag("pipeline_export_csv_btn")
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp), tint = NaturalPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Εξαγωγή CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NaturalPrimary)
            }
        }

        // Horizontal Status Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedFilterStatus == null && !onlySaved,
                onClick = {
                    selectedFilterStatus = null
                    onlySaved = false
                },
                label = { Text("Όλα τα CRM Leads (${pipelineLeads.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NaturalPrimary,
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFFF5F2EB),
                    labelColor = NaturalOnBackground
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedFilterStatus == null && !onlySaved,
                    borderColor = Color(0xFFE0DBCF),
                    selectedBorderColor = NaturalPrimary
                ),
                modifier = Modifier.testTag("pipeline_filter_all")
            )

            FilterChip(
                selected = onlySaved,
                onClick = {
                    onlySaved = !onlySaved
                    selectedFilterStatus = null
                },
                label = { Text("Αποθηκευμένα ($savedCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = { Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(14.dp), tint = NaturalPrimary) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NaturalPrimary,
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFFF5F2EB),
                    labelColor = NaturalOnBackground
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = onlySaved,
                    borderColor = Color(0xFFE0DBCF),
                    selectedBorderColor = NaturalPrimary
                ),
                modifier = Modifier.testTag("pipeline_filter_saved")
            )

            PipelineStatus.entries.forEach { status ->
                val isSelected = selectedFilterStatus == status && !onlySaved
                val count = pipelineLeads.count { it.pipelineStatus == status }
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedFilterStatus = if (isSelected) null else status
                        onlySaved = false
                    },
                    label = { Text("${status.labelGr} ($count)", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NaturalSecondary,
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFFF5F2EB),
                        labelColor = NaturalOnBackground
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Color(0xFFE0DBCF),
                        selectedBorderColor = NaturalSecondary
                    ),
                    modifier = Modifier.testTag("pipeline_filter_${status.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Leads List
        if (displayedLeads.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F2EB)),
                    border = BorderStroke(1.dp, Color(0xFFE0DBCF)),
                    modifier = Modifier.padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = NaturalPrimary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Κενό CRM Pipeline",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalOnBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Αποθηκεύστε Leads από τα «Προτεινόμενα» ή αλλάξτε την κατάστασή τους σε Επαφή/Ραντεβού για να τα διαχειριστείτε εδώ.",
                            fontSize = 13.sp,
                            color = NaturalSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("pipeline_leads_list"),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayedLeads, key = { it.gemiNumber }) { lead ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pipeline_card_${lead.gemiNumber}"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp),
                        border = BorderStroke(1.dp, if (lead.isSaved) NaturalPrimary else Color(0xFFE0DBCF))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
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
                                    PipelineStatusChip(status = lead.pipelineStatus)
                                }

                                IconButton(
                                    onClick = { onToggleSave(lead) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (lead.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Save",
                                        tint = if (lead.isSaved) NaturalPrimary else Color(0xFF79756D)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = lead.companyName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalOnBackground
                            )

                            if (lead.tradeName.isNotBlank()) {
                                Text(
                                    text = "«${lead.tradeName}»",
                                    fontSize = 13.sp,
                                    color = NaturalPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "ΚΑΔ ${lead.primaryKad} • ${lead.region}, ${lead.municipality}",
                                fontSize = 12.sp,
                                color = NaturalSecondary
                            )

                            // Follow-up notes preview if present
                            if (lead.userNotes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Color(0xFFF5F2EB),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFFEDE8DD)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Notes, contentDescription = null, tint = NaturalPrimary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = lead.userNotes,
                                            fontSize = 12.sp,
                                            color = NaturalOnBackground,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (lead.phone.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${lead.phone}"))
                                                context.startActivity(intent)
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, Color(0xFFE0DBCF)),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NaturalPrimary)
                                        ) {
                                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp), tint = NaturalPrimary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Κλήση", fontSize = 12.sp, color = NaturalPrimary)
                                        }
                                    }
                                    if (lead.address.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = {
                                                val query = "${lead.address} ${lead.municipality} ${lead.region}"
                                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(query)}")))
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, Color(0xFFE0DBCF)),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NaturalSecondary)
                                        ) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = NaturalSecondary)
                                        }
                                    }
                                }

                                Button(
                                    onClick = { onLeadClick(lead) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NaturalPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Επεξεργασία", fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

