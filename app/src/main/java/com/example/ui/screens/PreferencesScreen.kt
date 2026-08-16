package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.local.entity.UserProfileEntity
import com.example.data.model.GreekRegions
import com.example.data.model.GreekTaxonomy
import com.example.data.remote.GemiApiService
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalOnBackground
import com.example.ui.theme.NaturalOutline
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.NaturalSecondary
import com.example.ui.theme.NaturalSurfaceHighlight
import com.example.ui.theme.NaturalSurfaceVariant

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreferencesScreen(
    userProfile: UserProfileEntity?,
    onSavePreferences: (sectors: String, kads: String, isPanHellenic: Boolean, regions: String) -> Unit,
    onOpenBackendSpecs: () -> Unit,
    onTriggerSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val initialSectors = userProfile?.targetSectors?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: listOf("HORECA", "TOURISM")
    val initialKads = userProfile?.targetKads?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: listOf("56.10", "56.30", "55.10")
    val initialRegions = userProfile?.targetRegions?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: listOf("Αττική", "Κρήτη", "Κεντρική Μακεδονία")

    val selectedSectors = remember(userProfile) { mutableStateListOf<String>().apply { addAll(initialSectors) } }
    val selectedKads = remember(userProfile) { mutableStateListOf<String>().apply { addAll(initialKads) } }
    var isPanHellenic by remember(userProfile) { mutableStateOf(userProfile?.isPanHellenic ?: false) }
    val selectedRegions = remember(userProfile) { mutableStateListOf<String>().apply { addAll(initialRegions) } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("preferences_screen")
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Profile Overview Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp),
            border = BorderStroke(1.dp, Color(0xFFE0DBCF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = NaturalPrimary, modifier = Modifier.size(24.dp))
                    Column {
                        Text(
                            text = userProfile?.companyName?.ifBlank { "Η Επιχείρησή μου" } ?: "Hellas Catering Equipment Ltd",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalOnBackground
                        )
                        Text(
                            text = userProfile?.businessType?.ifBlank { "B2B Lead Target Profile" } ?: "B2B Lead Target Profile",
                            fontSize = 12.sp,
                            color = NaturalSecondary
                        )
                    }
                }
                if (userProfile?.ownGemiNumber?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Αριθμός Γ.Ε.ΜΗ.: ${userProfile.ownGemiNumber}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = NaturalPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Target Sectors Selection
        Text(
            text = "Στοχευμένοι Κλάδοι Επιχειρήσεων",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = NaturalOnBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GreekTaxonomy.CATEGORIES.forEach { cat ->
                val isSelected = selectedSectors.contains(cat.id)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) selectedSectors.remove(cat.id)
                        else selectedSectors.add(cat.id)
                    },
                    label = { Text(cat.nameGr, fontSize = 12.sp) },
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
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Target Regions
        Text(
            text = "Γεωγραφική Κάλυψη Leads",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = NaturalOnBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isPanHellenic = !isPanHellenic },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isPanHellenic) NaturalPrimary else Color(0xFFF5F2EB)
            ),
            border = BorderStroke(1.dp, if (isPanHellenic) NaturalPrimary else Color(0xFFE0DBCF))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Public, contentDescription = null, tint = if (isPanHellenic) Color.White else NaturalPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Πανελλαδικά (Όλες οι Περιφέρειες)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPanHellenic) Color.White else NaturalOnBackground
                    )
                }
                Switch(
                    checked = isPanHellenic,
                    onCheckedChange = { isPanHellenic = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF8C754D),
                        checkedTrackColor = Color(0xFF534600)
                    )
                )
            }
        }

        if (!isPanHellenic) {
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GreekRegions.REGIONS.forEach { reg ->
                    val isSelected = selectedRegions.contains(reg)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) selectedRegions.remove(reg)
                            else selectedRegions.add(reg)
                        },
                        label = { Text(reg, fontSize = 12.sp) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White) }
                        } else null,
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
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save Preferences Button
        Button(
            onClick = {
                onSavePreferences(
                    selectedSectors.joinToString(","),
                    selectedKads.joinToString(","),
                    isPanHellenic,
                    selectedRegions.joinToString(",")
                )
                Toast.makeText(context, "Οι στοχεύσεις αποθηκεύτηκαν!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_preferences_btn"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NaturalPrimary)
        ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Αποθήκευση & Επαναϋπολογισμός Match Score", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // GEMI Open Data API Specs & Rate Limit Card
        Text(
            text = "Στοιχεία Διασύνδεσης Γ.Ε.ΜΗ. & Όρια",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = NaturalOnBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F2EB)),
            border = BorderStroke(1.dp, Color(0xFFE0DBCF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFFB47836), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Rate Limit: 8 κλήσεις ανά λεπτό (8 req/min)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalOnBackground
                    )
                }
                Text(
                    text = "API Key: VeWz15eTqbFrqaazLUYUfyUVMr1w6Zfa (Ενεργό)",
                    fontSize = 12.sp,
                    color = NaturalSecondary
                )
                Text(
                    text = "Endpoint: https://opendata.uhc.gr/api/v1/companies/new",
                    fontSize = 11.sp,
                    color = NaturalSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // View Backend Specs Modal Action (Cloud function & Firestore schema)
        OutlinedButton(
            onClick = onOpenBackendSpecs,
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("open_backend_specs_btn"),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFFE0DBCF)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NaturalPrimary)
        ) {
            Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp), tint = NaturalPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Προβολή Cloud Ingestion Backend Specs", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NaturalPrimary)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

