package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.UserProfile
import com.example.data.repository.AppRepository
import com.example.data.gemini.GeminiService
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    val userProfile by AppRepository.userProfile.collectAsState()
    val milestones by AppRepository.milestones.collectAsState()
    val criScore = remember(userProfile, milestones) { AppRepository.calculateCRI() }

    var selectedRoadmapTab by remember { mutableStateOf("Product-Based") } // Product-Based, Service-Based, Startup
    var aiGeneratorLoading by remember { mutableStateOf(false) }
    var aiGeneratedRoadmapText by remember { mutableStateOf("") }
    var showOnboardingDialog by remember { mutableStateOf(false) }

    // Onboarding form state
    var editName by remember { mutableStateOf(userProfile.name) }
    var editCollege by remember { mutableStateOf(userProfile.college) }
    var editDept by remember { mutableStateOf(userProfile.department) }
    var editCgpa by remember { mutableStateOf(userProfile.cgpa.toString()) }
    var editSkills by remember { mutableStateOf(userProfile.skills.joinToString(", ")) }
    var editCompanies by remember { mutableStateOf(userProfile.targetCompanies.joinToString(", ")) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // --- High Impact Header Card with Radial Gradient Banner ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Welcome back, Specialist",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = userProfile.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        
                        // Edit / Onboarding Button
                        IconButton(
                            onClick = {
                                editName = userProfile.name
                                editCollege = userProfile.college
                                editDept = userProfile.department
                                editCgpa = userProfile.cgpa.toString()
                                editSkills = userProfile.skills.joinToString(", ")
                                editCompanies = userProfile.targetCompanies.joinToString(", ")
                                showOnboardingDialog = true
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = Color.White
                            )
                        }
                    }

                    Text(
                        text = "🎓 ${userProfile.college}  •  ${userProfile.department} (${userProfile.year})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    // Staggered Status Badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
                        ) {
                            Text(
                                text = "🔥 STREAK: ${userProfile.streak} days",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(
                                text = "🏅 LEVEL ${userProfile.level}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF00FF87).copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, Color(0xFF00FF87))
                        ) {
                            Text(
                                text = userProfile.plan.uppercase(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF00FF87),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Level XP Indicator
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${userProfile.xp % 500} / 500 XP to next level",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Total XP: ${userProfile.xp}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        LinearProgressIndicator(
                            progress = { (userProfile.xp % 500) / 500f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }

        // --- Career Readiness Index (CRI) Main Meter ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "⚡ Career Readiness Index (CRI)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Big Arc Score Meter
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(100.dp)
                                .background(
                                    MaterialTheme.colorScheme.background,
                                    shape = CircleShape
                                )
                                .border(
                                    width = 4.dp,
                                    brush = Brush.sweepGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary,
                                            MaterialTheme.colorScheme.primary
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$criScore",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "/ 100",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Score Analysis Legend Breakdown
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = when {
                                    criScore >= 80 -> "🔥 Placement Champion Ready!"
                                    criScore >= 60 -> "📈 Solid Preparations (Targeting 80+)"
                                    else -> "⚠️ High Gap Alert. Resume & coding required."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (criScore >= 70) Color(0xFF00FF87) else MaterialTheme.colorScheme.tertiary
                            )

                            Text(
                                text = "Your score updates dynamically as you solve coding problems, pass quizzes, and review resumes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // --- Dynamic Custom Roadmap Engine with AI trigger ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🚀 Career Roadmap Tracks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        // AI button
                        Button(
                            onClick = {
                                aiGeneratorLoading = true
                                coroutineScope.launch {
                                    val prompt = """
                                        Generate a detailed structured Career Roadmap for a student preparing for $selectedRoadmapTab.
                                        Student Profile:
                                        - CGPA: ${userProfile.cgpa}
                                        - Department: ${userProfile.department}
                                        - Key Skills: ${userProfile.skills.joinToString()}
                                        - Target Companies: ${userProfile.targetCompanies.joinToString()}
                                        - Prep Year: ${userProfile.year}
                                        Provide actionable 3-phase milestones with formulas and concrete topic names. Ensure beautiful Markdown formatting.
                                    """.trimIndent()
                                    
                                    val result = GeminiService.callGemini(prompt)
                                    aiGeneratedRoadmapText = result
                                    aiGeneratorLoading = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("AI Plan", fontSize = 12.sp)
                        }
                    }

                    // Roadmap Segment Selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        arrayOf("Product-Based", "Service-Based", "Startup").forEach { tab ->
                            val isSelected = selectedRoadmapTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedRoadmapTab = tab }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab.split("-")[0],
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Loading Indicator or AI text
                    if (aiGeneratorLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                        }
                    } else if (aiGeneratedRoadmapText.isNotEmpty()) {
                        // AI Response Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    RoundedCornerShape(12.dp)
                                )
                                .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "✨ Personalized AI Roadmap Success",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Clear",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.clickable { aiGeneratedRoadmapText = "" },
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                                Text(
                                    text = aiGeneratedRoadmapText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Native Prebuilt Roadmap Tracker Cards
                    Text(
                        text = "Interactive Milestones (Click to Complete & earn +40 XP):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )

                    val filteredMilestones = milestones.filter {
                        when (selectedRoadmapTab) {
                            "Product-Based" -> it.category in listOf("DSA", "System Design", "Fundamentals")
                            "Service-Based" -> it.category in listOf("Aptitude", "HR")
                            else -> it.category in listOf("Projects", "DSA")
                        }
                    }

                    filteredMilestones.forEach { milestone ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (milestone.isCompleted) Color(0xFF00FF87) else Color.White.copy(alpha = 0.2f))
                                    .clickable {
                                        if (!milestone.isCompleted) {
                                            AppRepository.completeMilestone(milestone.title)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (milestone.isCompleted) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = milestone.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (milestone.isCompleted) Color.Gray else Color.White
                                )
                                Text(
                                    text = milestone.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "💡 Tip: ${milestone.tips}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.background
                            ) {
                                Text(
                                    text = milestone.category.uppercase(),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Gamification Badges Drawer Panel ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🛡️ Earned Badges",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            BadgeItem("Ninja Coder", Icons.Default.Code, "Solve 1st DSA Problem", true)
                        }
                        item {
                            BadgeItem("Quant Wizard", Icons.Default.Calculate, "Score 80%+ on Quant", true)
                        }
                        item {
                            BadgeItem("Speak Coach", Icons.Default.RecordVoiceOver, "Pass English speech test", false)
                        }
                        item {
                            BadgeItem("Perfect Resume", Icons.Default.Task, "Scan resume on ATS", true)
                        }
                    }
                }
            }
        }
    }

    // --- Onboarding / Profile Dialog ---
    if (showOnboardingDialog) {
        AlertDialog(
            onDismissRequest = { showOnboardingDialog = false },
            title = { Text("Setup Onboarding Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editCollege,
                        onValueChange = { editCollege = it },
                        label = { Text("Engineering College") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDept,
                        onValueChange = { editDept = it },
                        label = { Text("Department") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editCgpa,
                        onValueChange = { editCgpa = it },
                        label = { Text("CGPA") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editSkills,
                        onValueChange = { editSkills = it },
                        label = { Text("Skills (comma-separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editCompanies,
                        onValueChange = { editCompanies = it },
                        label = { Text("Target Companies") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedCgpa = editCgpa.toDoubleOrNull() ?: 8.0
                        val parsedSkills = editSkills.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val parsedCompanies = editCompanies.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        
                        AppRepository.updateProfile(
                            userProfile.copy(
                                name = editName,
                                college = editCollege,
                                department = editDept,
                                cgpa = parsedCgpa,
                                skills = parsedSkills,
                                targetCompanies = parsedCompanies
                            )
                        )
                        showOnboardingDialog = false
                    }
                ) {
                    Text("Save Profile")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOnboardingDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BadgeItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, unlocked: Boolean) {
    Box(
        modifier = Modifier
            .width(110.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (unlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.background)
            .border(
                width = 1.dp,
                color = if (unlocked) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (unlocked) MaterialTheme.colorScheme.secondary else Color.Gray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (unlocked) Color.Black else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (unlocked) Color.White else Color.Gray,
                textAlign = TextAlign.Center
            )
            Text(
                text = desc,
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 10.sp
            )
        }
    }
}
