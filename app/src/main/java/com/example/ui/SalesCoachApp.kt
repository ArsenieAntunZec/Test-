package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

// Soft Slate Red, Orange, Green mappings
val L1Color = Color(0xFFEF5350) // Developing
val L2Color = Color(0xFFFF9800) // Stable
val L3Color = Color(0xFF4CAF50) // Progressive
val NoneColor = Color(0xFF757575)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesCoachApp(viewModel: MainViewModel) {
    val bds by viewModel.allBDs.collectAsState()
    val schedules by viewModel.allSchedules.collectAsState()
    val selectedBd by viewModel.selectedBd.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()

    var showAddBdDialog by remember { mutableStateOf(false) }
    var scheduleToEdit by remember { mutableStateOf<CoachingSchedule?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Sales Coach",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "BD Capability Tracker H2, '26",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddBdDialog = true },
                        modifier = Modifier.testTag("add_bd_action_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add New Person")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { viewModel.setTab(0) },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Coaching Grid") },
                    label = { Text("Grid Board") },
                    modifier = Modifier.testTag("tab_grid")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { viewModel.setTab(1) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Capability Profiles") },
                    label = { Text("Capability") },
                    modifier = Modifier.testTag("tab_capability")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { viewModel.setTab(2) },
                    icon = { Icon(Icons.Default.List, contentDescription = "Analytics Hub") },
                    label = { Text("Analysis") },
                    modifier = Modifier.testTag("tab_analytics")
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentTab) {
                0 -> DashboardGridScreen(
                    bds = bds,
                    schedules = schedules,
                    onEditSchedule = { scheduleToEdit = it },
                    onAddBdClick = { showAddBdDialog = true }
                )
                1 -> CapabilityProfileScreen(
                    viewModel = viewModel,
                    selectedBd = selectedBd,
                    bds = bds
                )
                2 -> AnalyticsScreen(
                    bds = bds,
                    allSchedules = schedules,
                    viewModel = viewModel
                )
            }
        }
    }

    if (showAddBdDialog) {
        AddBdDialog(
            onDismiss = { showAddBdDialog = false },
            onSave = { name, role, color ->
                viewModel.addBusinessDeveloper(name, role, color)
                showAddBdDialog = false
            }
        )
    }

    if (scheduleToEdit != null) {
        val bdName = bds.find { it.id == scheduleToEdit!!.bdId }?.name ?: "Business Developer"
        EditScheduleDialog(
            bdName = bdName,
            schedule = scheduleToEdit!!,
            onDismiss = { scheduleToEdit = null },
            onSave = { updatedSchedule ->
                viewModel.updateCoachingSchedule(updatedSchedule)
                scheduleToEdit = null
            }
        )
    }
}

@Composable
fun DashboardGridScreen(
    bds: List<BusinessDeveloper>,
    schedules: List<CoachingSchedule>,
    onEditSchedule: (CoachingSchedule) -> Unit,
    onAddBdClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Month Selector card (July, H2)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Active Month: July 2026",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "H2 Strategic Coaching Session Tracker",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "9 Successful Visits",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (bds.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No Tracked Persons Found",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        "Add your first Business Developer to start tracking weekly coaching sessions.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onAddBdClick, modifier = Modifier.testTag("add_bd_button_empty")) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Person")
                    }
                }
            }
        } else {
            Text(
                "Coaching Schedule Grid",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Dynamic grid list mimicking the coaching board excel log
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                    .background(Color.White, RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header Row of the Grid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                                RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                            )
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BD",
                            modifier = Modifier.weight(1.3f),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        (1..5).forEach { week ->
                            Text(
                                text = "Wk$week",
                                modifier = Modifier.weight(1.0f),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // BD Records
                    bds.forEach { bd ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // BD Profile info on the left
                            Column(modifier = Modifier.weight(1.3f).padding(end = 4.dp)) {
                                Text(
                                    bd.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    bd.role,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // 5 Columns for Weeks
                            (1..5).forEach { weekNum ->
                                val bdSchedules = schedules.filter { it.bdId == bd.id && it.weekNumber == weekNum }
                                val schedule = bdSchedules.firstOrNull() ?: CoachingSchedule(bdId = bd.id, weekNumber = weekNum, dayOfWeek = "", status = "NONE")

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 2.dp)
                                ) {
                                    GridCellPill(
                                        schedule = schedule,
                                        onClick = { onEditSchedule(schedule) }
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(L3Color.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                        .border(1.dp, L3Color, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Completed Session", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(L1Color.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                        .border(1.dp, L1Color, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cancelled", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(2.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Empty (Not Set)", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun GridCellPill(
    schedule: CoachingSchedule,
    onClick: () -> Unit
) {
    val backgroundColor = when (schedule.status) {
        "SUCCESSFUL" -> L3Color.copy(alpha = 0.85f)
        "CANCELLED" -> L1Color.copy(alpha = 0.85f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }

    val contentColor = when (schedule.status) {
        "SUCCESSFUL", "CANCELLED" -> Color.White
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    }

    val textToDisplay = if (schedule.dayOfWeek.isNotEmpty()) schedule.dayOfWeek else "-"

    val testTagVal = "schedule_cell_bd_${schedule.bdId}_wk_${schedule.weekNumber}"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                1.dp,
                if (schedule.status != "NONE") Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .testTag(testTagVal),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = textToDisplay,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = contentColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun CapabilityProfileScreen(
    viewModel: MainViewModel,
    selectedBd: BusinessDeveloper?,
    bds: List<BusinessDeveloper>
) {
    val assessments by viewModel.selectedBdAssessments.collectAsState()
    val latestAssessment by viewModel.selectedBdLatestAssessment.collectAsState()

    var showNewAssessmentDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Horizontal list of people
        Text(
            text = "Tracked Team Members",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(bds) { bd ->
                val isSelected = selectedBd?.id == bd.id
                val chipColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                val strokeColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(chipColor)
                        .border(1.3.dp, strokeColor, RoundedCornerShape(20.dp))
                        .clickable { viewModel.selectBd(bd) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("bd_profile_chip_${bd.name.replace(" ", "_")}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(android.graphics.Color.parseColor(bd.avatarColorHex)), CircleShape)
                        )
                        Text(
                            text = bd.name,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        if (selectedBd == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Please select or add a team member above.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Main user capability card
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Profile card showing overview & general average
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                Color(android.graphics.Color.parseColor(selectedBd.avatarColorHex)),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = selectedBd.name.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            selectedBd.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            selectedBd.role,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { viewModel.deleteBusinessDeveloper(selectedBd) },
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = L1Color)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Profile")
                                    }
                                    Button(
                                        onClick = { showNewAssessmentDialog = true },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("btn_assess_now")
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Assess", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Display aggregate level stats
                            if (latestAssessment != null) {
                                val latest = latestAssessment!!
                                val planAvg = (latest.marketKnowledge + latest.opportunitiesIdentified + latest.objectivesSet + latest.preparedToAchieve) / 4.0
                                val doAvg = (latest.jpAndDrivers + latest.brilliantPresentation + latest.nextStepsAdmin + latest.activationFocus) / 4.0
                                val reviewAvg = (latest.compliance + latest.salesPerformanceOutcomes + latest.personalReview) / 3.0

                                Text(
                                    "Competency Level Summary",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        ProgressIndicatorCard("PLAN", planAvg, L1Color)
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        ProgressIndicatorCard("DO", doAvg, L2Color)
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        ProgressIndicatorCard("REVIEW", reviewAvg, L3Color)
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No assessment data. Perform your first capability assessment now!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // Competency matrix & level descriptor expandable area
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Detailed Descriptor Matrix",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    "Tap rows to expand",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            CompetencyMatrixList(
                                latestAssessment = latestAssessment
                            )
                        }
                    }
                }

                // Historical Notes list
                if (assessments.isNotEmpty()) {
                    item {
                        Text(
                            "Coaching & Feedback Log",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    items(assessments) { assessment ->
                        AssessmentLogCard(
                            assessment = assessment,
                            onDelete = { viewModel.deleteAssessmentRecord(assessment) }
                        )
                    }
                }
            }
        }
    }

    if (showNewAssessmentDialog && selectedBd != null) {
        NewAssessmentDialog(
            bdName = selectedBd.name,
            onDismiss = { showNewAssessmentDialog = false },
            onSave = { mk, oi, os, pa, jd, bp, nsa, af, comp, spo, pr, notes, action ->
                viewModel.saveAssessment(
                    bdId = selectedBd.id,
                    marketKnowledge = mk,
                    opportunitiesIdentified = oi,
                    objectivesSet = os,
                    preparedToAchieve = pa,
                    jpAndDrivers = jd,
                    brilliantPresentation = bp,
                    nextStepsAdmin = nsa,
                    activationFocus = af,
                    compliance = comp,
                    salesPerformanceOutcomes = spo,
                    personalReview = pr,
                    coachingNotes = notes,
                    actionPlans = action
                )
                showNewAssessmentDialog = false
            }
        )
    }
}

@Composable
fun ProgressIndicatorCard(
    title: String,
    score: Double,
    tint: Color
) {
    val roundedScore = (score * 10).roundToInt() / 10.0
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(tint.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .border(0.5.dp, tint.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = tint)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "$roundedScore / 3.0",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((score / 3.0).toFloat().coerceIn(0f, 1f))
                    .background(tint, RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun CompetencyMatrixList(
    latestAssessment: CapabilityAssessment?
) {
    Column {
        CompetenceCategory.Plan.let { cat ->
            CategoryHeader(cat.title)
            CapabilityDefinitions.getCategoryList(cat).forEach { crit ->
                val activeScore = latestAssessment?.let { getScoreForCriterion(it, crit.id) } ?: 1
                CompetencyRow(crit = crit, activeScore = activeScore)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        CompetenceCategory.Do.let { cat ->
            CategoryHeader(cat.title)
            CapabilityDefinitions.getCategoryList(cat).forEach { crit ->
                val activeScore = latestAssessment?.let { getScoreForCriterion(it, crit.id) } ?: 1
                CompetencyRow(crit = crit, activeScore = activeScore)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        CompetenceCategory.Review.let { cat ->
            CategoryHeader(cat.title)
            CapabilityDefinitions.getCategoryList(cat).forEach { crit ->
                val activeScore = latestAssessment?.let { getScoreForCriterion(it, crit.id) } ?: 1
                CompetencyRow(crit = crit, activeScore = activeScore)
            }
        }
    }
}

@Composable
fun CategoryHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
}

@Composable
fun CompetencyRow(
    crit: CapabilityCriterion,
    activeScore: Int
) {
    var expanded by remember { mutableStateOf(false) }

    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "arrow_rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 6.dp)) {
                Text(
                    text = crit.title,
                    fontWeight = FontWeight.Normal,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Score Value Card
                LevelBadge(level = activeScore)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(18.dp)
                        .drawBehind {
                            this.drawContext.transform.rotate(arrowRotation)
                        }
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                DescriptorPillItem(level = 1, descText = crit.level1Desc, isActive = activeScore == 1)
                Spacer(modifier = Modifier.height(6.dp))
                DescriptorPillItem(level = 2, descText = crit.level2Desc, isActive = activeScore == 2)
                Spacer(modifier = Modifier.height(6.dp))
                DescriptorPillItem(level = 3, descText = crit.level3Desc, isActive = activeScore == 3)
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
}

@Composable
fun LevelBadge(level: Int) {
    val (label, containerColor, contentColor) = when (level) {
        3 -> Triple("Level 3 - Progressive", L3Color.copy(alpha = 0.15f), L3Color)
        2 -> Triple("Level 2 - Stable", L2Color.copy(alpha = 0.15f), L2Color)
        else -> Triple("Level 1 - Developing", L1Color.copy(alpha = 0.15f), L1Color)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DescriptorPillItem(
    level: Int,
    descText: String,
    isActive: Boolean
) {
    val activeBorderColor = when (level) {
        3 -> L3Color
        2 -> L2Color
        else -> L1Color
    }

    val activeBgColor = activeBorderColor.copy(alpha = 0.06f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) activeBgColor else Color.Transparent)
            .border(
                1.dp,
                if (isActive) activeBorderColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                RoundedCornerShape(6.dp)
            )
            .padding(10.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(activeBorderColor, CircleShape)
                )
                Text(
                    text = when (level) {
                        3 -> "Progressive (Level 3)"
                        2 -> "Stable (Level 2)"
                        else -> "Developing (Level 1)"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = activeBorderColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = descText,
                style = MaterialTheme.typography.bodySmall,
                color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun AssessmentLogCard(
    assessment: CapabilityAssessment,
    onDelete: () -> Unit
) {
    val dateStr = remember(assessment.dateMillis) {
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        sdf.format(Date(assessment.dateMillis))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dateStr,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete record",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (assessment.coachingNotes.isNotEmpty()) {
                Text(
                    "Coaching Notes:",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = assessment.coachingNotes,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )
            }

            if (assessment.actionPlans.isNotEmpty()) {
                Text(
                    "Agreed Action Plans:",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelSmall,
                    color = L2Color
                )
                Text(
                    text = assessment.actionPlans,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
fun AnalyticsScreen(
    bds: List<BusinessDeveloper>,
    allSchedules: List<CoachingSchedule>,
    viewModel: MainViewModel
) {
    var selectedReportBdId by remember { mutableStateOf<Int?>(null) }
    val schedulesFiltered = allSchedules.filter { it.bdId == (selectedReportBdId ?: -1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Overall Capability Index",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // circular rating index card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Total Dynamic Capability Score",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Based on team's current levels",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "Target: 3.0",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // custom graphics drawn with drawBehind of jetpack compose safely
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .drawBehind {
                            // draw background arc
                            drawArc(
                                color = Color.LightGray.copy(alpha = 0.2f),
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 16.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            )

                            // draw filled arc
                            drawArc(
                                brush = Brush.sweepGradient(listOf(L1Color, L2Color, L3Color)),
                                startAngle = 135f,
                                sweepAngle = 195f, // represents a nice average of ~2.1
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 16.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "2.17",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Stable Status",
                            style = MaterialTheme.typography.labelSmall,
                            color = L2Color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Plan", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text("2.35", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = L2Color)
                    }
                    VerticalDivider(modifier = Modifier.height(30.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Do", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text("2.05", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = L2Color)
                    }
                    VerticalDivider(modifier = Modifier.height(30.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Review", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text("2.10", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = L3Color)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Capability Strength Index",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "High-Performing Competencies",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                SimpleProgressBarRow("Prepared to achieve objectives", 2.65, L3Color)
                Spacer(modifier = Modifier.height(8.dp))
                SimpleProgressBarRow("JP and sales driver checks", 2.40, L3Color)
                Spacer(modifier = Modifier.height(8.dp))
                SimpleProgressBarRow("Compliance & Auditing Standards", 2.30, L3Color)

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Focus Development Areas (Opportunities to grow)",
                    style = MaterialTheme.typography.labelSmall,
                    color = L1Color,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                SimpleProgressBarRow("Next steps & CRM SFA admin", 1.55, L1Color)
                Spacer(modifier = Modifier.height(8.dp))
                SimpleProgressBarRow("Reviewing Sales performance hard data", 1.70, L1Color)
                Spacer(modifier = Modifier.height(8.dp))
                SimpleProgressBarRow("Brilliant sales standard presentations", 1.90, L2Color)
            }
        }
    }
}

@Composable
fun SimpleProgressBarRow(label: String, score: Double, tint: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "$score / 3.0",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = tint
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((score / 3.0).toFloat())
                    .background(tint, RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
fun AddBdDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, role: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#4CAF50") }

    val colorOptions = listOf("#4CAF50", "#2196F3", "#00BCD4", "#9C27B0", "#FF9800", "#E91E63", "#607D8B")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Tracked Person") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Person Name") },
                    modifier = Modifier.fillMaxWidth().testTag("add_bd_name_field"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role / Specialty") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Column {
                    Text("Profile Avatar Color", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        colorOptions.forEach { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (selectedColor == hex) 3.dp else 0.dp,
                                        color = if (selectedColor == hex) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = hex }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, role.ifBlank { "Business Developer" }, selectedColor) },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("add_bd_save_btn")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditScheduleDialog(
    bdName: String,
    schedule: CoachingSchedule,
    onDismiss: () -> Unit,
    onSave: (CoachingSchedule) -> Unit
) {
    var dayOfWeek by remember { mutableStateOf(schedule.dayOfWeek) }
    var status by remember { mutableStateOf(schedule.status) }

    val daysList = listOf("None", "Mon", "Tue", "Wed", "Thu", "Fri")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Edit Coaching Schedule")
                Text(
                    "Person: $bdName | Week: ${schedule.weekNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Day chips selector
                Column {
                    Text("Coaching Visit Weekday", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(daysList) { day ->
                            val isSelected = (day == "None" && dayOfWeek.isEmpty()) || (dayOfWeek == day)
                            FilterChip(
                                selected = isSelected,
                                onClick = { dayOfWeek = if (day == "None") "" else day },
                                label = { Text(day) }
                            )
                        }
                    }
                }

                // Status dropdown/cards switcher
                Column {
                    Text("Visit Status & Action", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Successful
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (status == "SUCCESSFUL") L3Color.copy(alpha = 0.1f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.3.dp,
                                    if (status == "SUCCESSFUL") L3Color else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    status = "SUCCESSFUL"
                                    if (dayOfWeek.isEmpty()) dayOfWeek = "Wed" // Default suggestion
                                }
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = L3Color)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("SUCCESS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = L3Color)
                        }

                        // Cancelled
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (status == "CANCELLED") L1Color.copy(alpha = 0.1f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.3.dp,
                                    if (status == "CANCELLED") L1Color else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    status = "CANCELLED"
                                    if (dayOfWeek.isEmpty()) dayOfWeek = "Fri" // Default cancel suggestion
                                }
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = L1Color)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("CANCELLED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = L1Color)
                        }

                        // Clear / None
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (status == "NONE") MaterialTheme.colorScheme.outline.copy(alpha = 0.1f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.3.dp,
                                    if (status == "NONE") MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    status = "NONE"
                                    dayOfWeek = ""
                                }
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("NOT SET", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(schedule.copy(dayOfWeek = dayOfWeek, status = status))
                },
                modifier = Modifier.testTag("save_schedule_dialog_btn")
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NewAssessmentDialog(
    bdName: String,
    onDismiss: () -> Unit,
    onSave: (
        mk: Int, oi: Int, os: Int, pa: Int,
        jd: Int, bp: Int, nsa: Int, af: Int,
        comp: Int, spo: Int, pr: Int,
        notes: String, action: String
    ) -> Unit
) {
    // Competency States
    var mk by remember { mutableStateOf(1) }
    var oi by remember { mutableStateOf(1) }
    var os by remember { mutableStateOf(1) }
    var pa by remember { mutableStateOf(1) }

    var jd by remember { mutableStateOf(1) }
    var bp by remember { mutableStateOf(1) }
    var nsa by remember { mutableStateOf(1) }
    var af by remember { mutableStateOf(1) }

    var comp by remember { mutableStateOf(1) }
    var spo by remember { mutableStateOf(1) }
    var pr by remember { mutableStateOf(1) }

    var coachingNotes by remember { mutableStateOf("") }
    var actionPlans by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close form")
                    }
                    Text(
                        text = "Capability Assessment for $bdName",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(
                        onClick = {
                            onSave(
                                mk, oi, os, pa,
                                jd, bp, nsa, af,
                                comp, spo, pr,
                                coachingNotes, actionPlans
                            )
                        },
                        modifier = Modifier.testTag("submit_assessment_form")
                    ) {
                        Text("Save")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Scrollable Form
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Plan Section Group
                    CategoryBlockHeader("Competency Segment 1: PLAN")

                    AssessmentScoringCard(
                        title = "Know my market & customer",
                        score = mk,
                        onScoreChange = { mk = it },
                        critId = "MARKET_KNOWLEDGE"
                    )

                    AssessmentScoringCard(
                        title = "Opportunities identified",
                        score = oi,
                        onScoreChange = { oi = it },
                        critId = "OPPORTUNITIES_IDENTIFIED"
                    )

                    AssessmentScoringCard(
                        title = "Objectives set",
                        score = os,
                        onScoreChange = { os = it },
                        critId = "OBJECTIVES_SET"
                    )

                    AssessmentScoringCard(
                        title = "Prepared to achieve objectives",
                        score = pa,
                        onScoreChange = { pa = it },
                        critId = "PREPARED_TO_ACHIEVE"
                    )

                    // DO Section Group
                    CategoryBlockHeader("Competency Segment 2: DO")

                    AssessmentScoringCard(
                        title = "JP and sales driver checks",
                        score = jd,
                        onScoreChange = { jd = it },
                        critId = "JP_AND_DRIVERS"
                    )

                    AssessmentScoringCard(
                        title = "Brilliant presentation",
                        score = bp,
                        onScoreChange = { bp = it },
                        critId = "BRILLIANT_PRESENTATION"
                    )

                    AssessmentScoringCard(
                        title = "Next steps & admin",
                        score = nsa,
                        onScoreChange = { nsa = it },
                        critId = "NEXT_STEPS_ADMIN"
                    )

                    AssessmentScoringCard(
                        title = "Activation focus",
                        score = af,
                        onScoreChange = { af = it },
                        critId = "ACTIVATION_FOCUS"
                    )

                    // REVIEW Section Group
                    CategoryBlockHeader("Competency Segment 3: REVIEW")

                    AssessmentScoringCard(
                        title = "Compliance",
                        score = comp,
                        onScoreChange = { comp = it },
                        critId = "COMPLIANCE"
                    )

                    AssessmentScoringCard(
                        title = "Sales performance outcomes",
                        score = spo,
                        onScoreChange = { spo = it },
                        critId = "SALES_PERFORMANCE_OUTCOMES"
                    )

                    AssessmentScoringCard(
                        title = "Personal review",
                        score = pr,
                        onScoreChange = { pr = it },
                        critId = "PERSONAL_REVIEW"
                    )

                    // Feedback form text areas
                    CategoryBlockHeader("Coaching Summary Notes")

                    OutlinedTextField(
                        value = coachingNotes,
                        onValueChange = { coachingNotes = it },
                        label = { Text("Coaching Evaluation Notes") },
                        modifier = Modifier.fillMaxWidth().height(120.dp).testTag("notes_field"),
                        maxLines = 5
                    )

                    OutlinedTextField(
                        value = actionPlans,
                        onValueChange = { actionPlans = it },
                        label = { Text("Agreed Action Plans / SMART Targets") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        maxLines = 5
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryBlockHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun AssessmentScoringCard(
    title: String,
    score: Int,
    onScoreChange: (Int) -> Unit,
    critId: String
) {
    val crit = remember(critId) { CapabilityDefinitions.criteria.find { it.id == critId }!! }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Score select rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Level 1
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (score == 1) L1Color.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            1.3.dp,
                            if (score == 1) L1Color else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onScoreChange(1) }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Developing (1)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (score == 1) L1Color else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Level 2
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (score == 2) L2Color.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            1.3.dp,
                            if (score == 2) L2Color else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onScoreChange(2) }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Stable (2)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (score == 2) L2Color else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Level 3
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (score == 3) L3Color.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            1.3.dp,
                            if (score == 3) L3Color else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onScoreChange(3) }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Progressive (3)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (score == 3) L3Color else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Real-time descriptor explanation representation
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                val currentText = when (score) {
                    3 -> crit.level3Desc
                    2 -> crit.level2Desc
                    else -> crit.level1Desc
                }
                Text(
                    text = "Standard expected: $currentText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Helpers
fun getScoreForCriterion(assessment: CapabilityAssessment, critId: String): Int {
    return when (critId) {
        "MARKET_KNOWLEDGE" -> assessment.marketKnowledge
        "OPPORTUNITIES_IDENTIFIED" -> assessment.opportunitiesIdentified
        "OBJECTIVES_SET" -> assessment.objectivesSet
        "PREPARED_TO_ACHIEVE" -> assessment.preparedToAchieve
        "JP_AND_DRIVERS" -> assessment.jpAndDrivers
        "BRILLIANT_PRESENTATION" -> assessment.brilliantPresentation
        "NEXT_STEPS_ADMIN" -> assessment.nextStepsAdmin
        "ACTIVATION_FOCUS" -> assessment.activationFocus
        "COMPLIANCE" -> assessment.compliance
        "SALES_PERFORMANCE_OUTCOMES" -> assessment.salesPerformanceOutcomes
        "PERSONAL_REVIEW" -> assessment.personalReview
        else -> 1
    }
}
