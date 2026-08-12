package com.rork.mindsetframestracker.data

import kotlinx.serialization.Serializable

/**
 * Supported app languages. English (US) is the free default; English (UK) and
 * Tagalog / Taglish are also free. All other languages are Premium-exclusive
 * features gated behind the language selector in Settings.
 *
 * [displayName] is the native name shown in the UI; [englishName] is used for
 * A-Z sorting in the picker; [flagEmoji] is a regional indicator pair for
 * visual identification.
 *
 * Every language ships a full `assets/strings/{code}.json` UI translation.
 * Mood copy and starter habits live in `assets/quotes/quotes_{code}.json`;
 * any missing key or file automatically falls back to English.
 */
@Serializable
enum class AppLanguage(
    val code: String,
    val displayName: String,
    val englishName: String,
    val flagEmoji: String,
) {
    ENGLISH_US("en", "English (US)", "English (US)", "\uD83C\uDDFA\uD83C\uDDF8"),
    ENGLISH("en", "English (UK)", "English (UK)", "\uD83C\uDDEC\uD83C\uDDE7"),
    ARABIC("ar", "العربية", "Arabic", "\uD83C\uDDF8\uD83C\uDDE6"),
    BENGALI("bn", "বাংলা", "Bengali", "\uD83C\uDDE7\uD83C\uDDE9"),
    CHINESE("zh", "中文", "Chinese", "\uD83C\uDDE8\uD83C\uDDF3"),
    DUTCH("nl", "Nederlands", "Dutch", "\uD83C\uDDF3\uD83C\uDDF1"),
    FRENCH("fr", "Français", "French", "\uD83C\uDDEB\uD83C\uDDF7"),
    GERMAN("de", "Deutsch", "German", "\uD83C\uDDE9\uD83C\uDDEA"),
    GREEK("el", "Ελληνικά", "Greek", "\uD83C\uDDEC\uD83C\uDDF7"),
    HINDI("hi", "हिन्दी", "Hindi", "\uD83C\uDDEE\uD83C\uDDF3"),
    INDONESIAN("id", "Bahasa Indonesia", "Indonesian", "\uD83C\uDDEE\uD83C\uDDE9"),
    ITALIAN("it", "Italiano", "Italian", "\uD83C\uDDEE\uD83C\uDDF9"),
    JAPANESE("ja", "日本語", "Japanese", "\uD83C\uDDEF\uD83C\uDDF5"),
    KOREAN("ko", "한국어", "Korean", "\uD83C\uDDF0\uD83C\uDDF7"),
    MALAY("ms", "Bahasa Melayu", "Malay", "\uD83C\uDDF2\uD83C\uDDFE"),
    NORWEGIAN("no", "Norsk", "Norwegian", "\uD83C\uDDF3\uD83C\uDDF4"),
    POLISH("pl", "Polski", "Polish", "\uD83C\uDDF5\uD83C\uDDF1"),
    PORTUGUESE("pt", "Português", "Portuguese", "\uD83C\uDDE7\uD83C\uDDF7"),
    RUSSIAN("ru", "Русский", "Russian", "\uD83C\uDDF7\uD83C\uDDFA"),
    SPANISH("es", "Español", "Spanish", "\uD83C\uDDEA\uD83C\uDDF8"),
    SWEDISH("sv", "Svenska", "Swedish", "\uD83C\uDDF8\uD83C\uDDEA"),
    TAGALOG("tl", "Tagalog / Taglish", "Tagalog (Filipino)", "\uD83C\uDDF5\uD83C\uDDED"),
    THAI("th", "ภาษาไทย", "Thai", "\uD83C\uDDF9\uD83C\uDDED"),
    TURKISH("tr", "Türkçe", "Turkish", "\uD83C\uDDF9\uD83C\uDDF7"),
    UKRAINIAN("uk", "Українська", "Ukrainian", "\uD83C\uDDFA\uD83C\uDDE6"),
    URDU("ur", "اردو", "Urdu", "\uD83C\uDDF5\uD83C\uDDF0"),
    VIETNAMESE("vi", "Tiếng Việt", "Vietnamese", "\uD83C\uDDFB\uD83C\uDDF3"),
}

/** Default language for free-tier users and fresh installs. */
val DEFAULT_LANGUAGE: AppLanguage = AppLanguage.ENGLISH_US

/**
 * Languages available on the free tier. Everything else is Premium-locked.
 * English (US) is the default; English (UK) and Tagalog / Taglish are free
 * bonus languages.
 */
val freeLanguages: Set<AppLanguage> = setOf(
    AppLanguage.ENGLISH_US,
    AppLanguage.ENGLISH,
    AppLanguage.TAGALOG,
)

/** True when this language is available without Premium. */
val AppLanguage.isFreeLanguage: Boolean get() = this in freeLanguages

/**
 * Picker ordering: the free languages pinned first (US default, then UK, then
 * Tagalog / Taglish), everything else alphabetical A-Z by English name.
 */
val languagePickerOrder: List<AppLanguage> =
    listOf(AppLanguage.ENGLISH_US, AppLanguage.ENGLISH, AppLanguage.TAGALOG) +
        AppLanguage.entries
            .filter { it !in freeLanguages }
            .sortedBy { it.englishName }
