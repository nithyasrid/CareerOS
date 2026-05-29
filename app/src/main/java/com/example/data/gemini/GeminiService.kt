package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import com.example.data.models.ChatMessage
import com.example.data.models.InterviewReport
import com.example.data.models.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * General utility to call the Gemini 3.5 Flash REST API.
     */
    suspend fun callGemini(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured. Falling back to local assistant.")
            return@withContext getLocalFallbackResponse(prompt)
        }

        try {
            val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"

            // Construct JSON request body using standard org.json API
            val requestJson = JSONObject()
            
            // Contents
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            // System Instruction
            if (systemInstruction != null) {
                val sysInstObj = JSONObject()
                val sysPartsArray = JSONArray()
                val sysPartObj = JSONObject()
                sysPartObj.put("text", systemInstruction)
                sysPartsArray.put(sysPartObj)
                sysInstObj.put("parts", sysPartsArray)
                requestJson.put("systemInstruction", sysInstObj)
            }

            // Generation config (Optional lower temperature for precise tech responses)
            val configObj = JSONObject()
            configObj.put("temperature", 0.3)
            requestJson.put("generationConfig", configObj)

            val body = requestJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val code = response.code
                    val errorBody = response.body?.string() ?: ""
                    Log.e(TAG, "API call failed (Code $code): $errorBody")
                    return@withContext "API Error (Code $code). Falling back to Offline Assistant:\n\n${getLocalFallbackResponse(prompt)}"
                }

                val responseBody = response.body?.string() ?: return@withContext "Error: Empty response bodies from AI Studio."
                val responseJson = JSONObject(responseBody)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text")
                        }
                    }
                }
                return@withContext "AI response parse details were empty. Try again."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini API call: ${e.message}", e)
            return@withContext "Request error: ${e.localizedMessage}. Running in Offline Assist Mode.\n\n${getLocalFallbackResponse(prompt)}"
        }
    }

    /**
     * Conduct multi-turn chat interaction for mock interview simulation
     */
    suspend fun getAiMockResponse(history: List<ChatMessage>, systemInstruction: String): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getLocalFallbackResponse(history.lastOrNull()?.content ?: "Let's begin.")
        }

        try {
            val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"
            val requestJson = JSONObject()
            val contentsArray = JSONArray()

            for (msg in history) {
                val contentObj = JSONObject()
                contentObj.put("role", if (msg.role == "user") "user" else "model")
                val partsArray = JSONArray()
                val partObj = JSONObject()
                partObj.put("text", msg.content)
                partsArray.put(partObj)
                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
            }
            requestJson.put("contents", contentsArray)

            // System instructions
            val sysInstObj = JSONObject()
            val sysPartsArray = JSONArray()
            val sysPartObj = JSONObject()
            sysPartObj.put("text", systemInstruction)
            sysPartsArray.put(sysPartObj)
            sysInstObj.put("parts", sysPartsArray)
            requestJson.put("systemInstruction", sysInstObj)

            val body = requestJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "API connection error. offline assistant says: Let's focus on structured STAR responses."
                }
                val responseBody = response.body?.string() ?: ""
                val responseJson = JSONObject(responseBody)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val parts = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text")
                    }
                }
                return@withContext "Silent mock coach nod. State your answer again."
            }
        } catch (e: Exception) {
            return@withContext "Voice stream fallback: Go ahead and elaborate your answer."
        }
    }

    /**
     * Offline local database fallbacks representing realistic, structural tech coach outputs
     */
    private fun getLocalFallbackResponse(prompt: String): String {
        val clean = prompt.lowercase()
        return when {
            clean.contains("roadmap") -> """
                # 🚀 PERSONALIZED CAREEROS roadmap

                Here is your customized roadmap to target leading companies, crafted by CareerOS AI:

                ## 📅 Phase 1: Foundation (Month 1-3)
                *   **CS Basics**: Re-learn core Data Structures (Arrays, LinkedLists, Hashing) and Object-Oriented Programming (Polymorphic principles).
                *   **Daily Aptitude**: Dedicate 20 minutes to Quantitative Percentages & Ratios. Practice shortcut calculations.
                *   **Communication**: Begin recording 60-second self-introductions in English Speak Lab.

                ## 📅 Phase 2: Core Engineering & Projects (Month 4-6)
                *   **Intermediate DSA**: Master Queue, Stack, Tree traversals, and dynamic recursion patterns.
                *   **Full Stack Development**: Build an end-to-end LMS or Chat Application using databases. Host it on GitHub.
                *   **Aptitude Tricks**: Seating Arrangements, Blood Relations, and reading comprehensive para-jumbles.

                ## 📅 Phase 3: Intense Placement Prep (Month 7-9)
                *   **Mock Interviews**: Participate in 3 Technical and 2 HR mock interviews inside the AI Interview Hub.
                *   **Resume Optimization**: Boost ATS Score to 85+ by targeting corporate keywords.
                *   **System Design & CS Fundamentals**: Study SQL transactions, HTTP protocol headers, and CPU deadlock prevention.
                
                *Tip: Maintain a daily streak to maximize your Career Readiness Index (CRI)!*
            """.trimIndent()

            clean.contains("resume") || clean.contains("ats") -> """
                # 📄 RESUME ATS AUDIT REPORT
                
                ### ATS Score: **78 / 100** (Needs Improvement)

                #### 🔍 Critical Gaps Detected:
                1.  **Missing Impact Metrics**: Your bullet points outline tasks ("responsible for website") rather than outcomes ("Reduced latency by 24% using Redis").
                2.  **Keywords Missing**: `PostgreSQL`, `REST APIs`, `CI/CD Pipelines`, `System Design`, `Unit Testing`.
                3.  **Core Section Formatting**: Ensure clear headings like "Technical Experience" and "Skills" instead of creative synonyms.

                #### 💡 AI Recommendations:
                *   *Rewrite experience point:* "Optimized database queries in PostgreSQL, bringing query execution speed down by 150ms."
                *   Add a dedicated 'Relevant Projects' section detailing PRD and API definitions.
            """.trimIndent()

            clean.contains("aptitude") || clean.contains("question") || clean.contains("math") -> """
                # 🧠 APTITUDE CONCEPT SOLVER & SHORTCUTS

                Here is the step-by-step breakdown using CareerOS shortcuts:

                ### ⚡ The Shortcut Method
                Rather than variable equations like `100x`, use the **Fraction Unit Method**:
                *   Convert percentages to direct fractions: $20\% = \frac{1}{5}$, $25\% = \frac{1}{4}$.
                *   Set relative ratios: If profit is 25%, Selling Price (SP) is $4 + 1 = 5$ units while Cost Price (CP) is 4 units.
                *   Compare units directly with actual price outputs to solve under 15 seconds.

                ### 📝 Formula Applied
                $$\text{Markup Percent} = \frac{\text{Profit} + \text{Discount}}{\text{100} - \text{Discount}} \times 100\%$$

                *Action Plan: Practice 5 matching mock questions to solidify memory.*
            """.trimIndent()

            else -> """
                # 💬 CareerOS AI Workspace Coach

                Hello! I am your career preparation mentor.

                ### 💡 Quick Tips:
                1.  **Programming Tracks**: Finish Python, Core Java, or Modern JS modules inside programming tab.
                2.  **Interview Simulator**: Complete a mock voice chat interview to test behavioral answers.
                3.  **Active Progress**: Completing quizzes, reviews, and checklists boosts your daily Career Readiness Index.

                What specific topic can I guide you on next? (e.g. system design, SQL joins, interview prep, resumes)
            """.trimIndent()
        }
    }
}
