package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.LeadEntity
import com.example.data.model.PipelineStatus
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BadgeAe
import com.example.ui.theme.BadgeAtomiki
import com.example.ui.theme.BadgeEe
import com.example.ui.theme.BadgeEpe
import com.example.ui.theme.BadgeIke
import com.example.ui.theme.BadgeOe
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.StatusContacted
import com.example.ui.theme.StatusLost
import com.example.ui.theme.StatusMeeting
import com.example.ui.theme.StatusNew
import com.example.ui.theme.StatusWon

@Composable
fun MatchBadge(score: Int, modifier: Modifier = Modifier) {
    val (badgeBg, textColor, borderColor) = when {
        score >= 80 -> Triple(Color(0xFFE7E3D8), Color(0xFF6D5E00), Color(0xFFD7D2C5)) // Natural Gold/Olive
        score >= 60 -> Triple(Color(0xFFE2EADF), Color(0xFF487748), Color(0xFFC7D6C3)) // Natural Sage
        else -> Triple(Color(0xFFF5EBE1), Color(0xFFB47836), Color(0xFFE5D2BE)) // Natural Ochre
    }

    Surface(
        color = badgeBg,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.testTag("match_badge_$score")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$score% Match",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun LegalFormBadge(legalForm: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (legalForm.uppercase()) {
        "ΙΚΕ" -> BadgeIke to Color.White
        "ΑΕ" -> BadgeAe to Color.White
        "ΟΕ" -> BadgeOe to Color.White
        "ΕΕ" -> BadgeEe to Color.White
        "ΕΠΕ" -> BadgeEpe to Color.White
        else -> BadgeAtomiki to Color.White
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier.testTag("legal_form_$legalForm")
    ) {
        Text(
            text = legalForm,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun PipelineStatusChip(status: PipelineStatus, modifier: Modifier = Modifier) {
    val (chipColor, label) = when (status) {
        PipelineStatus.NEW -> StatusNew to status.labelGr
        PipelineStatus.CONTACTED -> StatusContacted to status.labelGr
        PipelineStatus.MEETING_SET -> StatusMeeting to status.labelGr
        PipelineStatus.PROPOSAL_SENT -> Color(0xFF7A628C) to status.labelGr
        PipelineStatus.DEAL_WON -> StatusWon to status.labelGr
        PipelineStatus.NOT_INTERESTED -> StatusLost to status.labelGr
    }

    Surface(
        color = chipColor.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, chipColor.copy(alpha = 0.3f)),
        modifier = modifier.testTag("pipeline_status_${status.name}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(chipColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = chipColor
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("stat_card_$title"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F2EB)
        ),
        border = BorderStroke(1.dp, Color(0xFFE0DBCF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8C754D),
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F1B16)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF8C754D)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LeadCard(
    lead: LeadEntity,
    showMatchBadge: Boolean = true,
    onCardClick: () -> Unit,
    onSaveToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("lead_card_${lead.gemiNumber}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, if (lead.isSaved) Color(0xFF6D5E00) else Color(0xFFE0DBCF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Legal Form, GEMI No, Save Button
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
                    Text(
                        text = "ΓΕΜΗ: ${lead.gemiNumber}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF8C754D)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showMatchBadge && lead.matchScore > 0) {
                        MatchBadge(score = lead.matchScore)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    IconButton(
                        onClick = onSaveToggle,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("save_btn_${lead.gemiNumber}")
                    ) {
                        Icon(
                            imageVector = if (lead.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save Lead",
                            tint = if (lead.isSaved) Color(0xFF6D5E00) else Color(0xFF79756D)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Company Name
            Text(
                text = lead.companyName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F1B16),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Trade Name if present
            if (lead.tradeName.isNotBlank()) {
                Text(
                    text = "«${lead.tradeName}»",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6D5E00),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // KAD & Description
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
                    Text(
                        text = "ΚΑΔ ${lead.primaryKad}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6D5E00)
                    )
                    Text(
                        text = " : ${lead.kadDescription}",
                        fontSize = 12.sp,
                        color = Color(0xFF1F1B16),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Location & Date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = Color(0xFF8C754D),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${lead.region}${if (lead.municipality.isNotBlank()) " • " + lead.municipality else ""}",
                        fontSize = 12.sp,
                        color = Color(0xFF79756D),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "Σύσταση: ${lead.registrationDate}",
                    fontSize = 11.sp,
                    color = Color(0xFF8C754D)
                )
            }

            // Match Reasons Badges if present
            if (lead.matchReasons.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    lead.matchReasons.split("•").map { it.trim() }.filter { it.isNotEmpty() }.forEach { reason ->
                        Surface(
                            color = Color(0xFFE7E3D8),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.5.dp, Color(0xFFD7D2C5))
                        ) {
                            Text(
                                text = "✓ $reason",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF6D5E00),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Quick Actions & Pipeline Chip
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                PipelineStatusChip(status = lead.pipelineStatus)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (lead.phone.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${lead.phone}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.size(32.dp).testTag("call_btn_${lead.gemiNumber}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call",
                                tint = Color(0xFF6D5E00),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (lead.address.isNotBlank() || lead.municipality.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val query = "${lead.address} ${lead.municipality} ${lead.region} Greece"
                                val uri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                context.startActivity(intent)
                            },
                            modifier = Modifier.size(32.dp).testTag("map_btn_${lead.gemiNumber}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Map",
                                tint = Color(0xFF8C754D),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val shareText = "Νέα Εγγραφή ΓΕΜΗ:\nΕπωνυμία: ${lead.companyName}\nΓΕΜΗ: ${lead.gemiNumber}\nΚΑΔ: ${lead.primaryKad} - ${lead.kadDescription}\nΈδρα: ${lead.address}, ${lead.region}\nΤηλ: ${lead.phone}"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "GEMI Lead: ${lead.companyName}")
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Κοινοποίηση Lead"))
                        },
                        modifier = Modifier.size(32.dp).testTag("share_btn_${lead.gemiNumber}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color(0xFF79756D),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholderText: String = "Αναζήτηση με Επωνυμία, ΓΕΜΗ, ΑΦΜ, ΚΑΔ, Περιοχή...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = placeholderText,
                fontSize = 13.sp,
                color = Color(0xFF8C754D).copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF6D5E00)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.testTag("clear_search_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = Color(0xFF79756D)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = Color(0xFF6D5E00),
            unfocusedBorderColor = Color(0xFFE0DBCF)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_search_bar")
    )
}

