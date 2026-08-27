package com.rork.mindsetframestracker.data

/**
 * Static, rule-based insight generator — no external API, no per-call cost.
 * Uses commonly cited general reference ranges (not medical diagnosis).
 * Every message ends by pointing to a professional for anything outside
 * typical range, same caution as before, without the API dependency.
 */
object RuleBasedInsight {

    fun forSteps(steps: Long): String = when {
        steps >= 10_000 -> "Great job — $steps steps is at or above the commonly cited daily target."
        steps >= 6_000 -> "$steps steps today — a solid amount of movement, a bit more gets you to the common 10,000 benchmark."
        steps > 0 -> "$steps steps logged. Any movement counts — try a short walk to build the habit."
        else -> "No steps recorded yet today."
    }

    fun forHeartRate(avgBpm: Int): String = when {
        avgBpm in 60..100 -> "Average resting heart rate of $avgBpm bpm falls within the commonly cited typical adult range (60-100 bpm)."
        avgBpm < 60 -> "$avgBpm bpm is below the commonly cited typical range — this can be normal for athletes, but if unexpected, worth mentioning to a doctor."
        else -> "$avgBpm bpm is above the commonly cited typical resting range (60-100 bpm). If this persists, checking with a healthcare professional is a good idea."
    }

    fun forSleep(minutes: Long): String {
        val hours = minutes / 60.0
        return when {
            hours >= 7 -> "%.1f hours of sleep — within the commonly recommended 7-9 hour range for adults.".format(hours)
            hours >= 5 -> "%.1f hours of sleep — a bit under the commonly recommended range. Worth prioritizing rest tonight.".format(hours)
            else -> "%.1f hours of sleep logged — noticeably below the typical recommended range.".format(hours)
        }
    }

    fun forStreak(currentStreak: Int, habitName: String): String = when {
        currentStreak >= 30 -> "$currentStreak days strong on \"$habitName\" — this is well into long-term habit territory."
        currentStreak >= 7 -> "$currentStreak day streak on \"$habitName\" — you're past the hardest first week."
        currentStreak >= 1 -> "$currentStreak day streak on \"$habitName\" — keep it going."
        else -> "Start today on \"$habitName\" — every streak begins at day one."
    }
}
