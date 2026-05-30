package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object GeminiApi {
    private const val TAG = "GeminiApi"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun solveEquation(equation: String): Pair<String, String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Pair(
                "API Key Missing",
                "Please configure your GEMINI_API_KEY inside the AI Studio Secrets panel to use the advanced AI Equation Solver."
            )
        }

        // Try primary model (gemini-3.5-flash) and fallback if necessary to gemini-3.1-pro-preview
        val models = listOf("gemini-3.5-flash", "gemini-3.1-pro-preview")
        var lastError = ""

        for (model in models) {
            try {
                Log.d(TAG, "Attempting to solve equation using model: $model")
                val result = callGeminiRestApi(apiKey, model, equation)
                if (result != null) {
                    return@withContext result
                }
            } catch (e: Exception) {
                lastError = e.localizedMessage ?: "Unknown network error"
                Log.e(TAG, "Error trying model $model: $lastError", e)
            }
        }

        return@withContext Pair(
            "Connection Error",
            "Unable to solve equation. Details: $lastError. Please double check that your GEMINI_API_KEY in the Secrets panel is correct and active."
        )
    }

    private fun callGeminiRestApi(apiKey: String, model: String, equation: String): Pair<String, String>? {
        val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"

        // Build the body JSON manually using org.json
        val contentsArray = JSONArray().apply {
            put(JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", """
                            Solve the mathematical equation, expression, or system of equations: "$equation".
                            
                            You must respond with a JSON object containing EXACTLY these two keys:
                            1. "roots": A short summary string listing the solutions, roots, or decimal answers (e.g. "x = -4", "x = 2 or x = 3", "x = 0.523, 2.617", "x = 3, y = -1", "No real solutions"). Keep it highly concise.
                            2. "explanation": A detailed, beautiful step-by-step mathematical reasoning and explanation of how you solved the equation. Format this explanation with clean markdown (use bullets, numbered lists, math symbols, and nice spacing).
                            
                            Ensure your entire output is valid JSON and nothing else. Do not wrap in markdown code blocks in your raw network response. Just valid JSON.
                        """.trimIndent())
                    })
                })
            })
        }

        val requestBodyJson = JSONObject().apply {
            put("contents", contentsArray)
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.2) // Low temperature for high precision math calculation
            })
        }

        val requestBody = requestBodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("$baseUrl?key=$apiKey")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: ""
            Log.e(TAG, "Unsuccessful response from Gemini for model $model: Code ${response.code}, Error: $errorBody")
            // Throw exception to trigger fallback
            throw Exception("API returned status code ${response.code}: $errorBody")
        }

        val responseBody = response.body?.string() ?: ""
        val jsonResponse = JSONObject(responseBody)
        
        val candidates = jsonResponse.optJSONArray("candidates")
        if (candidates == null || candidates.length() == 0) {
            val errMsg = if (jsonResponse.has("error")) {
                jsonResponse.getJSONObject("error").optString("message", "API returned an error.")
            } else {
                "No outputs returned by Gemini model."
            }
            return Pair("API Error", errMsg)
        }

        val candidate = candidates.getJSONObject(0)
        val content = candidate.optJSONObject("content") ?: return Pair("API Error", "No content candidates found.")
        val parts = content.optJSONArray("parts") ?: return Pair("API Error", "No parts returned.")
        if (parts.length() == 0) {
            return Pair("API Error", "Empty response parts.")
        }

        val textOfResponse = parts.getJSONObject(0).optString("text", "")
        if (textOfResponse.isBlank()) {
            return Pair("Empty response", "Model returned an empty text response.")
        }

        // Clean any possible markdown surrounding JSON wrapper
        var cleanText = textOfResponse.trim()
        if (cleanText.startsWith("```json")) {
            cleanText = cleanText.substringBeforeLast("```").substringAfter("```json").trim()
        } else if (cleanText.startsWith("```")) {
            cleanText = cleanText.substringBeforeLast("```").substringAfter("```").trim()
        }

        var roots = ""
        var explanation = ""
        try {
            val parsedTextJson = JSONObject(cleanText)
            roots = parsedTextJson.optString("roots", "No solution summary")
            explanation = parsedTextJson.optString("explanation", "No detailed explanation provided.")
        } catch (e: Exception) {
            // Fallback: If not valid JSON, use raw text of response as explanation!
            roots = "Answer Calculated"
            explanation = textOfResponse
        }

        return Pair(roots, explanation)
    }
}
