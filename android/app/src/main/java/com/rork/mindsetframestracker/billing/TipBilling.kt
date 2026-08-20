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
                Log.i(TAG, "createPurchaseIntent success for $productId")
                val status = result.status
                if (status.hasResolution()) {
                    status.resolution?.let { onReady(it.intentSender) }
                        ?: run {
                            Log.w(TAG, "hasResolution() true but resolution was null")
                            onError("Unable to launch payment sheet.")
                        }
                } else {
                    Log.w(TAG, "No resolution available for $productId")
                    onError("Unable to launch payment sheet.")
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
