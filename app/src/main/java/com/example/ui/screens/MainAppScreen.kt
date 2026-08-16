package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.DynamicFeed
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.ViewKanban
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryNavy
import com.example.ui.viewmodel.LeadViewModel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalOnBackground
import com.example.ui.theme.NaturalOutline
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.NaturalSecondary
import com.example.ui.theme.NaturalSurfaceHighlight
import com.example.ui.theme.NaturalSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: LeadViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val matchedLeads by viewModel.matchedLeads.collectAsState()
    val allLeads by viewModel.filteredAllLeads.collectAsState()
    val rawAllLeads by viewModel.allLeadsRaw.collectAsState()
    val pipelineLeads by viewModel.pipelineLeads.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedLeadDetail by viewModel.selectedLeadDetail.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val showBackendSpecs by viewModel.showBackendArchitectureModal.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedLegalForm by viewModel.selectedLegalFormFilter.collectAsState()
    val selectedSector by viewModel.selectedSectorFilter.collectAsState()
    val selectedRegion by viewModel.selectedRegionFilter.collectAsState()

    // If onboarding not completed, show Onboarding Wizard
    if (userProfile == null || !userProfile!!.isOnboardingCompleted) {
        OnboardingScreen(
            onComplete = { compName, ownGemi, bType, sectors, kads, isPan, regions, goal ->
                viewModel.completeOnboarding(
                    compName, ownGemi, bType, sectors, kads, isPan, regions, goal
                )
            }
        )
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFEFEBE0),
                            border = BorderStroke(1.dp, Color(0xFFE0DBCF)),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "ΓΜ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NaturalPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "B2B DASHBOARD",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalSecondary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "GEMI Lead Discovery",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalOnBackground
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.showBackendArchitectureModal.value = true },
                        modifier = Modifier.testTag("topbar_backend_specs_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Cloud Function & Firestore Specs",
                            tint = NaturalSecondary
                        )
                    }
                    IconButton(
                        onClick = { viewModel.triggerSync() },
                        enabled = !isSyncing,
                        modifier = Modifier.testTag("topbar_sync_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync",
                            tint = NaturalPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = NaturalBackground,
                    titleContentColor = NaturalOnBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF5F2EB),
                tonalElevation = 4.dp,
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                // Tab 0: My Matches (Home)
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectedTab.value = 0 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (matchedLeads.isNotEmpty()) {
                                    Badge(containerColor = NaturalPrimary, contentColor = Color.White) {
                                        Text("${matchedLeads.size}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Default.Home else Icons.Outlined.Home,
                                contentDescription = "Home / Προτεινόμενα"
                            )
                        }
                    },
                    label = { Text("Προτεινόμενα", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NaturalPrimary,
                        selectedTextColor = NaturalPrimary,
                        unselectedIconColor = NaturalOnBackground.copy(alpha = 0.6f),
                        unselectedTextColor = NaturalOnBackground.copy(alpha = 0.6f),
                        indicatorColor = NaturalSurfaceHighlight
                    ),
                    modifier = Modifier.testTag("nav_tab_matches")
                )

                // Tab 1: All Leads (Analytics & Search)
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectedTab.value = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Default.Analytics else Icons.Outlined.Analytics,
                            contentDescription = "Όλα τα ΓΕΜΗ"
                        )
                    },
                    label = { Text("Όλα τα ΓΕΜΗ", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NaturalPrimary,
                        selectedTextColor = NaturalPrimary,
                        unselectedIconColor = NaturalOnBackground.copy(alpha = 0.6f),
                        unselectedTextColor = NaturalOnBackground.copy(alpha = 0.6f),
                        indicatorColor = NaturalSurfaceHighlight
                    ),
                    modifier = Modifier.testTag("nav_tab_all_leads")
                )

                // Tab 2: CRM Pipeline (Bookmark & Saved Leads)
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.selectedTab.value = 2 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (pipelineLeads.isNotEmpty()) {
                                    Badge(containerColor = NaturalSecondary, contentColor = Color.White) {
                                        Text("${pipelineLeads.size}", fontSize = 10.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 2) Icons.Default.Bookmark else Icons.Outlined.Bookmark,
                                contentDescription = "Pipeline"
                            )
                        }
                    },
                    label = { Text("CRM Pipeline", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NaturalPrimary,
                        selectedTextColor = NaturalPrimary,
                        unselectedIconColor = NaturalOnBackground.copy(alpha = 0.6f),
                        unselectedTextColor = NaturalOnBackground.copy(alpha = 0.6f),
                        indicatorColor = NaturalSurfaceHighlight
                    ),
                    modifier = Modifier.testTag("nav_tab_pipeline")
                )

                // Tab 3: Target Preferences & Settings
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.selectedTab.value = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Default.Settings else Icons.Outlined.Settings,
                            contentDescription = "Ρυθμίσεις"
                        )
                    },
                    label = { Text("Ρυθμίσεις", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NaturalPrimary,
                        selectedTextColor = NaturalPrimary,
                        unselectedIconColor = NaturalOnBackground.copy(alpha = 0.6f),
                        unselectedTextColor = NaturalOnBackground.copy(alpha = 0.6f),
                        indicatorColor = NaturalSurfaceHighlight
                    ),
                    modifier = Modifier.testTag("nav_tab_preferences")
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Crossfade(
            targetState = selectedTab,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "ScreenCrossfade"
        ) { tab ->
            when (tab) {
                0 -> MyMatchesScreen(
                    matchedLeads = matchedLeads,
                    totalAllLeadsCount = rawAllLeads.size,
                    userProfile = userProfile,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.searchQuery.value = it },
                    onLeadClick = { viewModel.selectLeadForDetail(it) },
                    onToggleSave = { viewModel.toggleSaveLead(it) },
                    onTriggerSync = { viewModel.triggerSync() },
                    isSyncing = isSyncing,
                    syncMessage = syncMessage,
                    onOpenPreferences = { viewModel.selectedTab.value = 3 }
                )
                1 -> AllLeadsFeedScreen(
                    leads = allLeads,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.searchQuery.value = it },
                    selectedLegalForm = selectedLegalForm,
                    onLegalFormChange = { viewModel.selectedLegalFormFilter.value = it },
                    selectedSector = selectedSector,
                    onSectorChange = { viewModel.selectedSectorFilter.value = it },
                    selectedRegion = selectedRegion,
                    onRegionChange = { viewModel.selectedRegionFilter.value = it },
                    onLeadClick = { viewModel.selectLeadForDetail(it) },
                    onToggleSave = { viewModel.toggleSaveLead(it) },
                    onExportCsv = { leadsToExport -> viewModel.exportCsv(leadsToExport) },
                    onClearFilters = { viewModel.clearFilters() }
                )
                2 -> PipelineScreen(
                    pipelineLeads = pipelineLeads,
                    onLeadClick = { viewModel.selectLeadForDetail(it) },
                    onToggleSave = { viewModel.toggleSaveLead(it) },
                    onExportCsv = { leadsToExport -> viewModel.exportCsv(leadsToExport) }
                )
                3 -> PreferencesScreen(
                    userProfile = userProfile,
                    onSavePreferences = { sectors, kads, isPan, regions ->
                        viewModel.updateTargetPreferences(sectors, kads, isPan, regions)
                    },
                    onOpenBackendSpecs = { viewModel.showBackendArchitectureModal.value = true },
                    onTriggerSync = { viewModel.triggerSync() }
                )
            }
        }

        // Lead Details Modal Bottom Sheet
        selectedLeadDetail?.let { lead ->
            LeadDetailBottomSheet(
                lead = lead,
                onDismiss = { viewModel.selectLeadForDetail(null) },
                onToggleSave = { viewModel.toggleSaveLead(it) },
                onUpdateStatus = { targetLead, status ->
                    viewModel.updateLeadStatus(targetLead, status)
                },
                onUpdateNotes = { targetLead, notes ->
                    viewModel.updateLeadNotes(targetLead, notes)
                }
            )
        }

        // Backend Architecture & Cloud Function Specs Modal
        if (showBackendSpecs) {
            BackendArchitectureDialog(
                onDismiss = { viewModel.showBackendArchitectureModal.value = false }
            )
        }
    }
}
