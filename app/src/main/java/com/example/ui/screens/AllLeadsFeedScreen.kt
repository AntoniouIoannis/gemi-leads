package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.ListAlt
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.LeadEntity
import com.example.data.model.GreekLegalForms
import com.example.data.model.GreekRegions
import com.example.data.model.GreekTaxonomy
import com.example.ui.components.AppSearchBar
import com.example.ui.components.LeadCard
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalOnBackground
import com.example.ui.theme.NaturalOutline
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.NaturalSecondary
import com.example.ui.theme.NaturalSurfaceHighlight
import com.example.ui.theme.NaturalSurfaceVariant

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AllLeadsFeedScreen(
    leads: List<LeadEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedLegalForm: String?,
    onLegalFormChange: (String?) -> Unit,
    selectedSector: String?,
    onSectorChange: (String?) -> Unit,
    selectedRegion: String?,
    onRegionChange: (String?) -> Unit,
    onLeadClick: (LeadEntity) -> Unit,
    onToggleSave: (LeadEntity) -> Unit,
    onExportCsv: (List<LeadEntity>) -> String,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("all_leads_feed_screen")
    ) {
        // Search Bar & Export CSV Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholderText = "Αναζήτηση σε όλες τις εγγραφές ΓΕΜΗ...",
                modifier = Modifier.weight(1f)
            )

            OutlinedButton(
                onClick = {
                    val csv = onExportCsv(leads)
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, csv)
                        type = "text/csv"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Εξαγωγή Leads σε CSV"))
                },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFE0DBCF)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NaturalPrimary),
                modifier = Modifier.testTag("export_csv_btn")
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = NaturalPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NaturalPrimary)
            }
        }

        // Horizontal Filter Chips: Legal Forms (ΙΚΕ, ΑΕ, ΟΕ, ΕΕ, Ατομική)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedLegalForm == null,
                onClick = { onLegalFormChange(null) },
                label = { Text("Όλες οι Μορφές", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NaturalPrimary,
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFFF5F2EB),
                    labelColor = NaturalOnBackground
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedLegalForm == null,
                    borderColor = Color(0xFFE0DBCF),
                    selectedBorderColor = NaturalPrimary
                ),
                modifier = Modifier.testTag("filter_all_legal_forms")
            )
            GreekLegalForms.ALL.forEach { form ->
                val isSelected = selectedLegalForm == form
                FilterChip(
                    selected = isSelected,
                    onClick = { onLegalFormChange(if (isSelected) null else form) },
                    label = { Text(form, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
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
                    modifier = Modifier.testTag("filter_form_$form")
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Horizontal Filter Chips: Sectors (HORECA, Tourism, Retail, Construction, Tech...)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedSector == null,
                onClick = { onSectorChange(null) },
                label = { Text("Όλοι οι Κλάδοι", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NaturalSecondary,
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFFF5F2EB),
                    labelColor = NaturalOnBackground
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedSector == null,
                    borderColor = Color(0xFFE0DBCF),
                    selectedBorderColor = NaturalSecondary
                ),
                modifier = Modifier.testTag("filter_all_sectors")
            )
            GreekTaxonomy.CATEGORIES.forEach { category ->
                val isSelected = selectedSector == category.id
                FilterChip(
                    selected = isSelected,
                    onClick = { onSectorChange(if (isSelected) null else category.id) },
                    label = { Text(category.nameGr, fontSize = 11.sp) },
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
                    modifier = Modifier.testTag("filter_sector_${category.id}")
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Feed Header & Results Counter
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text(
                text = "${leads.size} νέες εγγραφές Ελλάδας",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NaturalOnBackground
            )

            if (selectedLegalForm != null || selectedSector != null || selectedRegion != null || searchQuery.isNotBlank()) {
                Text(
                    text = "Καθαρισμός φίλτρων",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NaturalPrimary,
                    modifier = Modifier.clickable { onClearFilters() }
                )
            }
        }

        // Leads List
        if (leads.isEmpty()) {
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
                            imageVector = Icons.Default.ListAlt,
                            contentDescription = null,
                            tint = NaturalPrimary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Δεν βρέθηκαν αποτελέσματα",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalOnBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Δοκιμάστε να τροποποιήσετε τα κριτήρια αναζήτησης.",
                            fontSize = 13.sp,
                            color = NaturalSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onClearFilters,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NaturalPrimary)
                        ) {
                            Text("Επαναφορά Φίλτρων", fontSize = 13.sp, color = Color.White)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("all_leads_list"),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(leads, key = { it.gemiNumber }) { lead ->
                    LeadCard(
                        lead = lead,
                        showMatchBadge = false,
                        onCardClick = { onLeadClick(lead) },
                        onSaveToggle = { onToggleSave(lead) }
                    )
                }
            }
        }
    }
}

