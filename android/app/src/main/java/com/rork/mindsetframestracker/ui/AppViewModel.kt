package com.rork.mindsetframestracker.ui

import android.app.Application
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.util.Patterns
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rork.mindsetframestracker.BuildConfig
import com.rork.mindsetframestracker.auth.HuaweiAuthClient
import com.rork.mindsetframestracker.data.AppData
import com.rork.mindsetframestracker.data.CloudBackupWorker
import com.rork.mindsetframestracker.data.Dates
import com.rork.mindsetframestracker.data.BadgeTier
import com.rork.mindsetframestracker.data.Habit
import com.rork.mindsetframestracker.data.MAX_FREE_HABITS
import com.rork.mindsetframestracker.data.MindsetRepository
import com.rork.mindsetframestracker.data.MoodMode
import com.rork.mindsetframestracker.data.hasFeatureAccess
import com.rork.mindsetframestracker.data.regionalLanguageFor
import com.rork.mindsetframestracker.data.universallyFreeLanguages
import com.rork.mindsetframestracker.data.SupabaseSync
import com.rork.mindsetframestracker.data.ThemeMode
import com.rork.mindsetframestracker.notifications.CheckInNotifier
import com.rork.mindsetframestracker.notifications.NotificationScheduler
import com.rork.mindsetframestracker.notifications.StreakAlertNotifier
import com.rork.mindsetframestracker.notifications.WeeklyRecapNotifier
import com.rork.mindsetframestracker.ui.avatar.AvatarCatalog
import com.rork.mindsetframestracker.util.LocalizationManager
import com.rork.mindsetframestracker.util.isBatteryLow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID

/** Max habit name length — mirrors the Supabase column constraint (schema check <= 60). */
const val MAX_HABIT_NAME_LENGTH = 60

/** Minimum password length enforced client-side at sign-up. */
const val MIN_PASSWORD_LENGTH = 8

/**
 * UI state for the optional cloud backup & sync (user's own Supabase project).
 * Available only when Supabase credentials are configured at build time.
 */
data class SyncUiState(
    val available: Boolean = false,
    /** True when Huawei Account Kit sign-in is available (HMS Core present). */
    val huaweiAvailable: Boolean = false,
    val email: String? = null,
    /** Auth provider of the active session — "huawei" or "email". */
    val provider: String? = null,
    /** Epoch millis of the last successful cloud backup (0 = never). */
    val lastSyncAtMs: Long = 0L,
    val busy: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    /** Seconds remaining before another manual sync is allowed (0 = ready). */
    val cooldownSecondsLeft: Int = 0,
    /** One-shot flag: sign-up hit "email already registered" — UI should
     * switch to the Sign In tab, then call [AppViewModel.consumeSuggestSignIn]. */
    val suggestSignIn: Boolean = false,
)

/** Minimum gap between manual syncs, to avoid spamming Supabase with duplicate pushes. */
private const val SYNC_COOLDOWN_MS = 15_000L

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MindsetRepository? = runCatching {
        MindsetRepository(application)
    }.onFailure {
        if (BuildConfig.DEBUG) Log.e("AppViewModel", "Repository init failed: ${it.message}", it)
    }.getOrNull()

    private val notificationScheduler = runCatching {
        NotificationScheduler(application)
    }.onFailure {
        if (BuildConfig.DEBUG) Log.e("AppViewModel", "NotificationScheduler init failed: ${it.message}", it)
    }.getOrNull() ?: NotificationScheduler(application)

    private val supabaseSync = runCatching {
        SupabaseSync(application)
    }.onFailure {
        if (BuildConfig.DEBUG) Log.e("AppViewModel", "SupabaseSync init failed: ${it.message}", it)
    }.getOrNull() ?: SupabaseSync(application)

    private val _state = MutableStateFlow(repository?.load() ?: AppData())
    val state: StateFlow<AppData> = _state.asStateFlow()

    init {
        runCatching {
            LocalizationManager.init(application)
            LocalizationManager.contentFor(_state.value.settings.language)
            LocalizationManager.loadStringJson(_state.value.settings.language.code)
        }.onFailure {
            if (BuildConfig.DEBUG) Log.e("AppViewModel", "Localization init failed: ${it.message}", it)
        }
        runCatching { ensureRegionalLanguage() }.onFailure {
            if (BuildConfig.DEBUG) Log.e("AppViewModel", "Regional language resolve failed: ${it.message}", it)
        }
        runCatching { retryPendingSync() }.onFailure {
            if (BuildConfig.DEBUG) Log.e("AppViewModel", "Retry pending sync failed: ${it.message}", it)
        }
        runCatching { alignDailyBackup() }.onFailure {
            if (BuildConfig.DEBUG) Log.e("AppViewModel", "Daily backup alignment failed: ${it.message}", it)
        }
        runCatching { syncCompanionReminders() }.onFailure {
            if (BuildConfig.DEBUG) Log.e("AppViewModel", "Companion reminder sync failed: ${it.message}", it)
        }
        runCatching { refreshCompanionUnlocks() }.onFailure {
            if (BuildConfig.DEBUG) Log.e("AppViewModel", "Companion unlock refresh failed: ${it.message}", it)
        }
    }

    // ── Companion Studio task unlocks ──────────────────────────

    private val _newCompanionUnlocks = MutableStateFlow<List<String>>(emptyList())

    val newCompanionUnlocks: StateFlow<List<String>> = _newCompanionUnlocks.asStateFlow()

    fun refreshCompanionUnlocks() {
        val data = _state.value
        val newly = AvatarCatalog.taskUnlockableIds(data)
            .filter { it !in data.settings.companionUnlocks }
        if (newly.isEmpty()) return
        update { d ->
            d.copy(
                settings = d.settings.copy(
                    companionUnlocks = d.settings.companionUnlocks + newly,
                )
            )
        }
        _newCompanionUnlocks.value = (_newCompanionUnlocks.value + newly).distinct()
    }

    fun consumeCompanionUnlocks() {
        _newCompanionUnlocks.value = emptyList()
    }

    /**
     * Aligns the one-shot companion alarms with persisted state at launch.
     * Safe to call repeatedly — stale alarms are cancelled/replaced.
     */
    private fun syncCompanionReminders() {
// No trial payment reminders needed — no billing system.
    }

    /**
     * Offline resilience: if a previous session left local changes that never
     * reached the cloud, retry the backup automatically on launch.
     */
    private fun retryPendingSync() {
        if (supabaseSync.isConfigured && supabaseSync.isSignedIn && supabaseSync.hasPendingPush) {
            queueSync()
        }
    }

    /**
     * Keeps the WorkManager daily backup aligned with the session: scheduled
     * while signed in, cancelled when signed out. Safe to call repeatedly.
     */
    private fun alignDailyBackup() {
        if (supabaseSync.isConfigured && supabaseSync.isSignedIn) {
            CloudBackupWorker.schedule(getApplication())
        } else {
            CloudBackupWorker.cancel(getApplication())
        }
    }

    private val _syncState = MutableStateFlow(
        SyncUiState(
            available = supabaseSync.isConfigured,
            huaweiAvailable = supabaseSync.isConfigured &&
                HuaweiAuthClient.isHmsAvailable(application),
            email = supabaseSync.sessionEmail,
            provider = supabaseSync.sessionProvider,
            lastSyncAtMs = supabaseSync.lastSyncAtMs,
        )
    )
    val syncState: StateFlow<SyncUiState> = _syncState.asStateFlow()

    // ── Save-your-data prompt ────────────────────────────────

    private val _showAuthPrompt = MutableStateFlow(false)
    val showAuthPrompt: StateFlow<Boolean> = _showAuthPrompt.asStateFlow()

    fun maybeShowAuthPrompt() {
        if (_state.value.settings.authPromptDone) return
        if (!supabaseSync.isConfigured || supabaseSync.isSignedIn) return
        if (!_state.value.settings.onboardingDone) return
        // Privacy consent is NOT required to SHOW the sheet — the consent
        // checkbox lives inside it and gates the login buttons themselves,
        // which is what AppGallery's consent-before-login-trigger rule needs.
        markAuthPromptDone()
        _showAuthPrompt.value = true
    }

    /**
     * Re-opens the save-your-progress sheet on demand — Settings' "Back up &
     * restore" entry for anyone who dismissed the one-time popup earlier.
     */
    fun openAuthPrompt() {
        if (!supabaseSync.isConfigured || supabaseSync.isSignedIn) return
        markAuthPromptDone()
        _showAuthPrompt.value = true
    }

    private fun markAuthPromptDone() {
        if (!_state.value.settings.authPromptDone) {
            update { it.copy(settings = it.settings.copy(authPromptDone = true)) }
        }
    }

    fun dismissAuthPrompt() {
        _showAuthPrompt.value = false
    }

    // ── Privacy consent ────────────────────────────────────────

    /**
     * Records that the user has read and accepted the Privacy Policy.
     * Required by Huawei AppGallery before any login trigger fires.
     */
    fun acceptPrivacyConsent() {
        update { it.copy(settings = it.settings.copy(privacyConsentAccepted = true)) }
    }

    /** True when the user has accepted the privacy policy. */
    fun isPrivacyConsentAccepted(): Boolean = _state.value.settings.privacyConsentAccepted

    private fun update(transform: (AppData) -> AppData) {
        val next = transform(_state.value)
        _state.value = next
        repository?.save(next)
    }

    private fun validateCredentials(email: String, password: String, forSignUp: Boolean): String? {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() || password.isEmpty() -> "Enter your email and password"
            !Patterns.EMAIL_ADDRESS.matcher(trimmed).matches() -> "Enter a valid email address"
            forSignUp && password.length < MIN_PASSWORD_LENGTH ->
                "Use at least $MIN_PASSWORD_LENGTH characters for your password"
            else -> null
        }
    }

    private fun rejectAuth(message: String) {
        _syncState.value = _syncState.value.copy(busy = false, message = message, isError = true)
    }

    /** Signs in, then pulls the user's cloud snapshot and merges it locally. */
    fun signIn(email: String, password: String) {
        if (_syncState.value.busy) return
        validateCredentials(email, password, forSignUp = false)?.let { rejectAuth(it); return }
        _syncState.value = _syncState.value.copy(busy = true, message = null, isError = false)
        viewModelScope.launch {
            val error = supabaseSync.signIn(email, password)
            if (error != null) {
                _syncState.value = _syncState.value.copy(busy = false, message = error, isError = true)
                return@launch
            }
            onSignedIn("Signed in")
        }
    }

    /**
     * Huawei ID sign-in — sends the Huawei-signed ID token to the backend,
     * which verifies it with Huawei's account server and returns a Supabase
     * session. The user never sees a password; nothing on-device can derive
     * one.
     */
    fun signInWithHuawei(idToken: String, email: String?, displayName: String? = null) {
        if (_syncState.value.busy) return
        if (!supabaseSync.isConfigured) {
            rejectAuth("Cloud sync is not configured")
            return
        }
        _syncState.value = _syncState.value.copy(busy = true, message = null, isError = false)
        viewModelScope.launch {
            val error = supabaseSync.signInWithHuawei(idToken, email, displayName)
            if (error != null) {
                _syncState.value = _syncState.value.copy(busy = false, message = error, isError = true)
            } else {
                onSignedIn("Signed in with Huawei ID")
            }
        }
    }

    /** Surfaces a Huawei Account Kit failure inside the auth sheet. */
    fun onHuaweiSignInFailed(message: String) {
        rejectAuth(message)
    }

    /**
     * Dual-free language model: resolves the ONE regional language this
     * install unlocks for free. Runs once — the grant is persisted (and
     * synced) the first time a regional language can be determined. An
     * already-selected non-English language is grandfathered in first so an
     * update can never lock anyone out of their active language.
     */
    private fun ensureRegionalLanguage() {
        val settings = _state.value.settings
        if (settings.freeRegionalLanguage != null) return
        val selectedNonEnglish = settings.language.takeIf { it !in universallyFreeLanguages }
        val regional = selectedNonEnglish
            ?: regionalLanguageFor(Locale.getDefault())
            ?: return
        update { it.copy(settings = it.settings.copy(freeRegionalLanguage = regional)) }
        queueSync()
    }

    /**
     * Consumes an auth-callback deep link from the mindsetframes.online
     * web-bridge (email confirmation, magic link, recovery). Each link is
     * processed at most once — a persisted signature guard drops replays —
     * and the forwarded one-time refresh token is exchanged server-side,
     * which rotates it immediately.
     */
    fun handleAuthDeepLink(uri: Uri) {
        val signature = runCatching {
            MessageDigest.getInstance("SHA-256")
                .digest(uri.toString().toByteArray())
                .joinToString("") { "%02x".format(it) }
        }.getOrNull() ?: return
        if (!supabaseSync.consumeAuthLink(signature)) return

        val params = authLinkParams(uri)
        val errorCode = params["error_code"] ?: params["error"]
        if (errorCode != null) {
            val friendly = if (errorCode == "otp_expired" || errorCode == "access_denied") {
                "That email link has expired — request a fresh one from the sign-in screen."
            } else {
                "That email link couldn't be verified. Request a new one and try again."
            }
            _syncState.value = _syncState.value.copy(message = friendly, isError = true)
            return
        }

        val refreshToken = params["refresh_token"]
        if (!refreshToken.isNullOrBlank()) {
            if (_syncState.value.busy) return
            _syncState.value = _syncState.value.copy(busy = true, message = null, isError = false)
            viewModelScope.launch {
                val error = supabaseSync.signInWithRecoveredToken(refreshToken)
                if (error != null) {
                    _syncState.value = _syncState.value.copy(busy = false, message = error, isError = true)
                } else {
                    onSignedIn("Email verified — you're signed in!")
                }
            }
            return
        }

        // Verified link without tokens (older template or manual hand-off):
        // confirm success and route the user to the sign-in sheet.
        if (params["type"] != null && params["type"] != "manual") {
            _syncState.value = _syncState.value.copy(
                message = "Email verified! Sign in with your password to finish.",
                isError = false,
            )
            if (!supabaseSync.isSignedIn) openAuthPrompt()
        }
    }

    /** Merges query + fragment parameters of an auth deep link (fragment wins). */
    private fun authLinkParams(uri: Uri): Map<String, String> {
        val params = mutableMapOf<String, String>()
        runCatching {
            uri.queryParameterNames.forEach { name ->
                uri.getQueryParameter(name)?.let { params[name] = it }
            }
        }
        uri.fragment.orEmpty().split('&').forEach { pair ->
            val separator = pair.indexOf('=')
            if (separator > 0) {
                val key = pair.substring(0, separator)
                val value = runCatching {
                    URLDecoder.decode(pair.substring(separator + 1), "UTF-8")
                }.getOrNull()
                if (value != null) params[key] = value
            }
        }
        return params
    }

    /** Shared post-auth path: pull the cloud snapshot, then queue a backup of local data. */
    private suspend fun onSignedIn(successMessage: String) {
        restoreFromCloud()
        _syncState.value = _syncState.value.copy(
            busy = false,
            email = supabaseSync.sessionEmail,
            provider = supabaseSync.sessionProvider,
            message = successMessage,
            isError = false,
        )
        if (_state.value.settings.onboardingDone) {
            _showAuthPrompt.value = true
        }
        queueSync()
        alignDailyBackup()
    }

    /** Creates an account; when the project requires email confirmation, shows that message. */
    fun signUp(email: String, password: String) {
        if (_syncState.value.busy) return
        validateCredentials(email, password, forSignUp = true)?.let { rejectAuth(it); return }
        _syncState.value = _syncState.value.copy(busy = true, message = null, isError = false)
        viewModelScope.launch {
            val message = supabaseSync.signUp(email, password)
            val signedIn = supabaseSync.sessionEmail != null
            _syncState.value = _syncState.value.copy(
                busy = false,
                email = supabaseSync.sessionEmail,
                provider = supabaseSync.sessionProvider,
                message = message ?: "Account created — you're signed in",
                isError = message != null && !signedIn && !message.startsWith("Account created"),
            )
            if (signedIn) {
                queueSync()
                alignDailyBackup()
            }
        }
    }

    fun signOut() {
        if (supabaseSync.sessionProvider == "huawei") {
            // Revoke the cached HMS state so the next sign-in shows the picker.
            HuaweiAuthClient.signOut(getApplication())
        }
        supabaseSync.signOut()
        alignDailyBackup()
        update { it.copy(settings = it.settings.copy(authPromptDone = false)) }
        _syncState.value = _syncState.value.copy(
            email = null,
            provider = null,
            lastSyncAtMs = 0L,
            message = "Signed out — your data stays on this device.",
            isError = false,
        )
    }

    fun deleteAccount() {
        if (_syncState.value.busy) return
        if (!supabaseSync.isConfigured || !supabaseSync.isSignedIn) return
        _syncState.value = _syncState.value.copy(busy = true, message = null, isError = false)
        viewModelScope.launch {
            val error = supabaseSync.deleteAccount()
            if (error != null) {
                _syncState.value = _syncState.value.copy(busy = false, message = error, isError = true)
                return@launch
            }
            queuedSyncJob?.cancel()
            alignDailyBackup()
            markAuthPromptDone()
            _syncState.value = _syncState.value.copy(
                busy = false,
                email = null,
                provider = null,
                lastSyncAtMs = 0L,
                message = "Account deleted — your cloud data is permanently erased. " +
                    "Your habits stay on this device.",
                isError = false,
            )
        }
    }

    fun clearSyncMessage() {
        if (_syncState.value.message != null) {
            _syncState.value = _syncState.value.copy(message = null)
        }
    }

    fun sendPasswordReset(email: String) {
        if (_syncState.value.busy) return
        val trimmed = email.trim()
        if (trimmed.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
            rejectAuth("Enter a valid email address")
            return
        }
        _syncState.value = _syncState.value.copy(busy = true, message = null, isError = false)
        viewModelScope.launch {
            val error = supabaseSync.sendPasswordReset(email)
            _syncState.value = _syncState.value.copy(
                busy = false,
                message = error ?: "Password reset email sent — check your inbox",
                isError = error != null,
            )
        }
    }

    private suspend fun restoreFromCloud() {
        val (snapshot, error) = supabaseSync.pullSnapshot()
        if (snapshot == null) {
            if (error != null) {
                _syncState.value = _syncState.value.copy(message = error, isError = true)
            }
            return
        }
        update { data ->
            val localPins = data.habits.filter { it.isPinned }.map { it.id }.toSet()
            val mergedHabits = (snapshot.habits + data.habits)
                .distinctBy { it.id }
                .map { habit -> if (habit.id in localPins) habit.copy(isPinned = true) else habit }
            val mergedCheckIns = (data.checkIns.keys + snapshot.checkIns.keys).associateWith { habitId ->
                (data.checkIns[habitId].orEmpty() + snapshot.checkIns[habitId].orEmpty()).distinct()
            }
            data.copy(
                habits = mergedHabits,
                checkIns = mergedCheckIns,
                moodHistory = snapshot.moodHistory + data.moodHistory,
            )
        }
    }

    private var lastSyncAttemptAt = 0L
    private var cooldownJob: kotlinx.coroutines.Job? = null
    private var queuedSyncJob: kotlinx.coroutines.Job? = null

    private val queuedSyncDelayMs = 4_000L

    fun queueSync() {
        if (!supabaseSync.isConfigured || !supabaseSync.isSignedIn) return
        supabaseSync.hasPendingPush = true
        queuedSyncJob?.cancel()
        queuedSyncJob = viewModelScope.launch {
            kotlinx.coroutines.delay(queuedSyncDelayMs)
            while (_syncState.value.busy || _syncState.value.cooldownSecondsLeft > 0) {
                kotlinx.coroutines.delay(1_000)
            }
            if (!supabaseSync.isOnline) return@launch
            if (isBatteryLow(getApplication())) return@launch
            // Visible progress: the global status banner shows a spinner while
            // the automatic backup runs, and a persistent alert with Retry if
            // it fails — background syncs are never silent anymore.
            _syncState.value = _syncState.value.copy(busy = true)
            val error = supabaseSync.pushSnapshot(_state.value)
            if (error == null) {
                supabaseSync.hasPendingPush = false
                _syncState.value = _syncState.value.copy(
                    busy = false,
                    lastSyncAtMs = supabaseSync.lastSyncAtMs,
                )
            } else {
                _syncState.value = _syncState.value.copy(
                    busy = false,
                    message = error,
                    isError = true,
                )
            }
        }
    }

    /** Retry action for the sync-failure alert — clears it and re-runs the backup. */
    fun retrySync() {
        lastSyncAttemptAt = 0L
        _syncState.value = _syncState.value.copy(message = null, isError = false)
        syncNow()
    }

    fun syncNow() {
        val now = System.currentTimeMillis()
        val state = _syncState.value
        if (!supabaseSync.isConfigured || state.busy || state.cooldownSecondsLeft > 0) return
        if (now - lastSyncAttemptAt < SYNC_COOLDOWN_MS) return
        if (!supabaseSync.isOnline) {
            if (supabaseSync.isSignedIn) supabaseSync.hasPendingPush = true
            _syncState.value = state.copy(
                message = "You're offline — your data is saved on this device and will back up when you're back online.",
                isError = false,
            )
            return
        }
        if (isBatteryLow(getApplication())) {
            if (supabaseSync.isSignedIn) supabaseSync.hasPendingPush = true
            _syncState.value = state.copy(
                message = "Low Power — sync is paused below 20% battery to conserve energy. Your data is saved on this device.",
                isError = false,
            )
            return
        }
        lastSyncAttemptAt = now
        _syncState.value = state.copy(busy = true, message = null, isError = false)
        viewModelScope.launch {
            val error = supabaseSync.pushSnapshot(_state.value)
            if (error == null) {
                supabaseSync.hasPendingPush = false
            } else {
                // Never let the cooldown block an immediate retry of a failure.
                lastSyncAttemptAt = 0L
            }
            _syncState.value = _syncState.value.copy(
                busy = false,
                message = error ?: "Backed up just now",
                isError = error != null,
                lastSyncAtMs = supabaseSync.lastSyncAtMs,
            )
            if (error == null) startSyncCooldown()
        }
    }

    private fun startSyncCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            var secondsLeft = (SYNC_COOLDOWN_MS / 1000L).toInt()
            while (secondsLeft > 0) {
                _syncState.value = _syncState.value.copy(cooldownSecondsLeft = secondsLeft)
                kotlinx.coroutines.delay(1_000)
                secondsLeft--
            }
            _syncState.value = _syncState.value.copy(cooldownSecondsLeft = 0)
        }
    }

    fun selectMood(mode: MoodMode) {
        update { it.copy(moodHistory = it.moodHistory + (Dates.todayKey() to mode)) }
        refreshCompanionUnlocks()
    }

    fun toggleHabitToday(habitId: String) {
        update { data ->
            val today = Dates.todayKey()
            val days = data.checkIns[habitId].orEmpty().toMutableSet()
            if (!days.add(today)) days.remove(today)
            data.copy(checkIns = data.checkIns + (habitId to days.toList()))
        }
        refreshCompanionUnlocks()
    }

    /** Free tier caps at [MAX_FREE_HABITS] habits; Premium removes the cap. */
    fun canAddHabit(): Boolean =
        _state.value.settings.hasFeatureAccess() || _state.value.habits.size < MAX_FREE_HABITS

    fun addHabit(name: String): Boolean {
        val trimmed = name.trim().take(MAX_HABIT_NAME_LENGTH)
        if (trimmed.isEmpty()) return false
        update { data ->
            data.copy(
                habits = data.habits + Habit(
                    id = UUID.randomUUID().toString(),
                    name = trimmed,
                    createdAt = System.currentTimeMillis(),
                )
            )
        }
        queueSync()
        return true
    }

    fun renameHabit(habitId: String, name: String) {
        val trimmed = name.trim().take(MAX_HABIT_NAME_LENGTH)
        if (trimmed.isEmpty()) return
        val unchanged = _state.value.habits.firstOrNull { it.id == habitId }?.name == trimmed
        if (unchanged) return
        update { data ->
            data.copy(habits = data.habits.map { if (it.id == habitId) it.copy(name = trimmed) else it })
        }
        queueSync()
    }

    fun togglePinned(habitId: String) {
        update { data ->
            data.copy(
                habits = data.habits.map {
                    if (it.id == habitId) it.copy(isPinned = !it.isPinned) else it
                }
            )
        }
    }

    fun deleteHabit(habitId: String) {
        update { data ->
            data.copy(
                habits = data.habits.filterNot { it.id == habitId },
                checkIns = data.checkIns - habitId,
            )
        }
        queueSync()
    }

    fun restoreHabit(habit: Habit, checkInDays: List<String>) {
        update { data ->
            if (data.habits.any { it.id == habit.id }) data
            else data.copy(
                habits = data.habits + habit,
                checkIns = if (checkInDays.isEmpty()) data.checkIns
                else data.checkIns + (habit.id to checkInDays),
            )
        }
        queueSync()
    }

    fun completeOnboarding(starterHabits: List<String>, mood: MoodMode?) {
        update { data ->
            val newHabits = starterHabits
                .map { it.trim().take(MAX_HABIT_NAME_LENGTH) }
                .filter { it.isNotEmpty() }
                .distinct()
                .map { Habit(UUID.randomUUID().toString(), it, System.currentTimeMillis()) }
            data.copy(
                habits = (data.habits + newHabits).distinctBy { it.name },
                moodHistory = if (mood != null) {
                    data.moodHistory + (Dates.todayKey() to mood)
                } else data.moodHistory,
                settings = data.settings.copy(onboardingDone = true),
            )
        }
        refreshCompanionUnlocks()
    }

    fun setThemeMode(mode: ThemeMode) {
        update { it.copy(settings = it.settings.copy(themeMode = mode)) }
    }

    fun setReducedMotion(enabled: Boolean) {
        update { it.copy(settings = it.settings.copy(reducedMotion = enabled)) }
    }

    fun setPresetTime(presetId: String, minutes: Int) {
        update { data ->
            data.copy(
                settings = data.settings.copy(
                    presetTimes = data.settings.presetTimes + (presetId to minutes)
                )
            )
        }
    }

    fun setNotificationMinutes(minutes: Int) {
        update { it.copy(settings = it.settings.copy(notificationMinutes = minutes)) }
        scheduleNotification(minutes)
    }

    fun scheduleNotification(minutes: Int = _state.value.settings.notificationMinutes) {
        if (hasNotificationPermission()) {
            notificationScheduler.scheduleDailyReminder(minutes)
            notificationScheduler.scheduleEveningReflection()
        }
        syncStreakAlertAlarm()
        syncWeeklyRecapAlarm()
    }

    fun setStreakAlertEnabled(enabled: Boolean) {
        update { it.copy(settings = it.settings.copy(streakAlertEnabled = enabled)) }
        syncStreakAlertAlarm()
    }

    fun setStreakAlertMinutes(minutes: Int) {
        update { it.copy(settings = it.settings.copy(streakAlertMinutes = minutes)) }
        syncStreakAlertAlarm()
    }

    private fun syncStreakAlertAlarm() {
        val settings = _state.value.settings
        if (settings.streakAlertEnabled && hasNotificationPermission()) {
            notificationScheduler.scheduleStreakAlert(settings.streakAlertMinutes)
        } else {
            notificationScheduler.cancelStreakAlert()
        }
    }

    fun setWeeklyRecapEnabled(enabled: Boolean) {
        update { it.copy(settings = it.settings.copy(weeklyRecapEnabled = enabled)) }
        syncWeeklyRecapAlarm()
    }

    private fun syncWeeklyRecapAlarm() {
        if (_state.value.settings.weeklyRecapEnabled && hasNotificationPermission()) {
            notificationScheduler.scheduleWeeklyRecap()
        } else {
            notificationScheduler.cancelWeeklyRecap()
        }
    }

    fun sendReminderPreview(): Boolean {
        if (!hasNotificationPermission()) return false
        CheckInNotifier.show(getApplication(), preview = true)
        return true
    }

    fun sendStreakAlertPreview(): Boolean {
        if (!hasNotificationPermission()) return false
        StreakAlertNotifier.showIfStreakAtRisk(getApplication(), preview = true)
        return true
    }

    fun sendWeeklyRecapPreview(): Boolean {
        if (!hasNotificationPermission()) return false
        WeeklyRecapNotifier.showRecap(getApplication(), preview = true)
        return true
    }

    private fun hasNotificationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

    fun setAccentPack(pack: String) {
        update { it.copy(settings = it.settings.copy(accentPack = pack)) }
    }

    fun setLanguage(language: com.rork.mindsetframestracker.data.AppLanguage) {
        update { it.copy(settings = it.settings.copy(language = language)) }
        // Language choice is part of the synced settings payload — back it up.
        queueSync()
    }

    fun setAvatar(avatar: com.rork.mindsetframestracker.data.AvatarConfig) {
        update { it.copy(settings = it.settings.copy(avatar = avatar)) }
    }

    fun saveReflection(text: String) {
        val key = Dates.todayKey()
        val line = text.trim().take(160)
        update { data ->
            data.copy(
                reflections = if (line.isEmpty()) data.reflections - key
                else data.reflections + (key to line),
            )
        }
        refreshCompanionUnlocks()
    }

    fun markReviewPromptShown() {
        // No-op — in-app review removed (Google Play Review API not available on HMS).
    }

    fun shouldShowReviewPrompt(streak: Int): Boolean = false

    fun awardBadge(tier: BadgeTier) {
        if (_state.value.settings.earnedBadges.contains(tier)) return
        update { data ->
            data.copy(settings = data.settings.copy(earnedBadges = data.settings.earnedBadges + tier))
        }
        refreshCompanionUnlocks()
    }
}
