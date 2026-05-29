package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AptitudeQuestion
import com.example.data.models.VerbalQuestion
import com.example.data.repository.AppRepository
import com.example.data.gemini.GeminiService
import kotlinx.coroutines.launch

@Composable
fun PreparationScreen(modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    var selectedCategory by remember { mutableStateOf("Aptitude") } // Aptitude, Verbal, English-Lab

    // Aptitude state
    val aptitudeQuizzes = AppRepository.aptitudeQuizzes
    var curAptQuestionIdx by remember { mutableStateOf(0) }
    var selectedAptAnsIdx by remember { mutableStateOf<Int?>(null) }
    var aptAnswerSubmitted by remember { mutableStateOf(false) }
    var aptAiExplanation by remember { mutableStateOf("") }
    var aptExplanationLoading by remember { mutableStateOf(false) }

    // Verbal state
    val verbalQuizzes = AppRepository.verbalExercises
    var curVerbalQuestionIdx by remember { mutableStateOf(0) }
    var selectedVerbalAnsIdx by remember { mutableStateOf<Int?>(null) }
    var verbalAnswerSubmitted by remember { mutableStateOf(false) }
    var verbalAiExplanation by remember { mutableStateOf("") }
    var verbalExplanationLoading by remember { mutableStateOf(false) }

    // English Lab state
    var selectedLabPracticeArea by remember { mutableStateOf("Self Introduction") } // Self Introduction, HR Round, Client Communication
    var inputSpeechText by remember { mutableStateOf("") }
    var speechCoachLoading by remember { mutableStateOf(false) }
    var speechEvaluationResult by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // --- Navigation Tabs inside Preparation Tab ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                arrayOf("Aptitude", "Verbal", "English-Lab").forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedCategory = cat }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (cat == "English-Lab") "🗣️ English Lab" else if (cat == "Verbal") "📚 Verbal Academy" else "🧠 Aptitude",
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // --- SECTION A: APTITUDE ACADEMY ---
        if (selectedCategory == "Aptitude") {
            // Quantitative & Logical topics catalog list
            item {
                Text(
                    text = "📈 Topic Concept Notes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(AppRepository.quantitativeTopics + AppRepository.logicalTopics) { topic ->
                        Card(
                            modifier = Modifier
                                .width(150.dp)
                                .clickable {
                                    aptExplanationLoading = true
                                    coroutineScope.launch {
                                        val prompt = "Provide rapid shortcuts and formula tricks for aptitude: $topic."
                                        aptAiExplanation = GeminiService.callGemini(prompt)
                                        aptExplanationLoading = false
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                                Text(topic, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Tip: Tap to fetch formulas.", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Timed Mock Quiz Box
            item {
                val q = aptitudeQuizzes[curAptQuestionIdx]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "QUIZ: ${q.topic.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Question ${curAptQuestionIdx + 1}/${aptitudeQuizzes.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = q.question,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        // Option Radio Buttons
                        q.options.forEachIndexed { index, option ->
                            val isSelected = selectedAptAnsIdx == index
                            val isCorrect = q.correctIndex == index
                            val cardColor = when {
                                aptAnswerSubmitted && isCorrect -> Color(0xFF00FF87).copy(alpha = 0.2f)
                                aptAnswerSubmitted && isSelected && !isCorrect -> Color.Red.copy(alpha = 0.2f)
                                isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(cardColor)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        if (!aptAnswerSubmitted) {
                                            selectedAptAnsIdx = index
                                        }
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray, CircleShape)
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                )
                                Text(option, color = Color.White, fontSize = 13.sp)
                            }
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (!aptAnswerSubmitted) {
                                Button(
                                    onClick = {
                                        if (selectedAptAnsIdx != null) {
                                            aptAnswerSubmitted = true
                                            if (selectedAptAnsIdx == q.correctIndex) {
                                                AppRepository.addXp(20)
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = selectedAptAnsIdx != null
                                ) {
                                    Text("Verify Answer")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (curAptQuestionIdx < aptitudeQuizzes.size - 1) {
                                            curAptQuestionIdx++
                                        } else {
                                            curAptQuestionIdx = 0
                                        }
                                        selectedAptAnsIdx = null
                                        aptAnswerSubmitted = false
                                        aptAiExplanation = ""
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Next Question")
                                }
                            }

                            // AI Explanation button
                            Button(
                                onClick = {
                                    aptExplanationLoading = true
                                    coroutineScope.launch {
                                        val prompt = """
                                            Solve this arithmetic question step-by-step:
                                            Question: ${q.question}
                                            Options: ${q.options.joinToString()}
                                            Correct Answer: ${q.options[q.correctIndex]}
                                            Provide easy shortcut calculation techniques and relevant formulas.
                                        """.trimIndent()
                                        aptAiExplanation = GeminiService.callGemini(prompt)
                                        aptExplanationLoading = false
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Ask Coach")
                            }
                        }
                    }
                }
            }

            // Quick display of AI shortcuts
            if (aptExplanationLoading || aptAiExplanation.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("✨ AI Explanation & Formula Sheet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                            if (aptExplanationLoading) {
                                Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                                }
                            } else {
                                Text(aptAiExplanation, style = MaterialTheme.typography.bodySmall, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION B: VERBAL ACADEMY ---
        if (selectedCategory == "Verbal") {
            item {
                Text(
                    text = "📘 Vocabulary & Comprehension Builders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            item {
                val q = verbalQuizzes[curVerbalQuestionIdx]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "TOPIC: ${q.topic.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (q.passage != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(q.passage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Text(q.question, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)

                        q.options.forEachIndexed { index, option ->
                            val isSelected = selectedVerbalAnsIdx == index
                            val isCorrect = q.correctIndex == index
                            val tileColor = when {
                                verbalAnswerSubmitted && isCorrect -> Color(0xFF00FF87).copy(alpha = 0.2f)
                                verbalAnswerSubmitted && isSelected && !isCorrect -> Color.Red.copy(alpha = 0.2f)
                                isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(tileColor)
                                    .clickable {
                                        if (!verbalAnswerSubmitted) {
                                            selectedVerbalAnsIdx = index
                                        }
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Text(option, color = Color.White, fontSize = 13.sp)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (!verbalAnswerSubmitted) {
                                Button(
                                    onClick = {
                                        if (selectedVerbalAnsIdx != null) {
                                            verbalAnswerSubmitted = true
                                            if (selectedVerbalAnsIdx == q.correctIndex) {
                                                AppRepository.addXp(20)
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = selectedVerbalAnsIdx != null
                                ) {
                                    Text("Submit Answer")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        curVerbalQuestionIdx = (curVerbalQuestionIdx + 1) % verbalQuizzes.size
                                        selectedVerbalAnsIdx = null
                                        verbalAnswerSubmitted = false
                                        verbalAiExplanation = ""
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Next Question")
                                }
                            }

                            Button(
                                onClick = {
                                    verbalExplanationLoading = true
                                    coroutineScope.launch {
                                        val prompt = "Define Synonym/Antonym rules or sentence structure for verbal: ${q.question}. Breakdown spelling or phrase offsets of answer."
                                        verbalAiExplanation = GeminiService.callGemini(prompt)
                                        verbalExplanationLoading = false
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Explain")
                            }
                        }
                    }
                }
            }

            if (verbalExplanationLoading || verbalAiExplanation.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("📚 Literacy Explanation", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                            if (verbalExplanationLoading) {
                                Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                                }
                            } else {
                                Text(verbalAiExplanation, style = MaterialTheme.typography.bodySmall, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION C: ENGLISH COMMUNICATION LAB (SPEAKING COACH) ---
        if (selectedCategory == "English-Lab") {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🗣️ AI Speaking & Grammar Coach",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Choose an exercise below, type/draft your spoken dialogue answer to our prompt, and watch the coach grade your Fluency, Accent, and Confidence metrics.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            arrayOf("Self Introduction", "HR Round", "Client Email").forEach { area ->
                                val isSelected = selectedLabPracticeArea == area
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.background)
                                        .clickable {
                                            selectedLabPracticeArea = area
                                            inputSpeechText = ""
                                            speechEvaluationResult = null
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(area.split(" ")[0], color = if (isSelected) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Static Prompt based on selection
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "PRACTICE TASK PROMPT:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when (selectedLabPracticeArea) {
                                    "Self Introduction" -> "Introduce yourself in under 90 seconds. Speak about your major, key technologies you love, and your career dreams."
                                    "HR Round" -> "Answer: 'Why should we hire you as an intern over other applicants?' Explain your practical problem-solving milestones."
                                    else -> "Analyze a scenario where a client is angry about an interface delay. Compose a professional response acknowledging the latency."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Speech input box
                        OutlinedTextField(
                            value = inputSpeechText,
                            onValueChange = { inputSpeechText = it },
                            label = { Text("Draft/Type your spoken answer here...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = { Text("Example: My name is Alex, I am a third year CSE student focusing on high-scalability Android apps...") },
                            colors = OutlinedTextFieldDefaults.colors(focusedLabelColor = MaterialTheme.colorScheme.secondary)
                        )

                        // Submit
                        Button(
                            onClick = {
                                if (inputSpeechText.isNotEmpty()) {
                                    speechCoachLoading = true
                                    coroutineScope.launch {
                                        val prompt = """
                                            Analyze this student's response for the speaking practice task: '$selectedLabPracticeArea'.
                                            Student's draft: "$inputSpeechText"
                                            Grade their Fluency, Grammar, and Confidence level from 1 to 10.
                                            Provide bullet points of precise correction templates.
                                        """.trimIndent()
                                        speechEvaluationResult = GeminiService.callGemini(prompt)
                                        AppRepository.addXp(60)
                                        speechCoachLoading = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = inputSpeechText.isNotEmpty() && !speechCoachLoading
                        ) {
                            if (speechCoachLoading) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Analyze Speaking Performance")
                            }
                        }

                        // Evaluation Output Panel
                        if (speechEvaluationResult != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFF00FF87).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📊 Coach Performance Report Card",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF00FF87),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00FF87), modifier = Modifier.size(16.dp))
                                }

                                Text(
                                    text = speechEvaluationResult!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
