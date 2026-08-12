package com.rork.mindsetframestracker.auth

import android.content.Context
import android.util.Log
import com.huawei.agconnect.AGConnectInstance
import com.huawei.agconnect.AGConnectOptionsBuilder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Loads and validates the Huawei AGConnect configuration file
 * (agconnect-services.json) bundled in the APK assets, then initializes the
 * AGConnect SDK with it so HUAWEI Account Kit can authenticate.
 *
 * Setup: download agconnect-services.json from AppGallery Connect
 * (Project settings > General information > App information) and place it at
 * android/app/agconnect-services.json — the standard Huawei location. The
 * Gradle build copies it into assets automatically (see app/build.gradle.kts).
 *
 * When the file is missing or incomplete, Huawei sign-in reports a friendly
 * "not configured" message and the app keeps working with email sign-in only —
 * initialization never crashes the app.
 */
object HuaweiServicesConfig {

    private const val TAG = "HuaweiServicesConfig"
    private const val CONFIG_ASSET = "agconnect-services.json"

    @Volatile
    private var initialized = false

    @Volatile
    private var configuredAppId: String? = null

    /** True when a valid agconnect-services.json was found and AGConnect initialized. */
    val isConfigured: Boolean
        get() = initialized && !configuredAppId.isNullOrBlank()

    /**
     * Reads agconnect-services.json from assets, validates the client block,
     * and initializes AGConnect. Idempotent and failure-safe: errors are
     * logged and leave the app fully functional without Huawei sign-in.
     */
    fun initialize(context: Context) {
        if (initialized) return
        val appContext = context.applicationContext
        val raw = runCatching {
            appContext.assets.open(CONFIG_ASSET).bufferedReader().use { it.readText() }
        }.getOrNull()
        if (raw.isNullOrBlank()) {
            Log.i(TAG, "No $CONFIG_ASSET bundled — Huawei sign-in stays disabled until it's added")
            return
        }

        val client = runCatching {
            Json { ignoreUnknownKeys = true }
                .parseToJsonElement(raw).jsonObject["client"]?.jsonObject
        }.getOrNull()
        val appId = client?.get("app_id")?.jsonPrimitive?.content?.trim().orEmpty()
        val clientId = client?.get("client_id")?.jsonPrimitive?.content?.trim().orEmpty()
        val packageName = client?.get("package_name")?.jsonPrimitive?.content?.trim().orEmpty()

        if (appId.isBlank() || clientId.isBlank()) {
            Log.w(TAG, "$CONFIG_ASSET is missing client/app_id or client/client_id — Huawei sign-in disabled")
            return
        }
        if (packageName.isNotBlank() && packageName != appContext.packageName) {
            Log.w(
                TAG,
                "$CONFIG_ASSET package_name ($packageName) doesn't match ${appContext.packageName} — " +
                    "Huawei sign-in will fail until the AppGallery Connect app config matches",
            )
        }

        runCatching {
            AGConnectInstance.initialize(
                appContext,
                AGConnectOptionsBuilder().setInputStream(appContext.assets.open(CONFIG_ASSET)),
            )
        }.onSuccess {
            initialized = true
            configuredAppId = appId
            Log.i(TAG, "AGConnect initialized (app_id=$appId)")
        }.onFailure {
            Log.w(TAG, "AGConnect initialization failed: ${it.message}")
        }
    }
}
