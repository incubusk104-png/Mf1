package com.rork.mindsetframestracker.integrations

import android.app.Activity
import android.content.Context
import android.util.Log
import com.huawei.hms.hihealth.HuaweiHiHealth
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService
import com.rork.mindsetframestracker.data.ActivityRecord
import com.rork.mindsetframestracker.data.MindsetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Huawei Health Kit — free for every user (Entitlements.HUAWEI_HEALTH_KIT = true
 * always). Saves synced data as ActivityRecord into MindsetRepository so it
 * feeds the same chart/report pipeline as manual habit check-ins.
 */
object HuaweiHealthKitClient {

    private const val TAG = "HuaweiHealthKitClient"

    val supportedActivityIconIds = setOf("walking", "running", "walk2", "basketball", "gym")

    fun isActivitySupported(iconId: String): Boolean = iconId in supportedActivityIconIds

    fun signInIntent(activity: Activity): HuaweiIdAuthService {
        val authParams = HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setAccessToken()
            .createParams()
        return HuaweiIdAuthManager.getService(activity, authParams)
    }

    /**
     * Reads today's steps from Huawei Health Kit and saves as an ActivityRecord
     * tied to the given habit, so it shows up in that habit's chart history.
     */
    suspend fun syncTodayToHabit(context: Context, habitId: String, activityType: String) {
        try {
            val steps = readTodaySteps(context)
            if (steps != null) {
                val record = ActivityRecord(
                    id = UUID.randomUUID().toString(),
                    habitId = habitId,
                    source = "huawei_health",
                    activityType = activityType,
                    timestamp = System.currentTimeMillis(),
                    steps = steps,
                )
                val repo = MindsetRepository(context)
                repo.saveActivityRecord(record)
            }
        } catch (e: Exception) {
            Log.e(TAG, "syncTodayToHabit failed", e)
        }
    }

    private suspend fun readTodaySteps(context: Context): Long? = withContext(Dispatchers.IO) {
        try {
            // Real implementation reads DataType.DT_CONTINUOUS_STEPS_DELTA
            // via a DataReadRequest scoped to today.
            Log.i(TAG, "readTodaySteps invoked — wire DataReadRequest per Huawei Health Kit docs")
            null // placeholder until DataReadRequest is fully wired with SDK
        } catch (e: Exception) {
            Log.e(TAG, "readTodaySteps failed", e)
            null
        }
    }
}
