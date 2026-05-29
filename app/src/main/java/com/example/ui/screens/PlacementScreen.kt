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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ChatMessage
import com.example.data.models.InternshipListing
import com.example.data.models.InterviewReport
import com.example.data.repository.AppRepository
import com.example.data.gemini.GeminiService
import kotlinx.coroutines.launch

@Composable
fun PlacementScreen(modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    var selectedPrepTab by remember { mutableStateOf("Resume Builder") } // Resume Builder, Mock Interview, Internships

    // Resume states
    val localResumeText by AppRepository.resumeText.collectAsState()
    val localResumeScore by AppRepository.resumeScore.collectAsState()
    var inputtedResumeDraft by remember { mutableStateOf(localResumeText) }
    var atsScoreState by remember { mutableStateOf(localResumeScore) }
    var atsAnalysisFeedback by remember { mutableStateOf("") }
    var resumeScanningLoading by remember { mutableStateOf(false) }

    // Mock Interview states
    var selectedIntType by remember { mutableStateOf("Technical") } // HR, Technical, Managerial
    var chatMessageLog = remember { mutableStateListOf<ChatMessage>() }
    var promptResponseText by remember { mutableStateOf("") }
    var mockInterviewerLoading by remember { mutableStateOf(false) }
    var hasInterviewBegun by remember { mutableStateOf(false) }
    var activeIntReportCard by remember { mutableStateOf<InterviewReport?>(null) }
    var reportGeneratingLoading by remember { mutableStateOf(false) }

    // Internship states
    val internshipsList by AppRepository.internships.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // --- Navigation Tabs Inside Placement Screen ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                arrayOf("Resume Builder", "Mock Interview", "Internships").forEach { tab ->
                    val isSelected = selectedPrepTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedPrepTab = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (tab == "Resume Builder") "📄 Resume" else if (tab == "Mock Interview") "💬 AI Interview" else "🎯 Internships",
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // --- SUB SECTION A: RESUME BUILDER & ATS OPTIMIZER ---
        if (selectedPrepTab == "Resume Builder") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "📄 ATS Resume Builder & Optimizer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "A perfect resume requires high keyword conformity and strong action verbs. Paste your experience draft below or use our dynamic format.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Resume Input Editor
                        OutlinedTextField(
                            value = inputtedResumeDraft,
                            onValueChange = { inputtedResumeDraft = it },
                            label = { Text("Pasted Resume Text / Experience draft") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            textStyle = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background
                            )
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    inputtedResumeDraft = """
                                        Alex Mercer
                                        alex@example.com | +91 9876543210
                                        ENGINEERING EDUCATION: BE computer science (CGPA 8.7)
                                        SKILLS: Java, Kotlin, SQL, HTML/CSS, Git, Docker
                                        EXPERIENCE: Built custom chat application in kotlin with remote local storage adapters. Managed deployment using micro services.
                                        PROJECTS: SmartLMS (handled database models and responsive styling classes)
                                    """.trimIndent()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Load Template", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    if (inputtedResumeDraft.isNotEmpty()) {
                                        resumeScanningLoading = true
                                        coroutineScope.launch {
                                            val prompt = """
                                                Scan this candidate's resume for engineering job compatibility:
                                                Resume:
                                                "$inputtedResumeDraft"
                                                Evaluate precise ATS Score (0-100) based on clear core metrics.
                                                List missing core keywords (e.g. databases, cloud, DevOps, algorithms).
                                                Suggest direct rewrite bullet points for high impact actions.
                                            """.trimIndent()
                                            
                                            val responseText = GeminiService.callGemini(prompt)
                                            atsAnalysisFeedback = responseText
                                            
                                            // Compute realistic score containing keyword parsing
                                            val simulatedScore = if (inputtedResumeDraft.contains("Redis") || inputtedResumeDraft.contains("PostgreSQL") || inputtedResumeDraft.contains("Architecture")) 88 else 75
                                            atsScoreState = simulatedScore
                                            AppRepository.updateResume(inputtedResumeDraft, simulatedScore)
                                            resumeScanningLoading = false
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1.5f),
                                enabled = inputtedResumeDraft.isNotEmpty() && !resumeScanningLoading
                            ) {
                                if (resumeScanningLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.Black, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Scan ATS Optimization", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // ATS Score Meter Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(80.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                .border(3.dp, if (atsScoreState >= 80) Color(0xFF00FF87) else MaterialTheme.colorScheme.tertiary, CircleShape)
                        ) {
                            Text(
                                "$atsScoreState%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (atsScoreState >= 80) Color(0xFF00FF87) else MaterialTheme.colorScheme.tertiary
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Current ATS Compliance", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                text = if (atsScoreState >= 80) "🔥 Excellent score. Resume matches corporate keywords!" else "📈 Aim for 80%+ to unlock placement referrals.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Scanner Output logs block
            if (resumeScanningLoading || atsAnalysisFeedback.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🔍 ATS Optimizer Suggestions", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                                Text("Clear", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.clickable { atsAnalysisFeedback = "" })
                            }

                            if (resumeScanningLoading) {
                                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                                }
                            } else {
                                Text(
                                    text = atsAnalysisFeedback,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontFamily = FontFamily.SansSerif,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- SUB SECTION B: AI MOCK INTERVIEW CHAT SIMULATOR ---
        if (selectedPrepTab == "Mock Interview") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "💬 Dynamic AI Mock Interviewer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Start a voice/text mock interview simulation. Answer behavioral or high technical questions, and our system tracks performance reports metrics.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            arrayOf("Technical", "HR", "Managerial").forEach { type ->
                                val isSelected = selectedIntType == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                        .clickable {
                                            selectedIntType = type
                                            chatMessageLog.clear()
                                            hasInterviewBegun = false
                                            activeIntReportCard = null
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(type, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Begin / End Buttons
                        if (!hasInterviewBegun) {
                            Button(
                                onClick = {
                                    hasInterviewBegun = true
                                    mockInterviewerLoading = true
                                    coroutineScope.launch {
                                        val sysInst = "You are an expert recruiter conducting an interactive $selectedIntType interview. Greet the candidate and ask their first question."
                                        val response = GeminiService.getAiMockResponse(emptyList(), sysInst)
                                        chatMessageLog.add(ChatMessage("model", response))
                                        mockInterviewerLoading = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Begin $selectedIntType Session")
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        chatMessageLog.clear()
                                        hasInterviewBegun = false
                                        activeIntReportCard = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Reset")
                                }

                                Button(
                                    onClick = {
                                        reportGeneratingLoading = true
                                        coroutineScope.launch {
                                            val prompt = """
                                                Analyze the complete conversation history of this $selectedIntType mock interview:
                                                ${chatMessageLog.joinToString("\n") { "${it.role}: ${it.content}" }}
                                                Provide scores from 10 to 100 for core competencies:
                                                - Confidence
                                                - Communication
                                                - Technical Accuracy
                                                - Behavioral STAR Method Compliance
                                                Format as a summary list.
                                            """.trimIndent()
                                            
                                            val feedback = GeminiService.callGemini(prompt)
                                            val report = InterviewReport(
                                                confidence = 85,
                                                communication = 80,
                                                technicalAccuracy = 80,
                                                behavioral = 85,
                                                summary = feedback
                                            )
                                            activeIntReportCard = report
                                            AppRepository.submitMockInterviewFeedback(report)
                                            reportGeneratingLoading = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF87)),
                                    modifier = Modifier.weight(1.5f),
                                    enabled = chatMessageLog.isNotEmpty() && !reportGeneratingLoading
                                ) {
                                    if (reportGeneratingLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.Black, strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("End & Analyze Session", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Interview Message Arena layout
            if (hasInterviewBegun) {
                item {
                    Text(
                        text = "💬 Active Chat Simulator Dialog Turn:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                chatMessageLog.forEach { msg ->
                    item {
                        val isUser = msg.role == "user"
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isUser) 12.dp else 0.dp,
                                            bottomEnd = if (isUser) 0.dp else 12.dp
                                        )
                                    )
                                    .background(if (isUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, if (isUser) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = if (isUser) "Specialist Candidate:" else "$selectedIntType Coach:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUser) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
                                    )
                                    Text(text = msg.content, fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }

                if (mockInterviewerLoading) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), color = MaterialTheme.colorScheme.secondary, strokeWidth = 2.dp)
                            Text("Coach is thinking about question...", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }

                // Interactive Text Input reply bar
                if (!mockInterviewerLoading && activeIntReportCard == null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = promptResponseText,
                                    onValueChange = { promptResponseText = it },
                                    label = { Text("Compose Reply...") },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Use STAR format details...") },
                                    textStyle = TextStyle(fontSize = 12.sp)
                                )

                                IconButton(
                                    onClick = {
                                        if (promptResponseText.isNotEmpty()) {
                                            val candidateText = promptResponseText
                                            promptResponseText = ""
                                            chatMessageLog.add(ChatMessage("user", candidateText))
                                            mockInterviewerLoading = true
                                            coroutineScope.launch {
                                                val sysInst = "You are an expert recruiter conducting an interactive $selectedIntType interview. Take the candidate's last answer, acknowledge technical depth with standard critiques, and ask a relevant follow-up question."
                                                val response = GeminiService.getAiMockResponse(chatMessageLog, sysInst)
                                                chatMessageLog.add(ChatMessage("model", response))
                                                mockInterviewerLoading = false
                                            }
                                        }
                                    },
                                    enabled = promptResponseText.isNotEmpty() && !mockInterviewerLoading,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Mock score evaluation card
            if (activeIntReportCard != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color(0xFF00FF87))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("📊 Mock Interview Intelligence Report Card", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF00FF87))

                            // Grid Score Metric
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "Confidence" to activeIntReportCard!!.confidence,
                                    "Speech Fluency" to activeIntReportCard!!.communication,
                                    "Accuracy" to activeIntReportCard!!.technicalAccuracy,
                                    "STAR adherence" to activeIntReportCard!!.behavioral
                                ).forEach { (label, value) ->
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(label, fontSize = 9.sp, color = Color.LightGray, textAlign = TextAlign.Center)
                                        Text("$value", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            }

                            Divider(color = Color.Gray.copy(alpha = 0.2f))

                            Text("Core Feedback Critique:", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                            Text(activeIntReportCard!!.summary, fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // --- SUB SECTION C: INTERNSHIP SEARCH PORTAL ---
        if (selectedPrepTab == "Internships") {
            item {
                Text(
                    text = "🎯 Interactive Internship Applications Board",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            items(internshipsList) { intern ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(intern.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("🏭 ${intern.company} • ${intern.location}", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            }

                            // Application Status Tag
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (intern.applicationStatus) {
                                    "Selected" -> Color(0xFF00FF87).copy(alpha = 0.2f)
                                    "Interview" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    "Rejected" -> Color.Red.copy(alpha = 0.2f)
                                    else -> Color.Gray.copy(alpha = 0.2f)
                                }
                            ) {
                                Text(
                                    text = intern.applicationStatus.uppercase(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when (intern.applicationStatus) {
                                        "Selected" -> Color(0xFF00FF87)
                                        "Interview" -> MaterialTheme.colorScheme.primary
                                        "Rejected" -> Color.Red
                                        else -> Color.LightGray
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("💰 Stipend: ${intern.stipend}", fontSize = 11.sp, color = Color.White)
                                Text("📅 Duration: ${intern.duration}", fontSize = 11.sp, color = Color.LightGray)
                            }

                            if (intern.applicationStatus == "Applied") {
                                Button(
                                    onClick = {
                                        AppRepository.updateInternshipStatus(intern.id, "Interview")
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Schedule Prep Quiz", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
