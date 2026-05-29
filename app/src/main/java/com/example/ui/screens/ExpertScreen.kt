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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ProjectProposal
import com.example.data.repository.AppRepository

@Composable
fun ExpertScreen(modifier: Modifier = Modifier) {
    var activeCategory by remember { mutableStateOf("Core CS") } // Core CS, Project Hub, Playbooks

    var expandedProjectIndex by remember { mutableStateOf<Int?>(null) }
    var selectedCsDomain by remember { mutableStateOf("DBMS") } // DBMS, OS, Network, OOP

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // --- Tab Selection Row ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                arrayOf("Core CS", "Project Hub", "Playbooks").forEach { cat ->
                    val isSelected = activeCategory == cat
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { activeCategory = cat }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (cat == "Core CS") "📑 Core CS & Cloud" else if (cat == "Project Hub") "🏗️ Project Hub" else "📚 Playbooks",
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // --- SUB SECTION A: CORE CS & DEVOPS PATHS ---
        if (activeCategory == "Core CS") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "💡 Computer Science Fundamentals",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        // CS Selector Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AppRepository.csFundamentals.keys.forEach { domain ->
                                val isSelected = selectedCsDomain == domain
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                        .clickable { selectedCsDomain = domain }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(domain, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // DOMAIN LIST BULLETS
                        val list = AppRepository.csFundamentals[selectedCsDomain] ?: emptyList()
                        list.forEach { itemText ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                )
                                Text(itemText, color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Cloud & DevOps Panel
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Cloud, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            }
                            Column {
                                Text("🐳 DevOps & Cloud Architectures", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Linux, Containerization, and AWS Services", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        AppRepository.cloudDevOps.forEach { text ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Text(text, color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // --- SUB SECTION B: PROJECT HUB WITH DIAGRAMS & SCHEMAS ---
        if (activeCategory == "Project Hub") {
            item {
                Text(
                    text = "🏗️ Premium Open-Source Project Designs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            val projects = AppRepository.projectsDatabase
            projects.forEachIndexed { index, proj ->
                item {
                    val isExpanded = expandedProjectIndex == index
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedProjectIndex = if (isExpanded) null else index
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, if (isExpanded) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(proj.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (proj.difficulty == "Advanced") Color.Red.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = proj.difficulty,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (proj.difficulty == "Advanced") Color(0xFFF93B57) else MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Tags row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                proj.tags.forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.background)
                                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(tag, fontSize = 9.sp, color = Color.LightGray)
                                    }
                                }
                            }

                            Text(proj.prd, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            if (!isExpanded) {
                                Text("👇 Tap to expansion architecture diagram, DB designs, and APIs guide...", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                            }

                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier.padding(top = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Simulated Microservices Architecture Card
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("📡 SYSTEM ARCHITECTURE MODEL:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        Text(proj.architecture, style = MaterialTheme.typography.bodySmall, color = Color.White)
                                    }

                                    // DB Designs Schema
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(10.dp))
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("🗄️ DATABASE SCHEMA:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                        Text(proj.schema, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFFE2E8F0))
                                    }

                                    // Contract API endpoints design
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(10.dp))
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("⚡ REST API ENDPOINTS CONTRACT:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                                        Text(proj.apiDesign, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFFFDE047))
                                    }

                                    // Source Template Snip
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("📦 STARTER SOURCE CODE:", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        Text(proj.sourceTemplate, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.LightGray)
                                    }

                                    // Deployment steps
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp))
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("🐳 CLOUD ENGINE DEPLOYMENT:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00FF87), fontWeight = FontWeight.Bold)
                                        Text(proj.deploymentGuide, style = MaterialTheme.typography.bodySmall, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SUB SECTION C: REVOLUTIONARY SUCCESS PLAYBOOKS ---
        if (activeCategory == "Playbooks") {
            item {
                Text(
                    text = "📚 Placement Success Playbooks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            AppRepository.playbooks.forEach { playbook ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(playbook.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = playbook.category.uppercase(),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Essentials bullet list
                            Text("Standard Formulas/Principles:", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                            playbook.formulas.forEach { f ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.LightGray)
                                    Text(f, fontSize = 12.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                                }
                            }

                            // Key Tricks list
                            Text("Playbook Strategy Shortcuts:", fontSize = 10.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                            playbook.shortcuts.forEach { shortcut ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.tertiary)
                                    Text(shortcut, fontSize = 12.sp, color = Color.LightGray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
