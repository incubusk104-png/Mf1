package com.rork.mindsetframestracker.ui.screens

// Added these imports for Huawei IAP
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest
import com.rork.mindsetframestracker.billing.TipBilling
import com.rork.mindsetframestracker.billing.TipPurchaseResult

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.rork.mindsetframestracker.data.AppData
import com.rork.mindsetframestracker.data.ContentPack
import com.rork.mindsetframestracker.data.Habit
import com.rork.mindsetframestracker.data.Dates
import com.rork.mindsetframestracker.data.BadgeTier
import com.rork.mindsetframestracker.data.completedCountOn
import com.rork.mindsetframestracker.data.currentMood
import com.rork.mindsetframestracker.data.dailyCheckInStreak
import com.rork.mindsetframestracker.data.fullCompletionStreak
import com.rork.mindsetframestracker.data.isCheckedToday
import com.rork.mindsetframestracker.data.newlyEarnedBadge
import com.rork.mindsetframestracker.data.hasFeatureAccess
import com.rork.mindsetframestracker.data.sortedHabits
import com.rork.mindsetframestracker.data.streakFor
import com.rork.mindsetframestracker.data.MoodMode
import com.rork.mindsetframestracker.data.ThemeMode
import com.rork.mindsetframestracker.ui.AppViewModel
import com.rork.mindsetframestracker.ui.appStrings
import com.rork.mindsetframestracker.ui.components.CompletionHeatmap
import com.rork.mindsetframestracker.ui.components.CompanionNotificationCard
import com.rork.mindsetframestracker.ui.components.ConfettiBurst
import com.rork.mindsetframestracker.ui.avatar.CompanionStudioSheet
import com.rork.mindsetframestracker.ui.components.DailyGoalShareDialog
import com.rork.mindsetframestracker.ui.components.EntranceItem
import com.rork.mindsetframestracker.ui.components.BadgeSection
import com.rork.mindsetframestracker.ui.components.BadgeStrings
import com.rork.mindsetframestracker.ui.components.BadgeUnlockOverlay
import com.rork.mindsetframestracker.ui.components.badgeTitle
import com.rork.mindsetframestracker.ui.components.badgeDesc
import com.rork.mindsetframestracker.ui.components.MilestoneBanner
import com.rork.mindsetframestracker.ui.components.MilestoneCelebration
import com.rork.mindsetframestracker.ui.components.MoodPicker
import com.rork.mindsetframestracker.ui.components.ThemeToggleButton
import com.rork.mindsetframestracker.ui.components.milestoneReached
import com.rork.mindsetframestracker.ui.components.TipSheet
import com.rork.mindsetframestracker.ui.theme.DisplayFontFamily
import com.rork.mindsetframestracker.ui.theme.LocalMoodTheme
import com.rork.mindsetframestracker.util.StreakShare
import com.rork.mindsetframestracker.util.ProgressShareImage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onGoToHabits: () -> Unit,
) {
    val data by viewModel.state.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val newCompanionUnlocks by viewModel.newCompanionUnlocks.collectAsStateWithLifecycle()
    val activity = androidx.activity.compose.LocalActivity.current
    val moodTheme = LocalMoodTheme.current
    val mood = data.currentMood()
    val copy = ContentPack.copyFor(mood, data.settings.language)
    val doneCount = data.habits.count { data.isCheckedToday(it.id) }
    val streak = data.dailyCheckInStreak()
    val hasAccess = data.settings.hasFeatureAccess()
    var showCompanionStudio by remember { mutableStateOf(false) }
    var showGrounding by remember { mutableStateOf(false) }
    var showTipSheet by remember { mutableStateOf(false) }
    val s = appStrings()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    val tipLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        activity?.let { act ->
            TipBilling.handlePurchaseResult(context = act, data = result.data) { outcome ->
                when (outcome) {
                    is TipPurchaseResult.Success -> {
                        scope.launch { snackbarHostState.showSnackbar("Thank you for the tip! 💜") }
                    }
                    is TipPurchaseResult.Cancelled -> Unit
                    is TipPurchaseResult.Error -> {
                        scope.launch { snackbarHostState.showSnackbar(outcome.message) }
                    }
                }
            }
        }
    }

    // ... (rest of your existing state and LaunchedEffect code remains unchanged) ...
    val systemDark = isSystemInDarkTheme()
    val isDarkTheme = when (data.settings.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemDark
    }

    var previousStreak by remember { mutableIntStateOf(streak) }
    var celebrationTrigger by remember { mutableIntStateOf(0) }
    var celebratedMilestone by remember { mutableIntStateOf(0) }
    LaunchedEffect(streak) {
        val milestone = milestoneReached(previousStreak, streak)
        if (milestone != null && milestone != celebratedMilestone) {
            celebratedMilestone = milestone
            celebrationTrigger++
        }
        previousStreak = streak
    }

    val fullCompletionStreak = data.fullCompletionStreak()
    var previousFullStreak by remember { mutableIntStateOf(fullCompletionStreak) }
    var newlyEarnedTier by remember { mutableStateOf<BadgeTier?>(null) }
    var showBadgeUnlock by remember { mutableStateOf(false) }
    LaunchedEffect(fullCompletionStreak) {
        val tier = newlyEarnedBadge(
            previousStreak = previousFullStreak,
            currentStreak = fullCompletionStreak,
            alreadyEarned = data.settings.earnedBadges,
        )
        if (tier != null) {
            viewModel.awardBadge(tier)
            newlyEarnedTier = tier
            showBadgeUnlock = true
        }
        previousFullStreak = fullCompletionStreak
    }

    val badgeStrings = BadgeStrings(
        sectionTitle = s.badgeSectionTitle,
        sectionSubtitle = s.badgeSectionSubtitle,
        tier3Title = s.badge3Title,
        tier3Desc = s.badge3Desc,
        tier7Title = s.badge7Title,
        tier7Desc = s.badge7Desc,
        tier14Title = s.badge14Title,
        tier14Desc = s.badge14Desc,
        tier30Title = s.badge30Title,
        tier30Desc = s.badge30Desc,
        newBadge = s.badgeNew,
    )
    val unlockTier = newlyEarnedTier
    if (showBadgeUnlock && unlockTier != null) {
        BadgeUnlockOverlay(
            tier = unlockTier,
            title = badgeTitle(unlockTier, badgeStrings),
            description = badgeDesc(unlockTier, badgeStrings),
            unlockedLabel = s.badgeUnlocked,
            shareCta = s.badgeShareCta,
            dismissLabel = s.badgeKeepGoing,
            onShare = {
                activity?.let { act ->
                    StreakShare.shareBadge(
                        act as android.content.Context,
                        badgeTitle(unlockTier, badgeStrings),
                        unlockTier.daysRequired,
                    )
                }
                showBadgeUnlock = false
            },
            onDismiss = { showBadgeUnlock = false },
        )
    }

    val habitsAllDone = data.habits.isNotEmpty() && doneCount == data.habits.size
    var wasAllDone by remember { mutableStateOf(habitsAllDone) }
    var shareCardOfferedDay by rememberSaveable { mutableStateOf("") }
    var showDailyShareDialog by remember { mutableStateOf(false) }
    LaunchedEffect(habitsAllDone) {
        if (habitsAllDone && !wasAllDone && shareCardOfferedDay != Dates.todayKey()) {
            shareCardOfferedDay = Dates.todayKey()
            kotlinx.coroutines.delay(900)
            showDailyShareDialog = true
        }
        wasAllDone = habitsAllDone
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = syncState.busy,
            onRefresh = { viewModel.syncNow() },
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // ... (your existing LazyColumn items remain unchanged) ...
                item(key = "header") { EntranceItem(index = 0) { HomeHeader(taglineKey = mood.name, tagline = copy.tagline, streak = streak, isDarkTheme = isDarkTheme, reducedMotion = data.settings.reducedMotion, onToggleTheme = { viewModel.setThemeMode(if (isDarkTheme) ThemeMode.LIGHT else ThemeMode.DARK) }, onShareStreak = { activity?.let { act -> StreakShare.shareStreak(act as android.content.Context, data) } }) } }
                item(key = "mood") { EntranceItem(index = 1) { Column { Text(text = "How are you arriving today?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 10.dp))
                    MoodPicker(selected = mood, onSelect = { viewModel.selectMood(it) }) } } }
                item(key = "prompt") { EntranceItem(index = 2) { 
                    val promptInk = Color(0xFFFFFCF5)
                    Box(modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.extraLarge).background(Brush.linearGradient(colors = listOf(moodTheme.gradient.first(), moodTheme.gradient.last())))) {
                        Icon(imageVector = Icons.Outlined.SelfImprovement, contentDescription = null, tint = promptInk.copy(alpha = 0.12f), modifier = Modifier.align(Alignment.BottomEnd).padding(end = 2.dp).size(104.dp))
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Outlined.SelfImprovement, contentDescription = null, tint = promptInk.copy(alpha = 0.9f), modifier = Modifier.size(18.dp))
                                Crossfade(targetState = copy.promptHeader.uppercase(Locale.getDefault()), animationSpec = moodTheme.motion.tween(500), label = "promptHeader") { header -> Text(text = header, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp, color = promptInk.copy(alpha = 0.85f), modifier = Modifier.padding(start = 8.dp)) }
                            }
                            Crossfade(targetState = ContentPack.promptFor(mood, hasAccess, data.settings.language), animationSpec = moodTheme.motion.tween(500), label = "prompt") { prompt -> Text(text = prompt, style = MaterialTheme.typography.titleLarge, color = promptInk, modifier = Modifier.padding(top = 12.dp, end = 24.dp)) }
                        }
                    }
                } }
                item(key = "quote") { EntranceItem(index = 3) { Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))) { Row(modifier = Modifier.padding(20.dp)) { Icon(imageVector = Icons.Outlined.FormatQuote, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                    Crossfade(targetState = ContentPack.quoteFor(mood, hasAccess, data.settings.language), animationSpec = moodTheme.motion.tween(500), label = "quote") { quote -> Text(text = quote, style = MaterialTheme.typography.titleMedium.copy(fontFamily = DisplayFontFamily, lineHeight = 26.sp), fontStyle = FontStyle.Italic, fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f), modifier = Modifier.padding(start = 12.dp)) } } } } }
                item(key = "companionMood") { EntranceItem(index = 4) { CompanionNotificationCard(data = data, hasNewUnlocks = newCompanionUnlocks.isNotEmpty(), onOpenStudio = { showCompanionStudio = true }) } }
                item(key = "groundingEntry") { EntranceItem(index = 4) { Card(onClick = { showGrounding = true }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = if (mood == MoodMode.OVERWHELMED) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)) { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) { Icon(imageVector = Icons.Outlined.SelfImprovement, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) { Text(text = s.groundingTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(text = s.groundingEntrySub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) } } } }
                item(key = "habitsHeader") { EntranceItem(index = 4) { Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) { Column(modifier = Modifier.weight(1f)) { Crossfade(targetState = copy.habitsHeader, animationSpec = moodTheme.motion.tween(500), label = "habitsHeader") { header -> Text(text = header, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                    Crossfade(targetState = copy.habitsSub, animationSpec = moodTheme.motion.tween(500), label = "habitsSub") { sub -> Text(text = sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp)) }
                    if (data.habits.isNotEmpty()) { Text(text = "$doneCount of ${data.habits.size} done today", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp)) } }
                    if (data.habits.isNotEmpty()) { TodayProgressRing(done = doneCount, total = data.habits.size, modifier = Modifier.padding(start = 12.dp)) } } } }
                // ... (rest of habit items) ...
                if (data.habits.isEmpty()) { item(key = "empty") { Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(text = copy.emptyHabits, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = onGoToHabits) { Text("Add a habit") } } } } }
                else {
                    val displayHabits = data.sortedHabits().sortedBy { data.isCheckedToday(it.id) }
                    items(count = displayHabits.size, key = { displayHabits[it].id }) { index -> val habit = displayHabits[index]
                        val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = { value -> if (value == SwipeToDismissBoxValue.EndToStart) { val removedCheckIns = viewModel.state.value.checkIns[habit.id].orEmpty(); haptics.performHapticFeedback(HapticFeedbackType.LongPress); viewModel.deleteHabit(habit.id); scope.launch { val result = snackbarHostState.showSnackbar(message = s.habitsDeleted.format(habit.name), actionLabel = s.habitsUndo, duration = SnackbarDuration.Short); if (result == SnackbarResult.ActionPerformed) viewModel.restoreHabit(habit, removedCheckIns) }; true } else false })
                        SwipeToDismissBox(state = dismissState, enableDismissFromStartToEnd = false, modifier = Modifier.fillMaxWidth(), backgroundContent = { val targeted = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart; val bgColor by animateColorAsState(targetValue = if (targeted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant, label = "swipeDeleteBg"); Box(modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.extraLarge).background(bgColor).padding(end = 24.dp), contentAlignment = Alignment.CenterEnd) { Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete ${habit.name}", tint = if (targeted) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurfaceVariant) } }) { HabitRow(habit = habit, data = data, moodKey = mood.name, staggerIndex = index, isSoftLocked = false, onToggle = { viewModel.toggleHabitToday(habit.id) }, onSoftLockClick = { }) }
                    }
                    if (doneCount == data.habits.size) { item(key = "allDone") { AllDoneCard(message = copy.allDone, streak = streak, onShare = { activity?.let { act -> ProgressShareImage.shareDailyCompletion(act as android.content.Context, data) } }) } }
                    if (data.settings.earnedBadges.isNotEmpty()) { item(key = "badges") { BadgeSection(earnedBadges = data.settings.earnedBadges, newlyEarnedTier = newlyEarnedTier, currentFullCompletionStreak = fullCompletionStreak, strings = badgeStrings, modifier = Modifier.padding(top = 4.dp)) } }
                    item(key = "weeklySummary") { PeriodSummaryCard(data = data, dayCount = 7, title = "Last 7 days", periodLabel = "this week", shareDescription = "Share your weekly progress", onShare = { activity?.let { act -> ProgressShareImage.shareWeeklySummary(act as android.content.Context, data) } }, modifier = Modifier.padding(top = 8.dp), entranceIndex = 0) }
                    item(key = "monthlySummary") { PeriodSummaryCard(data = data, dayCount = 30, title = "Last 30 days", periodLabel = "this month", shareDescription = "Share your monthly progress", onShare = { activity?.let { act -> ProgressShareImage.shareMonthlySummary(act as android.content.Context, data) } }, entranceIndex = 1) }
                    item(key = "heatmap") { CompletionHeatmap(data = data, modifier = Modifier.padding(top = 4.dp)) }
                    if (!hasAccess) { item(key = "supportTip") { Card(onClick = { showTipSheet = true }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) { Icon(imageVector = Icons.Outlined.FavoriteBorder, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp)); Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) { Text(text = "Enjoying the app?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold); Text(text = "Leave a small tip to support development", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) } } } } }
            }
        }
    }
    
    // ... (SnackbarHost, MilestoneCelebration, etc.) ...
    SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp))
    MilestoneCelebration(trigger = celebrationTrigger, accentColors = listOf(moodTheme.accent, moodTheme.gradient.first(), moodTheme.gradient.last(), MaterialTheme.colorScheme.primaryContainer), motionEnabled = moodTheme.motion.enabled)
    MilestoneBanner(trigger = celebrationTrigger, milestone = celebratedMilestone, motionEnabled = moodTheme.motion.enabled, modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp))
    
    // Updated IAP TipSheet implementation
    if (showTipSheet) {
        TipSheet(
            onDismiss = { showTipSheet = false },
            onSendTip = { productId ->
                showTipSheet = false
                activity?.let { act ->
                    TipBilling.purchase(
                        activity = act,
                        productId = productId,
                        onReady = { intentSender ->
                            tipLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                        },
                        onError = { message ->
                            scope.launch { snackbarHostState.showSnackbar(message) }
                        },
                    )
                }
            }
        )
    }

    if (showDailyShareDialog) { DailyGoalShareDialog(data = data, onShare = { showDailyShareDialog = false; activity?.let { act -> ProgressShareImage.shareDailyCompletion(act as android.content.Context, data) } }, onDismiss = { showDailyShareDialog = false }) }
    if (showCompanionStudio) { CompanionStudioSheet(viewModel = viewModel, onDismiss = { showCompanionStudio = false }) }
    if (showGrounding) { GroundingSheet(viewModel = viewModel, onDismiss = { showGrounding = false }) }
}
