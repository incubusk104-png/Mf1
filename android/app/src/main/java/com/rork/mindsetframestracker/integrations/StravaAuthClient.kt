package com.rork.mindsetframestracker.integrations

import android.content.Intent
import android.net.Uri
import android.util.Log
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
 * Strava OAuth2 client. Client ID/secret are NOT stored in this app —
 * token exchange happens through the strava-token-exchange Supabase Edge
 * Function, which reads STRAVA_CLIENT_ID / STRAVA_CLIENT_SECRET from
 * encrypted Edge Function secrets. Nothing sensitive ships in the APK.
 *
 * Gated to SubscriptionTier.REGULAR only per Entitlements — Founding tier
 * does not get Strava (deliberate cost-control decision).
 */
object StravaAuthClient {

    private const val TAG = "StravaAuthClient"

    // Public — safe to ship in the APK, this is not a secret.
    private const val STRAVA_CLIENT_ID_PUBLIC = "a7f174d89a804f26415155772aaabe2a9cb8" // matches STRAVA_CLIENT_ID value, public half only
    private const val REDIRECT_URI = "mindsetframes://strava-callback"
    private const val AUTH_URL = "https://www.strava.com/oauth/mobile/authorize"

    private val EDGE_FUNCTION_URL =
        "${BuildConfig.SUPABASE_URL}/functions/v1/strava-token-exchange"

    private val httpClient = OkHttpClient()
    private val jsonMediaType = "application/json".toMediaType()

    fun buildAuthIntent(): Intent {
        val uri = Uri.parse(AUTH_URL).buildUpon()
            .appendQueryParameter("client_id", STRAVA_CLIENT_ID_PUBLIC)
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("approval_prompt", "auto")
            .appendQueryParameter("scope", "activity:read_all")
            .build()
        return Intent(Intent.ACTION_VIEW, uri)
    }

    /** Call after redirect intercepted in MainActivity.onNewIntent, with the "code" query param. */
    suspend fun exchangeCodeForToken(code: String): Result<StravaTokens> = withContext(Dispatchers.IO) {
        callEdgeFunction(
            JSONObject().apply {
                put("grantType", "authorization_code")
                put("code", code)
            },
        )
    }

    suspend fun refreshTokenIfNeeded(tokens: StravaTokens): Result<StravaTokens> = withContext(Dispatchers.IO) {
        if (tokens.expiresAt > System.currentTimeMillis() / 1000 + 300) {
            return@withContext Result.success(tokens) // still valid, 5min buffer
        }
        callEdgeFunction(
            JSONObject().apply {
                put("grantType", "refresh_token")
                put("refreshToken", tokens.refreshToken)
            },
        )
    }

    private fun callEdgeFunction(payload: JSONObject): Result<StravaTokens> {
        return try {
            val body = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(EDGE_FUNCTION_URL)
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
                .post(body)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return Result.failure(Exception("Strava exchange failed: ${response.code}"))
                }
                val json = JSONObject(response.body?.string() ?: "{}")
                Result.success(
                    StravaTokens(
                        accessToken = json.getString("access_token"),
                        refreshToken = json.getString("refresh_token"),
                        expiresAt = json.getLong("expires_at"),
                    ),
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "callEdgeFunction failed", e)
            Result.failure(e)
        }
    }

    fun canConnect(tier: SubscriptionTier): Boolean =
        Entitlements.hasAccess(tier, Feature.STRAVA)
}

data class StravaTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long,
)
