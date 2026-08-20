package com.rork.mindsetframestracker.billing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.entity.PurchaseIntentReq
import org.json.JSONObject

sealed class TipPurchaseResult {
    data class Success(val purchaseData: String, val signature: String) : TipPurchaseResult()
    object Cancelled : TipPurchaseResult()
    data class Error(val message: String) : TipPurchaseResult()
}

object TipBilling {

    private const val TAG = "TipBilling"

    fun purchase(
        activity: Activity,
        productId: String,
        onReady: (android.content.IntentSender) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val req = PurchaseIntentReq().apply {
                // 0 = consumable product (tips)
                priceType = 0
                this.productId = productId
                reservedInfor = "mindset_frames_tip"
            }

            val task = Iap.getIapClient(activity).createPurchaseIntent(req)
            task.addOnSuccessListener { result ->
                val status = result.status
                Log.i(
                    TAG,
                    "createPurchaseIntent result for $productId — " +
                        "statusCode=${status.statusCode}, message=${status.statusMessage}, " +
                        "hasResolution=${status.hasResolution()}",
                )
                if (status.hasResolution()) {
                    status.resolution?.let { onReady(it.intentSender) }
                        ?: run {
                            onError("Unable to launch payment sheet.")
                        }
                } else {
                    // No resolution = Huawei rejected the purchase before any
                    // payment UI could show. statusCode pinpoints why (see
                    // OrderStatusCode in HMS docs) — surface it instead of a
                    // generic message so it's visible without adb.
                    onError(
                        "Payment unavailable (code ${status.statusCode}): " +
                            "${status.statusMessage ?: "no details"}",
                    )
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "createPurchaseIntent failed for $productId", e)
                onError(e.message ?: "Unknown billing error")
            }
        } catch (e: Exception) {
            // Synchronous throw (e.g. IAP Kit not ready, HMS Core missing) —
            // without this catch, onError never fires and the caller's
            // "purchase in flight" flag can get stuck true forever.
            Log.e(TAG, "createPurchaseIntent threw synchronously for $productId", e)
            onError(e.message ?: "Could not start the purchase. Try again.")
        }
    }

    fun handlePurchaseResult(
        context: Context,
        data: Intent?,
        onResult: (TipPurchaseResult) -> Unit
    ) {
        val purchaseResultInfo = Iap.getIapClient(context).parsePurchaseResultInfoFromIntent(data)
        when (purchaseResultInfo.returnCode) {
            0 -> { // ORDER_STATE_SUCCESS
                val inAppPurchaseData = purchaseResultInfo.inAppPurchaseData
                val inAppDataSignature = purchaseResultInfo.inAppDataSignature
                onResult(TipPurchaseResult.Success(inAppPurchaseData, inAppDataSignature))
            }
            -1 -> { // ORDER_STATE_CANCEL
                onResult(TipPurchaseResult.Cancelled)
            }
            else -> {
                onResult(TipPurchaseResult.Error("Purchase failed with code: ${purchaseResultInfo.returnCode}"))
            }
        }
    }
}
