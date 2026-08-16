package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GreekRegions
import com.example.data.model.GreekTaxonomy
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalOnBackground
import com.example.ui.theme.NaturalOutline
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.NaturalSecondary
import com.example.ui.theme.NaturalSurfaceHighlight
import com.example.ui.theme.NaturalSurfaceVariant

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onComplete: (
        companyName: String,
        ownGemi: String,
        businessType: String,
        sectors: String,
        kads: String,
        isPanHellenic: Boolean,
        regions: String,
        clientGoal: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(1) }

    // Step 1: Business Details
    var companyName by remember { mutableStateOf("Hellas Commercial Equipment Ltd") }
    var ownGemi by remember { mutableStateOf("154892010000") }
    var businessType by remember { mutableStateOf("Προμηθευτής Επαγγελματικού Εξοπλισμού Εστίασης & POS") }

    // Step 2: Target Sectors & KADs
    val selectedSectors = remember { mutableStateListOf("HORECA", "TOURISM") }
    val selectedKads = remember { mutableStateListOf("56.10", "56.30", "56.21", "55.10", "55.20") }

    // Step 3: Geographic Coverage
    var isPanHellenic by remember { mutableStateOf(false) }
    val selectedRegions = remember { mutableStateListOf("Αττική", "Κεντρική Μακεδονία", "Κρήτη", "Νότιο Αιγαίο (Κυκλάδες / Δωδεκάνησα)") }

    // Step 4: Client Target Goal & Notifications
    var clientGoal by remember { mutableStateOf("Νέα φυσικά καταστήματα, εστιατόρια και ξενοδοχειακές μονάδες") }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("onboarding_screen"),
        color = NaturalBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header Progress
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (step > 1) {
                    IconButton(
                        onClick = { step-- },
                        modifier = Modifier.testTag("onboarding_back_btn")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NaturalOnBackground)
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Text(
                    text = "Βήμα $step από 4",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NaturalPrimary
                )

                Text(
                    text = "Ρύθμιση Προφίλ",
                    fontSize = 13.sp,
                    color = NaturalSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { step / 4f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = NaturalPrimary,
                trackColor = Color(0xFFE0DBCF)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Step Content
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                modifier = Modifier.weight(1f),
                label = "OnboardingSteps"
            ) { currentStep ->
                when (currentStep) {
                    1 -> Step1BusinessInfo(
                        companyName = companyName,
                        onCompanyNameChange = { companyName = it },
                        ownGemi = ownGemi,
                        onOwnGemiChange = { ownGemi = it },
                        businessType = businessType,
                        onBusinessTypeChange = { businessType = it }
                    )
                    2 -> Step2TargetKads(
                        selectedSectors = selectedSectors,
                        onToggleSector = { sectorId ->
                            if (selectedSectors.contains(sectorId)) {
                                selectedSectors.remove(sectorId)
                            } else {
                                selectedSectors.add(sectorId)
                                // Add default kads for this sector
                                val defaultKads = GreekTaxonomy.CATEGORIES.find { it.id == sectorId }?.defaultKads ?: emptyList()
                                defaultKads.forEach { kad ->
                                    if (!selectedKads.contains(kad)) selectedKads.add(kad)
                                }
                            }
                        },
                        selectedKads = selectedKads,
                        onToggleKad = { kad ->
                            if (selectedKads.contains(kad)) selectedKads.remove(kad)
                            else selectedKads.add(kad)
                        }
                    )
                    3 -> Step3GeographicCoverage(
                        isPanHellenic = isPanHellenic,
                        onTogglePanHellenic = { isPanHellenic = it },
                        selectedRegions = selectedRegions,
                        onToggleRegion = { reg ->
                            if (selectedRegions.contains(reg)) selectedRegions.remove(reg)
                            else selectedRegions.add(reg)
                        }
                    )
                    4 -> Step4ClientGoalAndFinish(
                        clientGoal = clientGoal,
                        onClientGoalChange = { clientGoal = it },
                        companyName = companyName,
                        selectedKadsCount = selectedKads.size,
                        selectedRegionsCount = if (isPanHellenic) "Όλη η Ελλάδα" else "${selectedRegions.size} Περιφέρειες"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (step < 4) {
                    Button(
                        onClick = { step++ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("onboarding_next_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NaturalPrimary)
                    ) {
                        Text("Συνέχεια", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                    }
                } else {
                    Button(
                        onClick = {
                            onComplete(
                                companyName,
                                ownGemi,
                                businessType,
                                selectedSectors.joinToString(","),
                                selectedKads.joinToString(","),
                                isPanHellenic,
                                selectedRegions.joinToString(","),
                                clientGoal
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("onboarding_finish_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NaturalPrimary)
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Έναρξη Λήψης Leads Γ.Ε.ΜΗ.", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun Step1BusinessInfo(
    companyName: String,
    onCompanyNameChange: (String) -> Unit,
    ownGemi: String,
    onOwnGemiChange: (String) -> Unit,
    businessType: String,
    onBusinessTypeChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Πείτε μας για την επιχείρησή σας",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = NaturalOnBackground
        )
        Text(
            text = "Ρυθμίστε το προφίλ σας για να λαμβάνετε μόνο στοχευμένες νέες εγγραφές επιχειρήσεων.",
            fontSize = 13.sp,
            color = NaturalSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        OutlinedTextField(
            value = companyName,
            onValueChange = onCompanyNameChange,
            label = { Text("Επωνυμία Επιχείρησής σας") },
            placeholder = { Text("π.χ. Hellas Catering Solutions") },
            leadingIcon = { Icon(Icons.Outlined.Business, contentDescription = null, tint = NaturalPrimary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_company_name"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NaturalPrimary,
                unfocusedBorderColor = Color(0xFFE0DBCF)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = ownGemi,
            onValueChange = onOwnGemiChange,
            label = { Text("Αριθμός Γ.Ε.ΜΗ. (Προαιρετικό)") },
            placeholder = { Text("π.χ. 123456789000") },
            leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = NaturalPrimary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_own_gemi"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NaturalPrimary,
                unfocusedBorderColor = Color(0xFFE0DBCF)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = businessType,
            onValueChange = onBusinessTypeChange,
            label = { Text("Αντικείμενο & Προϊόντα / Υπηρεσίες") },
            placeholder = { Text("π.χ. Εξοπλισμός εστιατορίων, ταμειακά συστήματα, λογιστικά, marketing...") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth().testTag("input_business_type"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NaturalPrimary,
                unfocusedBorderColor = Color(0xFFE0DBCF)
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F2EB)),
            border = BorderStroke(1.dp, Color(0xFFE0DBCF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Public, contentDescription = null, tint = NaturalPrimary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Η εφαρμογή θα παρακολουθεί καθημερινά τα επίσημα Ανοιχτά Δεδομένα του Γ.Ε.ΜΗ. (Open Data) και θα φιλτράρει αυτόματα τους ιδανικούς πελάτες σας.",
                    fontSize = 12.sp,
                    color = NaturalOnBackground
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Step2TargetKads(
    selectedSectors: List<String>,
    onToggleSector: (String) -> Unit,
    selectedKads: List<String>,
    onToggleKad: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Στοχευμένοι Κλάδοι & ΚΑΔ",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = NaturalOnBackground
        )
        Text(
            text = "Επιλέξτε ποιες κατηγορίες νέων επιχειρήσεων αποτελούν πιθανούς πελάτες σας:",
            fontSize = 13.sp,
            color = NaturalSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        Text(
            text = "1. Επιλογή Βασικών Κλάδων:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = NaturalOnBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GreekTaxonomy.CATEGORIES.forEach { category ->
                val isSelected = selectedSectors.contains(category.id)
                FilterChip(
                    selected = isSelected,
                    onClick = { onToggleSector(category.id) },
                    label = { Text(category.nameGr, fontSize = 12.sp) },
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
                    modifier = Modifier.testTag("sector_chip_${category.id}")
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "2. Εξειδικευμένοι Κωδικοί ΚΑΔ:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = NaturalOnBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Display KAD chips for active categories
        val relevantKads = GreekTaxonomy.ALL_KADS.filter {
            selectedSectors.isEmpty() || selectedSectors.contains(it.categoryId)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            relevantKads.forEach { kadItem ->
                val isSelected = selectedKads.contains(kadItem.code)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleKad(kadItem.code) }
                        .testTag("kad_item_${kadItem.code}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFF5F2EB) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) NaturalPrimary else Color(0xFFE0DBCF)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) NaturalPrimary else Color.Transparent)
                                .border(1.dp, NaturalPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ΚΑΔ ${kadItem.code}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) NaturalPrimary else NaturalOnBackground
                            )
                            Text(
                                text = kadItem.titleGr,
                                fontSize = 12.sp,
                                color = NaturalSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Modifier.border(width: androidx.compose.ui.unit.Dp, color: Color, shape: androidx.compose.ui.graphics.Shape): Modifier {
    return this.then(Modifier.background(Color.Transparent, shape))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Step3GeographicCoverage(
    isPanHellenic: Boolean,
    onTogglePanHellenic: (Boolean) -> Unit,
    selectedRegions: List<String>,
    onToggleRegion: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Γεωγραφική Κάλυψη",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = NaturalOnBackground
        )
        Text(
            text = "Πού δραστηριοποιείστε και σε ποιες περιοχές αναζητάτε νέες εταιρείες;",
            fontSize = 13.sp,
            color = NaturalSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Panhellenic Option Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTogglePanHellenic(!isPanHellenic) }
                .testTag("panhellenic_option"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isPanHellenic) NaturalPrimary else Color(0xFFF5F2EB)
            ),
            border = BorderStroke(1.5.dp, if (isPanHellenic) NaturalPrimary else Color(0xFFE0DBCF))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = if (isPanHellenic) Color.White else NaturalPrimary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Πανελλαδικά (Όλη η Ελλάδα)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPanHellenic) Color.White else NaturalOnBackground
                    )
                    Text(
                        text = "Λήψη όλων των νέων εταιρειών ανεξαρτήτως περιοχής έδρας",
                        fontSize = 12.sp,
                        color = if (isPanHellenic) Color.White.copy(alpha = 0.85f) else NaturalSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Ή επιλέξτε συγκεκριμένες Περιφέρειες:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = NaturalOnBackground
        )
        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GreekRegions.REGIONS.forEach { region ->
                val isSelected = !isPanHellenic && selectedRegions.contains(region)
                FilterChip(
                    selected = isSelected,
                    enabled = !isPanHellenic,
                    onClick = { onToggleRegion(region) },
                    label = { Text(region, fontSize = 12.sp) },
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
                    ),
                    modifier = Modifier.testTag("region_chip_$region")
                )
            }
        }
    }
}

@Composable
fun Step4ClientGoalAndFinish(
    clientGoal: String,
    onClientGoalChange: (String) -> Unit,
    companyName: String,
    selectedKadsCount: Int,
    selectedRegionsCount: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Στόχος Πελατών & Ειδοποιήσεις",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = NaturalOnBackground
        )
        Text(
            text = "Όλα είναι έτοιμα για την ενεργοποίηση της καθημερινής ροής leads.",
            fontSize = 13.sp,
            color = NaturalSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        OutlinedTextField(
            value = clientGoal,
            onValueChange = onClientGoalChange,
            label = { Text("Στόχος / Προφίλ Ιδανικού Πελάτη") },
            placeholder = { Text("π.χ. Beach bars, νέα καφέ & ταβέρνες σε τουριστικές περιοχές") },
            maxLines = 2,
            modifier = Modifier.fillMaxWidth().testTag("input_client_goal"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NaturalPrimary,
                unfocusedBorderColor = Color(0xFFE0DBCF)
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Summary Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp),
            border = BorderStroke(1.dp, Color(0xFFE0DBCF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Σύνοψη Ρυθμίσεων Lead Matching",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NaturalPrimary
                )
                DetailRow(label = "Επιχείρηση:", value = companyName.ifBlank { "Η Επιχείρησή μου" })
                DetailRow(label = "Επιλεγμένοι ΚΑΔ:", value = "$selectedKadsCount στοχευμένοι κωδικοί δραστηριότητας")
                DetailRow(label = "Γεωγραφική Κάλυψη:", value = selectedRegionsCount)
                DetailRow(label = "Καθημερινή Ενημέρωση:", value = "Κάθε πρωί στις 08:30 π.μ. (UTC+2)")
                DetailRow(label = "Πηγή Δεδομένων:", value = "Γενικό Εμπορικό Μητρώο (Γ.Ε.ΜΗ. Open Data)")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F2EB)),
            border = BorderStroke(1.dp, Color(0xFFE0DBCF)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = NaturalPrimary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Μόλις ολοκληρώσετε, η εφαρμογή θα σας παρουσιάσει τα πιο πρόσφατα Leads με υπολογισμένο δείκτη συνάφειας (Match Score) και δυνατότητα άμεσης επικοινωνίας.",
                    fontSize = 12.sp,
                    color = NaturalOnBackground
                )
            }
        }
    }
}

