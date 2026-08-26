package com.rork.mindsetframestracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rork.mindsetframestracker.billing.SubscriptionTier
import com.rork.mindsetframestracker.integrations.GeminiInsightClient

@Composable
fun AIInsightSheet(
    tier: SubscriptionTier,
    habitName: String,
    dataType: String,
    value: String,
    unit: String? = null,
    onDismiss: () -> Unit,
    onUpgradeNeeded: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var insight by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(habitName, value) {
        val result = GeminiInsightClient.getInsight(tier, habitName, dataType, value, unit)
        isLoading = false
        result.fold(
            onSuccess = { insight = it },
            onFailure = { error ->
                if (error.message?.contains("premium") == true) {
                    onUpgradeNeeded()
                    onDismiss()
                } else {
                    insight = "Couldn't load insight right now — try again shortly."
                }
            },
        )
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Insight", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.padding(top = 12.dp)) {}
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text(insight ?: "")
            }
        }
    }
}
