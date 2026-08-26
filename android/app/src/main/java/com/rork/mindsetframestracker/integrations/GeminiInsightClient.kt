package com.rork.mindsetframestracker.integrations

import com.rork.mindsetframestracker.BuildConfig
import com.rork.mindsetframestracker.billing.Entitlements
import com.rork.mindsetframestracker.billing.Feature
import com.rork.mindsetframestracker.billing.SubscriptionTier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * AI insight layer — premium-gated (Feature.AI_INSIGHTS). Calls the
 * ai-insight Supabase Edge Function, which holds GEMINI_API_KEY server-side.
 * Reusable across habit detail screens, weekly reports, and any data-bearing
 * habit (heart rate, sleep, steps) — not tied to one icon.
 */
object GeminiInsightClient {

    private val EDGE_FUNCTION_URL = "${BuildConfig.SUPABASE_URL}/functions/v1/ai-insight"
    private val httpClient = OkHttpClient()
    private val jsonMediaType = "application/json".toMediaType()

    suspend fun getInsight(
        tier: SubscriptionTier,
        habitName: String,
        dataType: String,
        value: String,
        unit: String? = null,
        context: String? = null,
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!Entitlements.hasAccess(tier, Feature.AI_INSIGHTS)) {
            return@withContext Result.failure(Exception("AI insights require the premium plan."))
        }

        try {
            val payload = JSONObject().apply {
                put("habitName", habitName)
                put("dataType", dataType)
                put("value", value)
                unit?.let { put("unit", it) }
                context?.let { put("context", it) }
            }
            val request = Request.Builder()
                .url(EDGE_FUNCTION_URL)
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("AI insight failed: ${response.code}"))
                }
                val json = JSONObject(response.body?.string() ?: "{}")
                Result.success(json.optString("insight", "No insight available."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
