package com.example.ui

import android.util.Log
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

object AiChatManager {
    private const val TAG = "AiChatManager"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    suspend fun askAi(
        apiKey: String,
        service: String,
        question: String,
        students: List<StudentEntity>,
        feeHistory: List<FeeHistoryEntity>,
        staff: List<StaffEntity>,
        batches: List<BatchEntity>
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext "Error: Please configure and confirm your $service API key in Settings first."
        }

        val systemPrompt = """
            You are TuitionOS AI, a smart assistant helpful to tuition center administrators.
            You have access to the tuition database state listed below.
            Answer the user's question directly, clearly, and concisely. 
            If any students owe money, name them and specify how much they owe (standard, monthly fee, status).
            If asked about outstanding payments, analyze both student statuses ("Pending", "Overdue") and recent fee histories.
            Maintain a friendly, crisp, administrative assistant persona. Present figures or student names in lists if appropriate.
        """.trimIndent()

        // Construct Database Context
        val contextBuilder = StringBuilder()
        contextBuilder.append("=== TUITION CENTER DATABASE CONTEXT ===\n\n")
        
        contextBuilder.append("--- Batches ---\n")
        if (batches.isEmpty()) {
            contextBuilder.append("No batches registered.\n")
        } else {
            batches.forEach { batch ->
                contextBuilder.append("- Batch: ${batch.name}, Subject: ${batch.subject}, Days: ${batch.daysOfWeek}, Time: ${batch.startTime}-${batch.endTime}, Students: ${batch.studentCount}, Status: ${batch.status}\n")
            }
        }
        
        contextBuilder.append("\n--- Students ---\n")
        if (students.isEmpty()) {
            contextBuilder.append("No students registered.\n")
        } else {
            students.forEach { student ->
                contextBuilder.append("- ID: ${student.studentId}, Name: ${student.fullName}, Std: ${student.standard}, Status: ${student.status}, Monthly Fee: Rs. ${student.monthlyFee}, Attendance: ${student.attendancePercentage}%\n")
            }
        }
        
        contextBuilder.append("\n--- Fee Transactions ---\n")
        if (feeHistory.isEmpty()) {
            contextBuilder.append("No manual payment histories found.\n")
        } else {
            feeHistory.take(20).forEach { fee ->
                contextBuilder.append("- Student: ${fee.studentName}, Installment: ${fee.installment}, Amount: Rs. ${fee.amount}, Status: ${fee.status}, Month: ${fee.month}\n")
            }
        }
        
        contextBuilder.append("\n--- Staff members ---\n")
        if (staff.isEmpty()) {
            contextBuilder.append("No staff active.\n")
        } else {
            staff.forEach { member ->
                contextBuilder.append("- Name: ${member.name}, Role: ${member.role}, Contact: ${member.phone}, Batches: ${member.assignedBatches}\n")
            }
        }

        val fullPrompt = """
            $systemPrompt
            
            $contextBuilder
            
            User Question: $question
        """.trimIndent()

        try {
            if (service.equals("Gemini", ignoreCase = true)) {
                callGemini(apiKey, fullPrompt)
            } else if (service.equals("Groq", ignoreCase = true)) {
                callGroq(apiKey, systemPrompt, "$contextBuilder\n\nUser Question: $question")
            } else {
                "Error: Unknown service model requested ($service)."
            }
        } catch (e: Exception) {
            Log.e(TAG, "API Call Failed", e)
            "Error: ${e.message ?: "An unexpected request problem occurred"}"
        }
    }

    private fun callGemini(apiKey: String, prompt: String): String {
        // v1beta is excellent and supported in Gemini AI Studio for direct key access
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"
        
        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }

        val body = requestJson.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(url)
            .header("x-goog-api-key", apiKey)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                return "Gemini API Error (HTTP ${response.code}): $errBody"
            }
            val resStr = response.body?.string() ?: return "Error: Empty response body from Gemini."
            val root = JSONObject(resStr)
            val candidates = root.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return "Gemini returned no content output choices."
            }
            val candidate = candidates.getJSONObject(0)
            val contentObj = candidate.optJSONObject("content") ?: return "No response content content is available."
            val parts = contentObj.optJSONArray("parts") ?: return "No parts in the candidates response content."
            if (parts.length() == 0) return "No parts returned from Gemini text candidate response."
            return parts.getJSONObject(0).optString("text", "Empty response text.")
        }
    }

    private fun callGroq(apiKey: String, systemPrompt: String, userPrompt: String): String {
        val url = "https://api.groq.com/openai/v1/chat/completions"
        
        val requestJson = JSONObject().apply {
            put("model", "llama3-8b-8192")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userPrompt)
                })
            })
        }

        val body = requestJson.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                return "Groq API Error (HTTP ${response.code}): $errBody"
            }
            val resStr = response.body?.string() ?: return "Error: Empty response body from Groq."
            val root = JSONObject(resStr)
            val choices = root.optJSONArray("choices")
            if (choices == null || choices.length() == 0) {
                return "Groq returned no output choices."
            }
            val choice = choices.getJSONObject(0)
            val messageObj = choice.optJSONObject("message") ?: return "No response message object in Groq response choice."
            return messageObj.optString("content", "Empty message context.")
        }
    }
}
