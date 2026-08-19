package com.rork.mindsetframestracker.billing

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.entity.PurchaseIntentReq
import org.json.JSONObject

sealed class TipPurchaseResult {
    data class Success(val purchaseData: String, val signature: String) : TipPurchaseResult()
    object Cancelled : TipPurchaseResult()
    data class Error(val message: String) : TipPurchaseResult()
}

object TipBilling {

    fun purchase(
        activity: Activity,
        productId: String,
        onReady: (android.content.IntentSender) -> Unit,
        onError: (String) -> Unit
    ) {
        val req = PurchaseIntentReq().apply {
            // 0 = consumable product (tips)
            priceType = 0
            this.productId = productId
            reservedInfor = "mindset_frames_tip"
        }

        val task = Iap.getIapClient(activity).createPurchaseIntent(req)
        task.addOnSuccessListener { result ->
            val status = result.status
            if (status.hasResolution()) {
                status.resolution?.let { onReady(it.intentSender) }
            } else {
                onError("Unable to launch payment sheet.")
            }
        }.addOnFailureListener { e ->
            onError(e.message ?: "Unknown billing error")
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
