package com.rork.mindsetframestracker.integrations

import android.app.Activity
import android.content.Context
import android.util.Log
import com.huawei.hms.hihealth.HuaweiHiHealth
import com.huawei.hms.hihealth.data.DataType
import com.huawei.hms.hihealth.options.DataTypeAddOptions
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService

/**
 * Huawei Health Kit — free for every user, no subscription tier check
 * (see Entitlements.HUAWEI_HEALTH_KIT). Requires a scope-authorized
 * Huawei ID sign-in before any read call succeeds.
 */
object HuaweiHealthKitClient {

    private const val TAG = "HuaweiHealthKitClient"

    // Which activity habit-icon-ids this source natively supports.
    // Used by ActivitySourcePicker to decide whether to offer this option.
    val supportedActivityIconIds = setOf("walking", "running", "walk2", "basketball", "gym")

    fun signInIntent(activity: Activity): HuaweiIdAuthService {
        val authParams = HuaweiIdAuthParamsHelper(com.huawei.hms.support.hwid.request.HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setAccessToken()
            .createParams()
        return HuaweiIdAuthManager.getService(activity, authParams)
    }

    fun readTodaySteps(context: Context, onResult: (Long?) -> Unit) {
        try {
            HuaweiHiHealth.getDataController(context)
                .let { controller ->
                    // Real implementation reads DataType.DT_CONTINUOUS_STEPS_DELTA
                    // via a DataReadRequest scoped to today — full request builder
                    // omitted here for brevity, follows standard Health Kit SDK pattern.
                    Log.i(TAG, "readTodaySteps invoked — wire DataReadRequest per Huawei Health Kit docs")
                    onResult(null) // placeholder until DataReadRequest is filled in
                }
        } catch (e: Exception) {
            Log.e(TAG, "readTodaySteps failed", e)
            onResult(null)
        }
    }

    fun isActivitySupported(iconId: String): Boolean = iconId in supportedActivityIconIds
}.kt
