package com.example.data.models

data class UserProfile(
    val name: String = "",
    val college: String = "",
    val department: String = "",
    val year: String = "First Year",
    val cgpa: Double = 8.0,
    val skills: List<String> = emptyList(),
    val careerInterests: List<String> = emptyList(),
    val targetCompanies: List<String> = emptyList(),
    val xp: Int = 0,
    val level: Int = 1,
    val streak: Int = 1,
    val plan: String = "Free" // Free, Premium, Enterprise
)

data class MilestonesTrack(
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val category: String, // "DSA", "System Design", "Aptitude", "Projects", "Fundamentals", "HR"
    val tips: String
)

data class AptitudeQuestion(
    val id: String,
    val topic: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class VerbalQuestion(
    val id: String,
    val topic: String,
    val passage: String? = null,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class CodingProblem(
    val id: String,
    val title: String,
    val topic: String,
    val difficulty: String, // Easy, Medium, Hard
    val description: String,
    val starterCode: String,
    val solutionCode: String,
    val sampleInput: String,
    val sampleOutput: String
)

data class ProjectProposal(
    val id: String,
    val title: String,
    val difficulty: String, // Beginner, Intermediate, Advanced
    val tags: List<String>,
    val prd: String,
    val architecture: String,
    val schema: String,
    val apiDesign: String,
    val sourceTemplate: String,
    val deploymentGuide: String
)

data class ChatMessage(
    val role: String, // "user", "model", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class InterviewReport(
    val confidence: Int = 0,
    val communication: Int = 0,
    val technicalAccuracy: Int = 0,
    val behavioral: Int = 0,
    val summary: String = ""
)

data class InternshipListing(
    val id: String,
    val title: String,
    val company: String,
    val location: String,
    val type: String, // "Remote", "On-site", "Hybrid"
    val duration: String,
    val stipend: String,
    val isActive: Boolean = true,
    var applicationStatus: String = "Applied" // "Applied", "Interview", "Rejected", "Selected"
)

data class CareerTrend(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val percentageIncrease: String,
    val trendDirection: String // "UP", "DOWN", "FLAT"
)

data class PlaybookTopic(
    val title: String,
    val category: String,
    val formulas: List<String>,
    val shortcuts: List<String>,
    val starMethodSample: String? = null
)
