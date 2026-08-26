package com.rork.mindsetframestracker.integrations

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.rork.mindsetframestracker.billing.Entitlements
import com.rork.mindsetframestracker.billing.Feature
import com.rork.mindsetframestracker.billing.SubscriptionTier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Strava OAuth2 client. Gated to SubscriptionTier.REGULAR only per
 * Entitlements — Founding tier does not get Strava (deliberate cost-control
 * decision, confirmed with product owner; revisit if that changes).
 *
 * Requires a Strava API app registered at strava.com/settings/api with a
 * redirect URI matching MINDSET_REDIRECT_URI below, declared as an intent
 * filter in AndroidManifest.xml pointing back to this app.
 */
object StravaAuthClient {

    private const val TAG = "StravaAuthClient"
    private const val CLIENT_ID = "YOUR_STRAVA_CLIENT_ID"       // from strava.com/settings/api
    private const val CLIENT_SECRET = "YOUR_STRAVA_CLIENT_SECRET" // store server-side ideally, not hardcoded in APK
    private const val REDIRECT_URI = "mindsetframes://strava-callback"
    private const val AUTH_URL = "https://www.strava.com/oauth/mobile/authorize"
    private const val TOKEN_URL = "https://www.strava.com/oauth/token"

    private val httpClient = OkHttpClient()

    fun buildAuthIntent(): Intent {
        val uri = Uri.parse(AUTH_URL).buildUpon()
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("approval_prompt", "auto")
            .appendQueryParameter("scope", "activity:read_all")
            .build()
        return Intent(Intent.ACTION_VIEW, uri)
    }

    /** Call after redirect intercepted in MainActivity.onNewIntent, with the "code" query param. */
    suspend fun exchangeCodeForToken(code: String): Result<StravaTokens> = withContext(Dispatchers.IO) {
        try {
            val body = FormBody.Builder()
                .add("client_id", CLIENT_ID)
                .add("client_secret", CLIENT_SECRET)
                .add("code", code)
                .add("grant_type", "authorization_code")
                .build()

            val request = Request.Builder().url(TOKEN_URL).post(body).build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Strava token exchange failed: ${response.code}"))
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
            Log.e(TAG, "exchangeCodeForToken failed", e)
            Result.failure(e)
        }
    }

    suspend fun refreshTokenIfNeeded(tokens: StravaTokens): Result<StravaTokens> = withContext(Dispatchers.IO) {
        if (tokens.expiresAt > System.currentTimeMillis() / 1000 + 300) {
            return@withContext Result.success(tokens) // still valid, 5min buffer
        }
        try {
            val body = FormBody.Builder()
                .add("client_id", CLIENT_ID)
                .add("client_secret", CLIENT_SECRET)
                .add("grant_type", "refresh_token")
                .add("refresh_token", tokens.refreshToken)
                .build()

            val request = Request.Builder().url(TOKEN_URL).post(body).build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Strava token refresh failed: ${response.code}"))
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
            Log.e(TAG, "refreshTokenIfNeeded failed", e)
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
