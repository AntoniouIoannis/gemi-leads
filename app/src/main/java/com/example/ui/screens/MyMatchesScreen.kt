package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.LeadEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.ui.components.AppSearchBar
import com.example.ui.components.LeadCard
import com.example.ui.components.StatCard
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalOnBackground
import com.example.ui.theme.NaturalOutline
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.NaturalSecondary
import com.example.ui.theme.NaturalTertiary
import com.example.ui.theme.StatusWon

@Composable
fun MyMatchesScreen(
    matchedLeads: List<LeadEntity>,
    totalAllLeadsCount: Int,
    userProfile: UserProfileEntity?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onLeadClick: (LeadEntity) -> Unit,
    onToggleSave: (LeadEntity) -> Unit,
    onTriggerSync: () -> Unit,
    isSyncing: Boolean,
    syncMessage: String?,
    onOpenPreferences: () -> Unit,
    modifier: Modifier = Modifier
) {
    val highMatchesCount = matchedLeads.count { it.matchScore >= 80 }
    val savedMatchesCount = matchedLeads.count { it.isSaved }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("my_matches_screen")
    ) {
        // Sync Banner / Rate Limit Bar
        if (syncMessage != null || isSyncing) {
            Surface(
                color = Color(0xFFF5F2EB),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE0DBCF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = NaturalPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF487748),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = syncMessage ?: "Συγχρονισμός σε εξέλιξη...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = NaturalOnBackground,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Summary Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Προτεινόμενα",
                value = "${matchedLeads.size}",
                subtitle = "Από $totalAllLeadsCount νέες εγγραφές",
                icon = Icons.Default.Stars,
                accentColor = NaturalPrimary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Υψηλή Συνάφεια",
                value = "$highMatchesCount",
                subtitle = "Match Score ≥ 80%",
                icon = Icons.Default.LocalFireDepartment,
                accentColor = Color(0xFFB47836),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Search Bar with sync and CSV action
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val context = androidx.compose.ui.platform.LocalContext.current

            AppSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholderText = "Αναζήτηση στα προτεινόμενα leads...",
                modifier = Modifier.weight(1f)
            )

            OutlinedButton(
                onClick = {
                    com.example.data.export.GemiCsvExporter.shareCsvFile(
                        context = context,
                        leads = matchedLeads,
                        chooserTitle = "Εξαγωγή Προτεινόμενων Leads σε CSV (${matchedLeads.size})",
                        filePrefix = "gemi_matched_leads"
                    )
                },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFE0DBCF)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NaturalPrimary),
                modifier = Modifier.testTag("matches_export_csv_btn")
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = NaturalPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NaturalPrimary)
            }

            IconButton(
                onClick = onTriggerSync,
                enabled = !isSyncing,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF5F2EB))
                    .testTag("sync_refresh_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Sync GEMI",
                    tint = NaturalPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Active Profile Target Filter Summary Chips
        userProfile?.let { prof ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
            ) {
                Text(
                    text = "Στοχεύσεις: ${prof.targetSectors.replace(",", " • ")}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = NaturalSecondary
                )
                Text(
                    text = "Επεξεργασία",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NaturalPrimary,
                    modifier = Modifier.clickable { onOpenPreferences() }
                )
            }
        }

        // Matched Leads List
        if (matchedLeads.isEmpty()) {
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
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = NaturalPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Δεν βρέθηκαν προτεινόμενα Leads",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalOnBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Δοκιμάστε να επεκτείνετε τους στοχευμένους ΚΑΔ ή τις Περιφέρειες στις Ρυθμίσεις.",
                            fontSize = 13.sp,
                            color = NaturalSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onOpenPreferences,
                            colors = ButtonDefaults.buttonColors(containerColor = NaturalPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Προσαρμογή Στοχεύσεων", fontSize = 13.sp, color = Color.White)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("matched_leads_list"),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(matchedLeads, key = { it.gemiNumber }) { lead ->
                    LeadCard(
                        lead = lead,
                        showMatchBadge = true,
                        onCardClick = { onLeadClick(lead) },
                        onSaveToggle = { onToggleSave(lead) }
                    )
                }
            }
        }
    }
}

