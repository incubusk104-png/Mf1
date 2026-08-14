package com.rork.mindsetframestracker.ui.screens

import android.content.Intent
import android.os.Build
import android.text.format.DateUtils
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Animation
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Restore
import com.rork.mindsetframestracker.BuildConfig
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.ripple
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rork.mindsetframestracker.data.dailyCheckInStreak
import com.rork.mindsetframestracker.util.MonthlyReportPdf
import com.rork.mindsetframestracker.util.StreakShare
import com.rork.mindsetframestracker.data.ThemeMode
import com.rork.mindsetframestracker.data.AppLanguage
import com.rork.mindsetframestracker.data.languagePickerOrder
import com.rork.mindsetframestracker.data.isLanguageUnlocked
import com.rork.mindsetframestracker.data.hasFeatureAccess
import com.rork.mindsetframestracker.ui.AppViewModel
import com.rork.mindsetframestracker.ui.SyncUiState
import com.rork.mindsetframestracker.ui.appStrings
import com.rork.mindsetframestracker.ui.stringsFor
import com.rork.mindsetframestracker.ui.components.AuthMessageBanner
import com.rork.mindsetframestracker.ui.components.BrandLogos
import com.rork.mindsetframestracker.ui.components.PremiumSheet
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/** Single support/contact address used in the policy, About, and store listing. */
private const val CONTACT_EMAIL = "mindsetframes2026@gmail.com"

/**
 * An outbound brand link shown in the low-key follow row (About card only).
 * Rendered as the official logo only — no text label; [label] feeds the
 * screen-reader description.
 */
private data class SocialLink(
    val label: String,
    val url: String,
    val icon: ImageVector,
    val brandColor: Color,
)

private val socialLinks: List<SocialLink> = listOf(
    SocialLink(
        label = "Facebook",
        url = "https://www.facebook.com/share/1M9CXbv8pH/",
        icon = BrandLogos.facebook,
        brandColor = BrandLogos.facebookBlue,
    ),
    SocialLink(
        label = "Reddit",
        url = "https://www.reddit.com/u/mindsetframes2026/s/ruKgiIND13",
        icon = BrandLogos.reddit,
        brandColor = BrandLogos.redditOrange,
    ),
    SocialLink(
        label = "Instagram",
        url = "https://www.instagram.com/mindsetframes2026?igsh=ZmJxZHpvM3dpMWM3",
        icon = BrandLogos.instagram,
        brandColor = BrandLogos.instagramPink,
    ),
    SocialLink(
        label = "TikTok",
        url = "https://www.tiktok.com/@mindset.frames7?_r=1&_t=ZS-98XnEEfu1fG",
        icon = BrandLogos.tiktok,
        brandColor = BrandLogos.tiktokRed,
    ),
)

/** In-app privacy policy sections shown in the Privacy Policy dialog. */
private val privacyPolicySections: List<Pair<String, String>> = listOf(
    "Data we collect" to "Mindset Frames is built privacy-first. The app itself collects no personal information and works fully without an account. Your habits, check-ins, moods, and settings are stored locally on your device.\n\nIf you choose to sign in for cloud backup, your habit data is stored in a private database tied to your account so you can restore it on another device.",
    "Third-party services" to "• Huawei Account Kit (optional) can be used to create your backup account. We request only basic profile authorization — your Huawei ID and email address. We never receive your contacts, files, or any other personal data. See Huawei's privacy policy at consumer.huawei.com/privacy/privacy-policy.\n• Supabase provides the cloud backup database and authentication. Your data is protected by Row Level Security — only your authenticated account can access it. See supabase.com/privacy.",
    "Distribution store" to "Mindset Frames is distributed through Huawei AppGallery. This privacy policy applies wherever you use the app.",
    "Notifications" to "Daily reminders are scheduled locally on your device using Android AlarmManager. No push servers are used.",
    "Data sharing and selling" to "We do not sell, rent, or share your personal data with anyone.",
    "Data deletion" to "Uninstalling the app deletes all locally stored data. If you use cloud backup, you can permanently erase your account and all backed-up data any time in Settings → Account & sync → Delete account (type DELETE to confirm). Deletion is immediate and irreversible.",
    "Children" to "The app is not directed at children under 13 and collects no personal data from any user.",
    "Changes" to "We may update this policy; material changes will be reflected by the date above.",
    "Contact" to "Questions, privacy requests, or help deleting your account: $CONTACT_EMAIL. If you can no longer access the app, email us from your account address and we'll erase your account and all backed-up data.",
)

private const val PRIVACY_POLICY_UPDATED = "Last updated: August 4, 2026"

/** In-app Terms & Conditions sections shown in the Terms dialog. */
private val termsSections: List<Pair<String, String>> = listOf(
    "Acceptance of terms" to "By downloading or using Mindset Frames you agree to these Terms & Conditions and our Privacy Policy. If you do not agree, please do not use the app.",
    "The service" to "Mindset Frames is a mood-aware habit tracker for personal reflection and self-improvement. The free tier includes up to 5 habits, daily prompts and quotes, weekly progress, and the classic theme. Mindset Frames Premium unlocks extended prompt packs, the exclusive quote library, advanced weekly insights, 12 exclusive accent themes, all 26 languages, unlimited habits, and PDF progress reports. Premium is offered through our Huawei AppGallery listing. There are no ads anywhere in the app.",
    "Not medical advice" to "Mindset Frames supports personal reflection and habit building. It is not a medical device and does not provide medical, psychological, or therapeutic advice, diagnosis, or treatment. If you are struggling with your mental health, please seek help from a qualified professional or a local crisis line.",
    "Acceptable use" to "You agree not to reverse-engineer, resell, or misuse the app, interfere with its operation, or use it in violation of applicable law.",
    "Intellectual property" to "The app, its design, prompts, quotes collections, and branding are owned by Mindset Frames or licensed to us. Famous quotes remain the property of their respective authors and are provided for personal, non-commercial reflection. You retain full ownership of the habit data you create.",
    "Disclaimer & liability" to "The app is provided “as is” without warranties of any kind. To the maximum extent permitted by law, we are not liable for indirect or consequential damages arising from use of the app. Nothing in these terms limits rights you have under consumer-protection law in your country.",
    "Changes & termination" to "We may update features or these terms; material changes will be reflected by the date above and, where required, notified in-app. You can stop using the app at any time; uninstalling deletes local data, and account deletion is available in Settings.",
    "Contact" to "Questions about these terms: $CONTACT_EMAIL.",
)

private const val TERMS_UPDATED = "Last updated: August 4, 2026"

/**
 * Opens an outbound link — Android app links route it into the installed
 * Facebook/Reddit app when available, otherwise the browser. Failures are
 * swallowed silently (e.g. a device with no browser at all).
 */
private fun openExternalLink(context: android.content.Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)))
    }
}

/** Settings: appearance, motion, reminder time, premium status. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: AppViewModel) {
    val data by viewModel.state.collectAsStateWithLifecycle()
    val settings = data.settings
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val s = appStrings()
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }
    var showReportMonthPicker by remember { mutableStateOf(false) }
    var showReportRangePicker by remember { mutableStateOf(false) }
    var showCompanionStudio by remember { mutableStateOf(false) }
    var showGrounding by remember { mutableStateOf(false) }
    var showPremiumSheet by remember { mutableStateOf(false) }
    val hasAccess = settings.hasFeatureAccess()

    var showTimePicker by remember { mutableStateOf(false) }
    var timeJustSaved by remember { mutableStateOf(false) }
    var editingPreset by remember { mutableStateOf<ReminderPreset?>(null) }
    var reminderPreviewResult by remember { mutableStateOf<Boolean?>(null) }
    var showStreakTimePicker by remember { mutableStateOf(false) }
    var streakTimeJustSaved by remember { mutableStateOf(false) }
    var streakPreviewResult by remember { mutableStateOf<Boolean?>(null) }
    var recapPreviewResult by remember { mutableStateOf<Boolean?>(null) }
    val view = LocalView.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
        )

        // Profile header — the companion avatar doubles as the Studio entry.
        ProfileHeader(
            email = syncState.email,
            avatar = settings.avatar,
            onAvatarClick = { showCompanionStudio = true },
            modifier = Modifier.fillMaxWidth(),
        )

        // Premium status / upgrade — Premium is unlocked through the Huawei
        // AppGallery listing; free users see the upgrade CTA.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (hasAccess) Icons.Filled.CheckCircle else Icons.Filled.WorkspacePremium,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp),
                    )
                    Column(modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)) {
                        Text(
                            text = if (hasAccess) s.settingsPremiumActive else "Mindset Frames Premium",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = if (hasAccess) {
                                "All features unlocked — your mind will thank you."
                            } else {
                                "Extended prompts & quotes, advanced insights, exclusive themes, all languages, unlimited habits, and PDF reports."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
                // ── Premium impact — a compact visual of what Premium
                // changes: each pillar lights up fully once unlocked.
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                ) {
                    val impactAlpha = if (hasAccess) 1f else 0.55f
                    listOf(
                        Icons.Outlined.Insights to s.weeklyInsights,
                        Icons.Outlined.AutoAwesome to s.settingsUnlimitedHabits,
                        Icons.Outlined.Translate to s.settingsLanguage,
                        Icons.Outlined.PictureAsPdf to s.settingsPdfReports,
                    ).forEach { (icon, label) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .alpha(impactAlpha),
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 2,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
                if (hasAccess) {
                    Text(
                        text = "All features unlocked — enjoy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                } else {
                    Button(
                        onClick = { showPremiumSheet = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            contentColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                            .defaultMinSize(minHeight = 48.dp),
                    ) { Text(s.settingsUpgradeBadge) }
                }
            }
        }

        // Companion & wellbeing — declares the newest free features so
        // they're discoverable outside their Home entry points.
        SettingsCard(title = s.settingsWellbeingTitle) {
            WellbeingRow(
                icon = Icons.Outlined.Face,
                title = s.studioTitle,
                description = s.settingsWellbeingStudioDesc,
                actionLabel = s.settingsOpen,
                onClick = { showCompanionStudio = true },
            )
            WellbeingRow(
                icon = Icons.Outlined.SelfImprovement,
                title = s.groundingTitle,
                description = s.settingsWellbeingGroundingDesc,
                actionLabel = s.settingsOpen,
                onClick = { showGrounding = true },
                modifier = Modifier.padding(top = 6.dp),
            )
            WellbeingRow(
                icon = Icons.Outlined.GridView,
                title = s.pixelsTitle,
                description = s.settingsWellbeingPixelsDesc,
                actionLabel = null,
                onClick = null,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        // Ads status — the app is completely ad-free.
        SettingsCard(title = s.settingsAds) {
            Text(
                text = s.settingsAdsPremium,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Account & cloud sync — signed-in management only, placed right
        // under the profile so it's easy to find. Creating or connecting an
        // account happens through the save-your-progress sheet (auto-shown
        // once on Today, re-openable below when signed out). The card grows
        // in on sign-in and collapses away on sign-out or account deletion.
        val accountCardVisible = remember {
            MutableTransitionState(syncState.available && syncState.email != null)
        }
        accountCardVisible.targetState = syncState.available && syncState.email != null
        if (accountCardVisible.currentState || accountCardVisible.targetState) {
            AnimatedVisibility(
                visibleState = accountCardVisible,
                enter = if (settings.reducedMotion) EnterTransition.None
                else fadeIn(tween(260)) + expandVertically(tween(320, easing = FastOutSlowInEasing)),
                exit = if (settings.reducedMotion) ExitTransition.None
                else fadeOut(tween(180)) + shrinkVertically(tween(300, easing = FastOutSlowInEasing)),
            ) {
                SettingsCard(title = "Account & sync", animateSize = !settings.reducedMotion) {
                    AccountSection(
                        syncState = syncState,
                        onSignOut = { viewModel.signOut() },
                        onSyncNow = { viewModel.syncNow() },
                        onDeleteAccount = { viewModel.deleteAccount() },
                    )
                }
            }
        }

        // Signed-out backup entry — the one-time popup is easy to dismiss,
        // so Settings keeps a way back into cloud backup. Opens the same
        // save-your-progress sheet (the single surface where auth happens).
        val backupEntryVisible = remember {
            MutableTransitionState(syncState.available && syncState.email == null)
        }
        backupEntryVisible.targetState = syncState.available && syncState.email == null
        if (backupEntryVisible.currentState || backupEntryVisible.targetState) {
            AnimatedVisibility(
                visibleState = backupEntryVisible,
                enter = if (settings.reducedMotion) EnterTransition.None
                else fadeIn(tween(260)) + expandVertically(tween(320, easing = FastOutSlowInEasing)),
                exit = if (settings.reducedMotion) ExitTransition.None
                else fadeOut(tween(180)) + shrinkVertically(tween(300, easing = FastOutSlowInEasing)),
            ) {
                SettingsCard(title = "Back up & restore") {
                    Text(
                        text = "Your habits, check-ins, and moods live only on this " +
                            "phone. Sign in to back them up automatically — or to " +
                            "bring progress back from a previous device.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(
                        onClick = { viewModel.openAuthPrompt() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .defaultMinSize(minHeight = 48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "Back up my progress",
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }

        // Sign-out / account-deletion confirmation — the account card is gone
        // by then, so a standalone banner carries the message, then clears.
        val signedOutMessage = syncState.message
        if (syncState.available && syncState.email == null && signedOutMessage != null) {
            AuthMessageBanner(message = signedOutMessage, isError = syncState.isError)
            LaunchedEffect(signedOutMessage) {
                kotlinx.coroutines.delay(6_000)
                viewModel.clearSyncMessage()
            }
        }

        // Progress report — print-ready PDF export covering a full month or
        // any custom date range, kept with the profile cluster so it reads
        // as "your data, your report".
        SettingsCard(title = "Progress report") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.PictureAsPdf,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Column(modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Export summary as PDF", style = MaterialTheme.typography.bodyLarge)
                        if (!hasAccess) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(start = 8.dp),
                            ) {
                                Text(
                                    text = s.settingsPremium,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                    Text(
                        text = "A polished, high-resolution report — completion stats, daily chart, mood mix, and habit breakdown — for a full month or any date range you choose.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            OutlinedButton(
                onClick = {
                    if (!hasAccess) {
                        showPremiumSheet = true
                    } else {
                        val months = MonthlyReportPdf.exportableMonths(data)
                        if (months.size <= 1) {
                            activity?.let { act ->
                                MonthlyReportPdf.shareMonthlyReport(
                                    act, data, months.firstOrNull() ?: YearMonth.now(),
                                )
                            }
                        } else {
                            showReportMonthPicker = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .defaultMinSize(minHeight = 48.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.IosShare,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Export a month",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            OutlinedButton(
                onClick = {
                    if (!hasAccess) showPremiumSheet = true
                    else showReportRangePicker = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .defaultMinSize(minHeight = 48.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Custom date range",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Text(
                    text = "A copy is also saved to your Downloads folder.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        // Appearance
        SettingsCard(title = "Appearance") {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)) {
                val options = listOf(
                    Triple(ThemeMode.SYSTEM, "System", Icons.Outlined.SettingsBrightness),
                    Triple(ThemeMode.LIGHT, "Light", Icons.Outlined.LightMode),
                    Triple(ThemeMode.DARK, "Dark", Icons.Outlined.DarkMode),
                )
                options.forEachIndexed { index, (mode, label, icon) ->
                    SegmentedButton(
                        selected = settings.themeMode == mode,
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            } else {
                                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                            }
                            viewModel.setThemeMode(mode)
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(SegmentedButtonDefaults.IconSize),
                            )
                        },
                    ) { Text(label) }
                }
            }
            Text(
                text = "Dark is a deep, high-contrast theme — easy on the eyes at night and battery-friendly on OLED screens. You can also flip themes anytime with the sun/moon button on the Today screen.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Animation,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Column(modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)) {
                    Text("Reduce motion", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "Turns off all non-essential animation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = settings.reducedMotion,
                    onCheckedChange = { viewModel.setReducedMotion(it) },
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = "Accent pack",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp),
            ) {
                // "classic" is free; all other packs are Premium exclusives.
                val packs = listOf(
                    "classic" to "Terracotta",
                    "sunrise" to "Sunrise",
                    "forest" to "Forest",
                    "lullaby" to "Lullaby",
                    "sakura" to "Sakura",
                    "ocean" to "Ocean",
                    "lavender" to "Lavender",
                    "honey" to "Honey",
                    "berry" to "Berry",
                    "mint" to "Mint Candy",
                    "peach" to "Peach",
                    "midnight" to "Midnight",
                    "rosewood" to "Rosewood",
                )
                packs.forEach { (id, label) ->
                    val locked = !hasAccess && id != "classic"
                    FilterChip(
                        selected = settings.accentPack == id,
                        onClick = {
                            if (locked) showPremiumSheet = true
                            else viewModel.setAccentPack(id)
                        },
                        label = { Text(label) },
                        leadingIcon = if (locked) {
                            {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = s.settingsLanguageLocked,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        } else null,
                    )
                }
            }
        }

        // Language — dual-free model: English (US & UK) is free everywhere,
        // one locale-matched regional language is free for this install, and
        // the other languages are Premium exclusives.
        SettingsCard(title = s.settingsLanguage) {
            Text(
                text = s.settingsLanguageDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            val languages = languagePickerOrder(settings.freeRegionalLanguage)
            languages.forEach { lang ->
                val isSelected = settings.language == lang
                val isLocked = !settings.isLanguageUnlocked(lang)
                val label = if (lang == AppLanguage.ENGLISH_US) s.settingsEnglishDefault else lang.displayName
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable {
                            if (isLocked) {
                                showPremiumSheet = true
                            } else if (!isSelected) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                } else {
                                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                }
                                viewModel.setLanguage(lang)
                            }
                        }
                        .padding(vertical = 4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Translate,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp),
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                        Text(
                            text = "${lang.flagEmoji}  ${lang.englishName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (isLocked) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = s.settingsLanguageLocked,
                                    modifier = Modifier.size(12.dp),
                                )
                                Text(
                                    text = s.settingsPremium,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(start = 4.dp),
                                )
                            }
                        }
                    } else {
                        if (lang == settings.freeRegionalLanguage && !hasAccess) {
                            // Locale-matched regional unlock — mark it so users
                            // see why this one language is free for them.
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ) {
                                Text(
                                    text = s.settingsLanguageRegionalFree,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(start = 6.dp)
                                    .size(20.dp),
                            )
                        }
                }
            }
        }

        // Reminder
        SettingsCard(title = s.settingsDailyReminder, animateSize = !settings.reducedMotion) {
            Text(
                text = s.settingsQuickPick,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp),
            ) {
                reminderPresets.forEach { preset ->
                    val minutes = settings.presetTimes[preset.id] ?: preset.defaultMinutes
                    val selected = settings.notificationMinutes == minutes
                    PresetChip(
                        label = preset.label,
                        timeLabel = formatMinutes(minutes),
                        icon = if (selected) Icons.Filled.CheckCircle else preset.icon,
                        selected = selected,
                        reducedMotion = settings.reducedMotion,
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            } else {
                                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                            }
                            viewModel.setNotificationMinutes(minutes)
                            timeJustSaved = true
                        },
                        onLongClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            editingPreset = preset
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Text(
                text = "Long-press a preset to change its default time.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .clickable { showTimePicker = true },
            ) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsNone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Column(modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)) {
                    Text("Custom time", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "Daily at ${formatMinutes(settings.notificationMinutes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = { showTimePicker = true }) { Text("Change") }
            }
            if (timeJustSaved) {
                Text(
                    text = "Reminder updated — you'll be nudged daily at ${formatMinutes(settings.notificationMinutes)}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            OutlinedButton(
                onClick = { reminderPreviewResult = viewModel.sendReminderPreview() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .defaultMinSize(minHeight = 48.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Send,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Preview daily reminder",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            when (reminderPreviewResult) {
                true -> Text(
                    text = "Preview sent — check your notification shade. It looks exactly like the real daily reminder.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
                false -> Text(
                    text = "Notifications are not allowed. Enable them for Mindset Frames in system settings, then try again.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                )
                null -> Unit
            }
        }

        // Streak protection — evening alert only when today's habits aren't done
        SettingsCard(title = "Streak protection", animateSize = !settings.reducedMotion) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocalFireDepartment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Column(modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)) {
                    Text("Evening streak alert", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "Alerts you only if today's habits aren't done yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = settings.streakAlertEnabled,
                    onCheckedChange = { enabled ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        } else {
                            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        }
                        viewModel.setStreakAlertEnabled(enabled)
                    },
                )
            }

            if (settings.streakAlertEnabled) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .defaultMinSize(minHeight = 48.dp)
                        .clickable { showStreakTimePicker = true },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Column(modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)) {
                        Text("Check-up time", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "Daily at ${formatMinutes(settings.streakAlertMinutes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = { showStreakTimePicker = true }) { Text("Change") }
                }
                if (streakTimeJustSaved) {
                    Text(
                        text = "Updated — we'll check your progress daily at ${formatMinutes(settings.streakAlertMinutes)}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                Text(
                    text = "If everything is already checked off by then, the alert stays silent — no nagging.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )

                OutlinedButton(
                    onClick = { streakPreviewResult = viewModel.sendStreakAlertPreview() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .defaultMinSize(minHeight = 48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = s.settingsPreviewStreak,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                when (streakPreviewResult) {
                    true -> Text(
                        text = "Preview sent — check your notification shade.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    false -> Text(
                        text = "Notifications are not allowed. Enable them for Mindset Frames in system settings, then try again.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    null -> Unit
                }
            }
        }

        // Weekly recap — one Sunday-evening summary of the week
        SettingsCard(title = "Weekly recap", animateSize = !settings.reducedMotion) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Column(modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)) {
                    Text("Sunday recap", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "A short summary like \u201CYou checked in 5/7 days\u201D",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = settings.weeklyRecapEnabled,
                    onCheckedChange = { enabled ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        } else {
                            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                        }
                        viewModel.setWeeklyRecapEnabled(enabled)
                    },
                )
            }

            if (settings.weeklyRecapEnabled) {
                Text(
                    text = "Arrives Sunday evening around 6:00 PM. One notification a week — strong weeks get celebrated, quiet weeks get a fresh start. Never a guilt trip.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
                OutlinedButton(
                    onClick = { recapPreviewResult = viewModel.sendWeeklyRecapPreview() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .defaultMinSize(minHeight = 48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Preview weekly recap",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                when (recapPreviewResult) {
                    true -> Text(
                        text = "Preview sent with this week's real numbers — check your notification shade.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    false -> Text(
                        text = "Notifications are not allowed. Enable them for Mindset Frames in system settings, then try again.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    null -> Unit
                }
            }
        }

        // Share streak
        val checkInStreak = data.dailyCheckInStreak()
        if (checkInStreak > 0) {
            SettingsCard(title = "Your streak") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalFireDepartment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp),
                    ) {
                        Text(
                            text = if (checkInStreak == 1) "1-day streak" else "$checkInStreak-day streak",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Share your progress with friends",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            activity?.let { act ->
                                StreakShare.shareStreak(act as android.content.Context, data)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.IosShare,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "Share",
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }
            }
        }

        // About
        SettingsCard(title = "About") {
            // Highlighted version chip — real build values so store reviews
            // and support tickets always reference the exact release.
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    text = "Mindset Frames v${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
            Text(
                text = "Your data lives on this device and works fully offline. Optional cloud backup is offered on the Today screen and keeps your progress safe if you switch devices.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { showPrivacyPolicy = true }
                    .defaultMinSize(minHeight = 48.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.PrivacyTip,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                ) {
                    Text("Privacy Policy", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "How your data is handled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = "View privacy policy",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Terms & Conditions — the service, the premium tier, fair use,
            // and liability, fully disclosed in-app.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { showTerms = true }
                    .defaultMinSize(minHeight = 48.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                ) {
                    Text("Terms & Conditions", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "The service, premium, and fair use — fully disclosed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = "View terms and conditions",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Follow row — deliberately low-key and Settings-only. Outbound
            // links never appear on the daily check-in screen (core loop).
            val aboutContext = LocalContext.current
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 12.dp, bottom = 12.dp),
            )
            Text(
                text = "Follow Mindset Frames",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            // Official logos only — no text labels. Each is a 48dp tap
            // target; the platform name is exposed to screen readers instead.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp),
            ) {
                socialLinks.forEach { link ->
                    IconButton(
                        onClick = { openExternalLink(aboutContext, link.url) },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = link.icon,
                            contentDescription = "Follow Mindset Frames on ${link.label}",
                            tint = link.brandColor,
                            modifier = Modifier.size(30.dp),
                        )
                    }
                }
            }
            Text(
                text = "Questions or feedback: $CONTACT_EMAIL",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }

    if (showPremiumSheet) {
        PremiumSheet(onDismiss = { showPremiumSheet = false })
    }

    if (showCompanionStudio) {
        com.rork.mindsetframestracker.ui.avatar.CompanionStudioSheet(
            viewModel = viewModel,
            onDismiss = { showCompanionStudio = false },
        )
    }

    if (showGrounding) {
        GroundingSheet(
            viewModel = viewModel,
            onDismiss = { showGrounding = false },
        )
    }

    if (showPrivacyPolicy) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyPolicy = false })
    }

    if (showTerms) {
        TermsDialog(onDismiss = { showTerms = false })
    }

    if (showReportMonthPicker) {
        AlertDialog(
            onDismissRequest = { showReportMonthPicker = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.PictureAsPdf,
                    contentDescription = null,
                )
            },
            title = { Text("Export report") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = "Pick a month to export as a high-resolution PDF — or choose your own date range.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    MonthlyReportPdf.exportableMonths(data).forEach { month ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .clickable {
                                    showReportMonthPicker = false
                                    activity?.let { act ->
                                        MonthlyReportPdf.shareMonthlyReport(act, data, month)
                                    }
                                }
                                .defaultMinSize(minHeight = 48.dp)
                                .padding(horizontal = 8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = monthLabel(month),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 12.dp),
                            )
                            if (month == YearMonth.now()) {
                                Text(
                                    text = "In progress",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .clickable {
                                showReportMonthPicker = false
                                showReportRangePicker = true
                            }
                            .defaultMinSize(minHeight = 48.dp)
                            .padding(horizontal = 8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "Custom date range\u2026",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp),
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showReportMonthPicker = false },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                ) { Text("Cancel") }
            },
        )
    }

    if (showReportRangePicker) {
        ReportRangeDialog(
            onDismiss = { showReportRangePicker = false },
            onExport = { start, end ->
                showReportRangePicker = false
                activity?.let { act ->
                    MonthlyReportPdf.shareRangeReport(act, data, start, end)
                }
            },
        )
    }

    editingPreset?.let { preset ->
        key(preset.id) {
            val currentMinutes = settings.presetTimes[preset.id] ?: preset.defaultMinutes
            val presetTimeState = rememberTimePickerState(
                initialHour = currentMinutes / 60,
                initialMinute = currentMinutes % 60,
                is24Hour = false,
            )
            AlertDialog(
                onDismissRequest = { editingPreset = null },
                icon = {
                    Icon(imageVector = preset.icon, contentDescription = null)
                },
                title = { Text("${preset.label} default time") },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Tapping ${preset.label} will set your daily reminder to this time.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp),
                        )
                        TimePicker(state = presetTimeState)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val newMinutes = presetTimeState.hour * 60 + presetTimeState.minute
                            viewModel.setPresetTime(preset.id, newMinutes)
                            // Keep the live reminder in sync when this preset is the active one.
                            if (settings.notificationMinutes == currentMinutes) {
                                viewModel.setNotificationMinutes(newMinutes)
                                timeJustSaved = true
                            }
                            editingPreset = null
                        },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                    ) { Text("Save") }
                },
                dismissButton = {
                    Row {
                        TextButton(
                            onClick = {
                                viewModel.setPresetTime(preset.id, preset.defaultMinutes)
                                if (settings.notificationMinutes == currentMinutes) {
                                    viewModel.setNotificationMinutes(preset.defaultMinutes)
                                    timeJustSaved = true
                                }
                                editingPreset = null
                            },
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                        ) { Text("Reset") }
                        TextButton(
                            onClick = { editingPreset = null },
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                        ) { Text("Cancel") }
                    }
                },
            )
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = settings.notificationMinutes / 60,
            initialMinute = settings.notificationMinutes % 60,
            is24Hour = false,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.AccessTime,
                    contentDescription = null,
                )
            },
            title = { Text("Reminder time") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    TimePicker(state = timeState)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setNotificationMinutes(timeState.hour * 60 + timeState.minute)
                        showTimePicker = false
                        timeJustSaved = true
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTimePicker = false },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                ) { Text("Cancel") }
            },
        )
    }

    if (showStreakTimePicker) {
        val streakTimeState = rememberTimePickerState(
            initialHour = settings.streakAlertMinutes / 60,
            initialMinute = settings.streakAlertMinutes % 60,
            is24Hour = false,
        )
        AlertDialog(
            onDismissRequest = { showStreakTimePicker = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.LocalFireDepartment,
                    contentDescription = null,
                )
            },
            title = { Text("Streak check-up time") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "We'll look at your progress at this time each day and only alert you if habits are still unchecked. An evening time works best.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                    TimePicker(state = streakTimeState)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setStreakAlertMinutes(streakTimeState.hour * 60 + streakTimeState.minute)
                        showStreakTimePicker = false
                        streakTimeJustSaved = true
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showStreakTimePicker = false },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                ) { Text("Cancel") }
            },
        )
    }
    }
}

/**
 * Full privacy policy shown inside the app — no external website needed.
 * AppGallery Connect still requires a hosted URL in the listing, but users
 * read it here.
 */
@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.PrivacyTip,
                contentDescription = null,
            )
        },
        title = { Text("Privacy Policy") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = PRIVACY_POLICY_UPDATED,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                privacyPolicySections.forEach { (heading, body) ->
                    Text(
                        text = heading,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 14.dp),
                    )
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
            ) { Text("Got it") }
        },
    )
}

/**
 * In-app Terms & Conditions covering the service, the premium tier,
 * acceptable use, and liability — required disclosure for the Huawei
 * AppGallery listing.
 */
@Composable
fun TermsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = null,
            )
        },
        title = { Text("Terms & Conditions") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = TERMS_UPDATED,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                termsSections.forEach { (heading, body) ->
                    Text(
                        text = heading,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 14.dp),
                    )
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
            ) { Text("Got it") }
        },
    )
}

/**
 * Custom date-range picker for the PDF report. Future dates can't be
 * selected, ranges are capped at roughly a year so the daily chart stays
 * readable, and Export enables once both ends of the range are picked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportRangeDialog(
    onDismiss: () -> Unit,
    onExport: (LocalDate, LocalDate) -> Unit,
) {
    val today = remember { LocalDate.now() }
    val rangeState = rememberDateRangePickerState(
        yearRange = IntRange(today.year - 4, today.year),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                !utcLocalDate(utcTimeMillis).isAfter(today)

            override fun isSelectableYear(year: Int): Boolean = year <= today.year
        },
    )
    val start = rangeState.selectedStartDateMillis?.let(::utcLocalDate)
    val end = rangeState.selectedEndDateMillis?.let(::utcLocalDate)
    val spanDays = if (start != null && end != null) {
        ChronoUnit.DAYS.between(start, end) + 1
    } else {
        null
    }
    val tooLong = (spanDays ?: 0L) > MonthlyReportPdf.MAX_RANGE_DAYS

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = start != null && end != null && !tooLong,
                onClick = { if (start != null && end != null) onExport(start, end) },
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
            ) { Text("Export PDF") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
            ) { Text("Cancel") }
        },
    ) {
        DateRangePicker(
            state = rangeState,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = when {
                tooLong -> "That's $spanDays days — reports cover up to ${MonthlyReportPdf.MAX_RANGE_DAYS} days (about a year). Pick a shorter range."
                spanDays == 1L -> "1 day selected."
                spanDays != null -> "$spanDays days selected."
                start != null -> "Now pick the end date."
                else -> "Pick a start and end date — any period up to a year."
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (tooLong) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
    }
}

/** The range picker reports UTC-midnight millis for each picked calendar date. */
private fun utcLocalDate(utcTimeMillis: Long): LocalDate =
    Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()

/**
 * Profile header showing the user's avatar (initial), display name/email, and
 * a small premium badge when premium features are unlocked. Tapping the badge
 * opens a benefits explainer dialog (when premium) or the upgrade sheet (when
 * not premium).
 */
@Composable
private fun ProfileHeader(
    email: String?,
    avatar: com.rork.mindsetframestracker.data.AvatarConfig =
        com.rork.mindsetframestracker.data.AvatarConfig(),
    onAvatarClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val s = appStrings()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 4.dp),
    ) {
        com.rork.mindsetframestracker.ui.avatar.CompanionAvatar(
            config = avatar,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .then(
                    if (onAvatarClick != null) Modifier.clickable(onClick = onAvatarClick)
                    else Modifier,
                ),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp),
        ) {
            Text(
                text = email ?: s.settingsLocalUser,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = s.settingsFreePlan,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

/**
 * One row in the Companion & wellbeing card: icon, title, description, and
 * an optional trailing action that opens the feature.
 */
@Composable
private fun WellbeingRow(
    icon: ImageVector,
    title: String,
    description: String,
    actionLabel: String?,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (actionLabel != null && onClick != null) {
            TextButton(onClick = onClick) { Text(actionLabel) }
        }
    }
}

/** "Just now" for fresh backups, then Android's relative time ("5 min. ago"). */
private fun formatBackupTime(lastSyncAtMs: Long): String {
    val elapsed = System.currentTimeMillis() - lastSyncAtMs
    return if (elapsed < 60_000L) "Just now"
    else DateUtils.getRelativeTimeSpanString(lastSyncAtMs).toString()
}

/**
 * Signed-in account management: identity summary (avatar, email, provider),
 * backup freshness, manual sync, and sign-out behind a confirmation dialog.
 * Account creation/sign-in is deliberately NOT offered here — it lives only
 * in the save-your-progress popup on the Today screen.
 */
@Composable
private fun AccountSection(
    syncState: SyncUiState,
    onSignOut: () -> Unit,
    onSyncNow: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    var showSignOutConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val email = syncState.email.orEmpty()

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
        ) {
            Text(
                text = email.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Text(
                text = email,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = if (syncState.provider == "huawei") "Connected with HUAWEI ID"
                    else "Connected with email",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
    }

    // Backup freshness — relative time keeps it glanceable.
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.CloudDone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = if (syncState.lastSyncAtMs > 0L) {
                "Last backup: ${formatBackupTime(syncState.lastSyncAtMs)}"
            } else {
                "No backup yet — tap Sync now"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp),
        )
    }

    Button(
        onClick = onSyncNow,
        enabled = !syncState.busy && syncState.cooldownSecondsLeft == 0,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .defaultMinSize(minHeight = 48.dp),
    ) {
        if (syncState.busy) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
            Icon(
                imageVector = Icons.Outlined.CloudSync,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = if (syncState.cooldownSecondsLeft > 0) {
                "Synced — ready in ${syncState.cooldownSecondsLeft}s"
            } else {
                "Sync now"
            },
            modifier = Modifier.padding(start = 8.dp),
        )
    }
    OutlinedButton(
        onClick = { showSignOutConfirm = true },
        enabled = !syncState.busy,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .defaultMinSize(minHeight = 48.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Logout,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Text(text = "Sign out", modifier = Modifier.padding(start = 8.dp))
    }

    // Danger zone — Huawei AppGallery requires an in-app account deletion path.
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
    )
    TextButton(
        onClick = { showDeleteConfirm = true },
        enabled = !syncState.busy,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.DeleteOutline,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Text(text = "Delete account", modifier = Modifier.padding(start = 8.dp))
    }
    Text(
        text = "Permanently erases your account and cloud backup. Habits on this device are kept.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )

    // Hidden while the delete dialog is open — errors show inside the dialog.
    if (!showDeleteConfirm) {
        syncState.message?.let { message ->
            AuthMessageBanner(
                message = message,
                isError = syncState.isError,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }

    if (showDeleteConfirm) {
        DeleteAccountDialog(
            email = email,
            busy = syncState.busy,
            errorMessage = syncState.message?.takeIf { syncState.isError },
            onConfirm = onDeleteAccount,
            onDismiss = { showDeleteConfirm = false },
        )
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            },
            title = { Text("Sign out?") },
            text = {
                Text(
                    "Your habits and moods stay on this device, and your cloud backup " +
                        "stays safe under $email. Sign back in anytime to keep syncing."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirm = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                ) { Text("Sign out") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSignOutConfirm = false },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                ) { Text("Cancel") }
            },
        )
    }
}

/**
 * Irreversible-action dialog for account deletion. The destructive button
 * stays disabled until the user types DELETE exactly (all caps), the whole
 * dialog locks while the server call runs, and failures surface inline.
 * Success closes it automatically — the signed-in account card unmounts.
 */
@Composable
private fun DeleteAccountDialog(
    email: String,
    busy: Boolean,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var confirmation by remember { mutableStateOf("") }
    val armed = confirmation.trim() == "DELETE"

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        icon = {
            Icon(
                imageVector = Icons.Outlined.DeleteForever,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(30.dp),
            )
        },
        title = { Text("Delete account?") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "This permanently erases your account ($email) and every habit, " +
                        "check-in, and mood in your cloud backup. It cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Your data on this device is kept — it just won't be backed up anymore.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
                OutlinedTextField(
                    value = confirmation,
                    onValueChange = { confirmation = it },
                    label = { Text("Type DELETE to confirm") },
                    singleLine = true,
                    enabled = !busy,
                    isError = confirmation.isNotEmpty() && !armed,
                    supportingText = {
                        if (confirmation.isNotEmpty() && !armed) {
                            Text("Type DELETE in capital letters")
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                )
                errorMessage?.let { message ->
                    AuthMessageBanner(
                        message = message,
                        isError = true,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = armed && !busy,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
            ) {
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onError,
                    )
                    Text(text = "Deleting…", modifier = Modifier.padding(start = 8.dp))
                } else {
                    Text("Delete forever")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !busy,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
            ) { Text("Cancel") }
        },
    )
}

/** A quick-pick reminder preset whose default time the user can customize via long-press. */
private data class ReminderPreset(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val defaultMinutes: Int,
)

private val reminderPresets = listOf(
    ReminderPreset("morning", "Morning", Icons.Outlined.WbSunny, 8 * 60),
    ReminderPreset("midday", "Midday", Icons.Outlined.LightMode, 12 * 60 + 30),
    ReminderPreset("evening", "Evening", Icons.Outlined.DarkMode, 20 * 60),
)

/**
 * FilterChip-style preset button with long-press support (FilterChip has no
 * onLongClick, so this uses combinedClickable on a styled Surface).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PresetChip(
    label: String,
    timeLabel: String,
    icon: ImageVector,
    selected: Boolean,
    reducedMotion: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val chipScale by animateFloatAsState(
        targetValue = if (isPressed && !reducedMotion) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "presetChipScale",
    )
    val shape = MaterialTheme.shapes.small
    Surface(
        shape = shape,
        color = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .graphicsLayer {
                scaleX = chipScale
                scaleY = chipScale
            },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(shape)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    onClick = onClick,
                    onLongClick = onLongClick,
                    onLongClickLabel = "Change default time for $label",
                )
                .defaultMinSize(minHeight = 48.dp)
                .padding(horizontal = 4.dp, vertical = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            Text(
                text = timeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    animateSize: Boolean = false,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .then(
                    if (animateSize) {
                        Modifier.animateContentSize(
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                        )
                    } else {
                        Modifier
                    }
                )
                .padding(20.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            content()
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    val time = LocalTime.of(minutes / 60, minutes % 60)
    return time.format(DateTimeFormatter.ofPattern("h:mm a"))
}

/** "July 2026"-style label for the report month picker. */
private fun monthLabel(month: YearMonth): String =
    month.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
