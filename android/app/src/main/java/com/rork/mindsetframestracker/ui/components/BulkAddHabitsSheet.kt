package com.rork.mindsetframestracker.ui.components

import android.net.Uri
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rork.mindsetframestracker.data.HabitSuggestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Lets the user paste/type several habit names at once, one per line, add
 * them all in a single tap — plus three faster paths in: import a .txt
 * file, tap a live on-device suggestion while typing, or speak habits one
 * at a time. Free-tier cap enforcement happens in AppViewModel.addHabits —
 * this sheet only handles input.
 *
 * @param suggestions Pre-computed suggestions (AI or on-device fallback —
 *   see AppViewModel.getSuggestions). No new AI call happens here; this
 *   sheet only filters that existing list against what's being typed, so
 *   live suggestions cost nothing extra.
 * @param voiceInputUnlocked Whether the mic button is usable (Premium gate,
 *   same as the single-habit dialog) — when false, tapping it calls
 *   [onRequirePremium] instead of listening.
 */
@Composable
fun BulkAddHabitsSheet(
    suggestions: List<HabitSuggestion> = emptyList(),
    voiceInputUnlocked: Boolean = true,
    onRequirePremium: (() -> Unit)? = null,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf("") }
    var dismissedSuggestions by remember { mutableStateOf(setOf<String>()) }
    var importError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ── Voice input ──────────────────────────────────────────
    var recognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    var micError by remember { mutableStateOf<String?>(null) }
    val startVoiceInput: () -> Unit = {
        recognizer = com.rork.mindsetframestracker.util.VoiceInputClient.startListening(
            context = context,
            onResult = { spoken ->
                text = if (text.isBlank()) spoken else "$text\n$spoken"
            },
            onError = { msg -> micError = msg },
        )
    }
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) startVoiceInput() else micError = "Microphone permission is needed for voice input." }
    DisposableEffect(Unit) { onDispose { recognizer?.destroy() } }

    // ── File import ──────────────────────────────────────────
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val imported = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BufferedReader(InputStreamReader(stream)).readLines()
                    }
                }.getOrNull()
            }
            if (imported.isNullOrEmpty()) {
                importError = "Couldn't read that file — make sure it's a plain text (.txt) file."
            } else {
                val lines = imported.map { it.trim() }.filter { it.isNotEmpty() }
                text = if (text.isBlank()) lines.joinToString("\n") else "$text\n${lines.joinToString("\n")}"
                importError = null
            }
        }
    }

    // ── Live suggestions filtered against the line being typed ──
    val currentLine = text.substringAfterLast('\n').trim().lowercase()
    val alreadyTyped = text.lines().map { it.trim().lowercase() }.toSet()
    val visibleSuggestions = suggestions
        .filter { it.name.lowercase() !in dismissedSuggestions }
        .filter { it.name.lowercase() !in alreadyTyped }
        .filter { currentLine.isEmpty() || it.name.lowercase().contains(currentLine) }
        .take(5)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add multiple habits") },
        text = {
            Column {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(
                        "One habit per line.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { filePickerLauncher.launch("text/plain") }) {
                        Icon(Icons.Outlined.UploadFile, contentDescription = "Import from text file")
                    }
                    IconButton(onClick = {
                        if (!voiceInputUnlocked) {
                            onRequirePremium?.invoke()
                        } else {
                            micError = null
                            if (com.rork.mindsetframestracker.util.VoiceInputClient.isAvailable(context)) {
                                micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                            } else {
                                micError = "Voice input isn't available on this device."
                            }
                        }
                    }) {
                        Icon(
                            Icons.Filled.Mic,
                            contentDescription = "Add habits by voice",
                            tint = if (voiceInputUnlocked) LocalContentColor.current
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                }

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    placeholder = { Text("Drink water\nRead 10 pages\nStretch") },
                    supportingText = when {
                        micError != null -> { { Text(micError!!, color = MaterialTheme.colorScheme.error) } }
                        importError != null -> { { Text(importError!!, color = MaterialTheme.colorScheme.error) } }
                        else -> null
                    },
                )

                if (visibleSuggestions.isNotEmpty()) {
                    Text(
                        text = "Suggestions",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(visibleSuggestions, key = { it.name }) { suggestion ->
                            InputChip(
                                selected = false,
                                onClick = {
                                    text = if (text.isBlank()) suggestion.name
                                    else if (text.endsWith("\n")) "$text${suggestion.name}"
                                    else "$text\n${suggestion.name}"
                                },
                                label = { Text(suggestion.name) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { dismissedSuggestions = dismissedSuggestions + suggestion.name.lowercase() },
                                        modifier = Modifier.height(20.dp),
                                    ) {
                                        Text("✕", style = MaterialTheme.typography.labelSmall)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
                    if (lines.isNotEmpty()) onConfirm(lines)
                },
            ) {
                Text("Add all")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
