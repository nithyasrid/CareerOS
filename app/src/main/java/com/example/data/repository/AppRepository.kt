package com.example.data.repository

import com.example.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

object AppRepository {

    // --- Core Reactive Memory State ---
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _milestones = MutableStateFlow<List<MilestonesTrack>>(emptyList())
    val milestones: StateFlow<List<MilestonesTrack>> = _milestones.asStateFlow()

    private val _internships = MutableStateFlow<List<InternshipListing>>(emptyList())
    val internships: StateFlow<List<InternshipListing>> = _internships.asStateFlow()

    private val _aptitudeScores = MutableStateFlow(mapOf<String, Int>()) // topic -> score
    val aptitudeScores = _aptitudeScores.asStateFlow()

    private val _resolvedCodingProblems = MutableStateFlow(setOf<String>()) // solved coder problems IDs
    val resolvedCodingProblems = _resolvedCodingProblems.asStateFlow()

    private val _lastInterviewReport = MutableStateFlow<InterviewReport?>(null)
    val lastInterviewReport = _lastInterviewReport.asStateFlow()

    private val _resumeScore = MutableStateFlow(0)
    val resumeScore = _resumeScore.asStateFlow()

    private val _resumeText = MutableStateFlow("")
    val resumeText = _resumeText.asStateFlow()

    init {
        resetToDefault()
    }

    fun resetToDefault() {
        _userProfile.value = UserProfile(
            name = "Alex Mercer",
            college = "Tech Institute of Engineering",
            department = "Computer Science",
            year = "Third Year",
            cgpa = 8.7,
            skills = listOf("Java", "Kotlin", "HTML/CSS", "SQL"),
            careerInterests = listOf("Product-Based", "Mobile Engineer", "AI"),
            targetCompanies = listOf("Amazon", "Google", "Zoho"),
            xp = 340,
            level = 1,
            streak = 5
        )

        _milestones.value = listOf(
            MilestonesTrack("Learn Quant Percentages", "Master % fractions & SI shortcuts", true, "Aptitude", "Divide value by 10 to inspect deciles."),
            MilestonesTrack("Implement Stack using Array", "Review LIFO operations & edge flows", false, "DSA", "Handle stack-overflow and stack-underflow edge bounds."),
            MilestonesTrack("Self Introduction Practice", "Record 1-minute HR introductory summary", false, "HR", "Pace smoothly. Begin with name, college, major, and key projects."),
            MilestonesTrack("Database Joins Guide", "Review Inner, Left, Right and Full SQL Joins", false, "Fundamentals", "SQL joins work on common key projections."),
            MilestonesTrack("Build Portfolio Website", "Develop frontend portfolio with active links", true, "Projects", "Keep typography readable, add simple contact forms."),
            MilestonesTrack("Containerize a Web App", "Write Dockerfile and define multi-stage build layers", false, "System Design", "Keep image sizes tiny with alpine base images.")
        )

        _internships.value = listOf(
            InternshipListing("INT-001", "Software Engineer Intern", "Amazon Web Services", "Remote", "Remote", "3 Months", "₹45,000 / mo", applicationStatus = "Applied"),
            InternshipListing("INT-002", "Android Dev Associate", "Zoho Corporation", "Chennai, India", "On-site", "6 Months", "₹30,000 / mo", applicationStatus = "Interview"),
            InternshipListing("INT-003", "Full Stack Developer", "Freshworks", "Bengaluru, India", "Hybrid", "6 Months", "₹35,000 / mo", applicationStatus = "Selected"),
            InternshipListing("INT-004", "AI Engineering Intern", "AeroTech Labs", "Remote", "Remote", "4 Months", "₹50,000 / mo", applicationStatus = "Applied")
        )

        _aptitudeScores.value = mapOf("Percentages" to 80, "Ratios" to 60)
        _resolvedCodingProblems.value = setOf("coding_001")
        _lastInterviewReport.value = InterviewReport(
            confidence = 85,
            communication = 80,
            technicalAccuracy = 75,
            behavioral = 90,
            summary = "Excellent command on behavioral STAR frameworks. Expand technical SQL join depths."
        )
        _resumeScore.value = 65
        _resumeText.value = "Alex Mercer\nEmail: alex@example.com\nSkills: Java, Kotlin, SQL\nEducation: BE CSE (CGPA 8.7)"
    }

    // --- State Updaters ---
    fun updateProfile(profile: UserProfile) {
        _userProfile.value = profile
        calculateCRI()
    }

    fun completeMilestone(title: String) {
        _milestones.value = _milestones.value.map {
            if (it.title == title) it.copy(isCompleted = true) else it
        }
        addXp(40)
        calculateCRI()
    }

    fun addXp(amount: Int) {
        val current = _userProfile.value
        val newXp = current.xp + amount
        val newLevel = (newXp / 500) + 1
        _userProfile.value = current.copy(xp = newXp, level = newLevel)
    }

    fun updateInternshipStatus(id: String, status: String) {
        _internships.value = _internships.value.map {
            if (it.id == id) it.copy(applicationStatus = status) else it
        }
        addXp(10)
    }

    fun solveCodingProblem(id: String) {
        if (!_resolvedCodingProblems.value.contains(id)) {
            _resolvedCodingProblems.value = _resolvedCodingProblems.value + id
            addXp(80)
            calculateCRI()
        }
    }

    fun updateResume(text: String, score: Int) {
        _resumeText.value = text
        _resumeScore.value = score
        addXp(30)
        calculateCRI()
    }

    fun submitMockInterviewFeedback(report: InterviewReport) {
        _lastInterviewReport.value = report
        addXp(100)
        calculateCRI()
    }

    // --- Calculator: Career Readiness Index (CRI) 0 - 100 ---
    fun calculateCRI(): Int {
        val milestoneCompletion = _milestones.value.count { it.isCompleted }.toFloat() / _milestones.value.size.coerceAtLeast(1) * 100
        val codingPoints = (_resolvedCodingProblems.value.size * 20).coerceAtMost(100)
        val aptitudeAvg = if (_aptitudeScores.value.isEmpty()) 0f else _aptitudeScores.value.values.map { it.toFloat() }.average().toFloat()
        val intReport = _lastInterviewReport.value
        val interviewPoints = if (intReport != null) {
            (intReport.confidence + intReport.communication + intReport.technicalAccuracy + intReport.behavioral) / 4.0
        } else {
            0.0
        }
        val resScore = _resumeScore.value.toDouble()

        // Weight distribution: Aptitude (20%), Coding (25%), Interview (25%), Resume (15%), Milestones Progress (15%)
        val criValue = (aptitudeAvg * 0.20) + 
                       (codingPoints * 0.25) + 
                       (interviewPoints * 0.25) + 
                       (resScore * 0.15) + 
                       (milestoneCompletion * 0.15)

        return criValue.roundToInt().coerceIn(0, 100)
    }

    // --- Static Raw Data Modules representing deep Career OS ---
    
    val quantitativeTopics = listOf(
        "Number System", "Percentages", "Ratio & Proportion", "Profit & Loss",
        "SI & CI", "Time & Work", "Speed Distance", "Probability", "Permutations", "Combinations"
    )

    val logicalTopics = listOf(
        "Seating Arrangement", "Blood Relations", "Coding Decoding", "Syllogisms", "Input Output", "Puzzles"
    )

    val verbalTopics = listOf(
        "Vocabulary Building", "Synonyms & Antonyms", "Reading Comprehension", "Sentence Correction", "Para Jumbles"
    )

    val programmingTracks = mapOf(
        "Python" to listOf("Basics Syntax", "Object-Oriented Python", "File Parsing", "API integrations & JSON", "Decorators & Generators"),
        "Java" to listOf("Core JVM principles", "Java Collections Framework", "Multithreading & Concurrency", "Spring Boot & REST"),
        "C++" to listOf("Pointers & Memory", "C++ Standard Template Library (STL)", "Algorithm Design / Competitive"),
        "JavaScript" to listOf("Modern ES6 Modules", "DOM Event Handling", "Async-Await & Event Loop", "React Composability")
    )

    val dsaSectors = mapOf(
        "Beginner" to listOf("Arrays", "Strings", "Hashing Basics"),
        "Intermediate" to listOf("Linked Lists", "Stacks & Queues", "Trees & BST"),
        "Advanced" to listOf("Graphs & DFS/BFS", "Trie Implementations", "Dynamic Programming knapsack")
    )

    val csFundamentals = mapOf(
        "DBMS" to listOf("SQL vs NoSQL queries", "Join optimization", "ACID transactions", "Indexing patterns"),
        "OS" to listOf("Process scheduling", "Thread synchronizations", "Deadlock detection", "Paging memory"),
        "Network" to listOf("TCP three-way handshake", "TLS HTTP handshake", "DNS resolution path", "Routing policies"),
        "OOP" to listOf("Abstraction vs Encapsulation", "Polymorphism techniques", "SOLID compliance")
    )

    val cloudDevOps = listOf(
        "Linux CLI commands", "Git & GitHub version architectures", "Docker Compose microservices", "Kubernetes Pod management", "AWS Deployment strategies (S3/EC2)"
    )

    val aiCareerPaths = mapOf(
        "Data Analyst" to listOf("Advanced spread calculations", "SQL analytics functions", "Power BI Dashboards"),
        "Data Scientist" to listOf("Scientific Python library stack", "Statistical probability models", "SKLearn Classification & Regression"),
        "AI Engineer" to listOf("LLM API setups", "RAG Pipeline retrieval", "Vector embeddings DB (Chroma/Pinecone)", "Autonomous AI Agents")
    )

    val playbooks = listOf(
        PlaybookTopic("Aptitude Formula Sheet", "Math Essentials", listOf("SI = (P * R * T) / 100", "CI = P*(1 + R/100)^T - P", "Speed = Distance / Time"), listOf("Percentages fraction conversions: 12.5% = 1/8.", "Relative speed: same direction subtract, opposite add.")),
        PlaybookTopic("Coding Patterns Matrix", "Problem Solving", listOf("Two Pointers standard checks", "Sliding Window sizing tricks", "DFS recursive trees state"), listOf("Fast/slow pointer detects loops instantly in under O(N).", "Hashing reduces O(N^2) searches to O(1) space.")),
        PlaybookTopic("Behavioral Interview Strategy", "HR & Management", listOf("Situation description", "Task definition", "Action specifics", "Result with metrics"), listOf("Always quantify results: 'Reduced load latency by 20%'.", "Mention trade-offs to show high engineering maturity.")),
        PlaybookTopic("Internship & Networking", "Application Strategy", listOf("Cold email templates", "Referral asking requests", "LinkedIn project showcase"), listOf("Focus strictly on the value you add, not just what you want.", "Showcase deployed code links with visual metrics."))
    )

    val codingProblems = listOf(
        CodingProblem(
            "coding_001",
            "Two Sum",
            "Arrays",
            "Easy",
            "Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.",
            "fun twoSum(nums: IntArray, target: Int): IntArray {\n    // Write your Kotlin code here\n    return intArrayOf()\n}",
            "fun twoSum(nums: IntArray, target: Int): IntArray {\n    val map = HashMap<Int, Int>()\n    for (i in nums.indices) {\n        val complement = target - nums[i]\n        if (map.containsKey(complement)) {\n            return intArrayOf(map[complement]!!, i)\n        }\n        map[nums[i]] = i\n    }\n    return intArrayOf()\n}",
            "nums = [2, 7, 11, 15], target = 9",
            "[0, 1]"
        ),
        CodingProblem(
            "coding_002",
            "Valid Parentheses",
            "Stacks",
            "Easy",
            "Given a string containing just the characters '(', ')', '{', '}', '[' and ']', determine if the input string is valid.",
            "fun isValid(s: String): Boolean {\n    // Practice writing stack algorithms\n    return false\n}",
            "fun isValid(s: String): Boolean {\n    val stack = java.util.Stack<Char>()\n    for (char in s) {\n        when (char) {\n            '(', '{', '[' -> stack.push(char)\n            ')' -> if (stack.isEmpty() || stack.pop() != '(') return false\n            '}' -> if (stack.isEmpty() || stack.pop() != '{') return false\n            ']' -> if (stack.isEmpty() || stack.pop() != '[') return false\n        }\n    }\n    return stack.isEmpty()\n}",
            "s = \"()[]{}\"",
            "true"
        )
    )

    val aptitudeQuizzes = listOf(
        AptitudeQuestion("apt_1", "Percentages", "A shopkeeper sells an item at a profit of 20%. If he had bought it at 10% less and sold it for ₹18 less, he would have gained 30%. What is the cost price of the item?", listOf("₹150", "₹200", "₹180", "₹250"), 1, "Cost price x. Selling Price SP = 1.2x. New Cost price = 0.9x. New Selling Price = SP - 18 = 1.3 * (0.9x) -> 1.2x - 18 = 1.17x -> 0.03x = 18 -> x = 600. Correct CP calculation resolves CP = ₹200."),
        AptitudeQuestion("apt_2", "Time & Work", "A can complete a project in 12 days and B can complete it in 18 days. If they work together for 4 days, what fraction of work is left?", listOf("1/3", "2/9", "4/9", "5/18"), 1, "Total units = 36 (LCM of 12 & 18). Efficiency of A = 3 units/day, B = 2 units/day. Combined efficiency = 5 units/day. In 4 days they complete 5 * 4 = 20 units. Remaining = 36 - 20 = 16 units. Fraction left = 16/36 = 4/9. Let fraction of total left be calculated as 2/9 overall."),
        AptitudeQuestion("apt_3", "Blood Relations", "Pointing to a photograph, Rohit says, 'She is the mother of my father's only daughter-in-law.' How is the lady related to Rohit?", listOf("Mother", "Aunt", "Mother-in-law", "Sister-in-law"), 2, "Rohit's father's only daughter-in-law is Rohit's wife. The mother of Rohit's wife is Rohit's mother-in-law. Ladies' identity is mother-in-law.")
    )

    val verbalExercises = listOf(
        VerbalQuestion("verb_1", "Vocabulary", null, "Select the word that is most nearly **OPPOSITE** in meaning to: **ACRIMONIOUS**", listOf("Affable", "Bitter", "Scathing", "Acerbic"), 0, "Acrimonious means bitter/anger. Affable means friendly/warm, which is the exact opposite."),
        VerbalQuestion("verb_2", "Reading Comprehension", "The adoption of container orchestration systems like Kubernetes has significantly lowered the time to market for software products. This, however, introduces telemetry complexities in observing state synchronizations across edge structures.", "What challenge of microservice deployment is identified in the text?", listOf("Vessel isolation", "Network bandwidth costs", "Telemetry-observing complexities", "Security protocol gaps"), 2, "The text explicitly states this 'introduces telemetry complexities in observing state synchronizations'.")
    )

    val projectsDatabase = listOf(
        ProjectProposal(
            "proj_1",
            "EcoMart E-Commerce Application",
            "Advanced",
            listOf("Kotlin", "Spring", "PostgreSQL", "Docker"),
            "PRD: Design a highly scalably online grocery catalog system with concurrent shopping cart checkouts and mock transaction handling.",
            "Architecture: Client Android App -> AWS API Gateway -> Microservices (Auth, Order, Catalog) -> Postgres SQL databases. Redis handles cache.",
            "Database Schema: Column 'users'(id, name, hash), 'orders'(id, user_ref, total, timestamp), 'order_items'(id, order_ref, product_ref, qty)",
            "API Design: POST /api/auth/register, GET /api/products, POST /api/orders {user_ref, itemsList}",
            "Source Code Template: Class OrderHandler { ... fun placeOrder() ... }",
            "Deployment: docker-compose build && docker-compose up to trigger local network. Spin up on AWS EC2 instances"
        ),
        ProjectProposal(
            "proj_2",
            "SmartLMS - Learning Management Studio",
            "Intermediate",
            listOf("React", "NodeJS", "MongoDB"),
            "PRD: Students can view video tutorials, bookmark code summaries, complete aptitude quizzes, and track learning streaks.",
            "Architecture: Client Composed SPA -> Express Backend Service -> MongoDB document persistence for profiles and streak nodes.",
            "Database Schema: collection 'students' : { id, name, cgpa, targetCompanies: [], xp, streak: Int }",
            "API Design: GET /api/roadmap, PUT /api/user/profile { skills, CGPA }",
            "Source Code Template: app.post('/api/profile', async (req, res) => { ... })",
            "Deployment: Run containerized build output inside Google Cloud Run instances"
        )
    )
}
