package com.jjrapps.sleepnoise.domain.model

/**
 * Everything the app remembers between sessions. Ten values, no database: there is
 * no history to keep, so DataStore is the whole persistence layer.
 */
data class PlaybackPreferences(
    val lastSound: NoiseType = NoiseType.Default,
    val volume: Int = 50,
    /** Minutes of the last timer used; zero means none. */
    val timerMinutes: Int = 60,
    val autoplayOnOpen: Boolean = true,
    /** `"auto"`, `"en"` or `"es"`. */
    val language: String = LANGUAGE_AUTO,
    val lastSeenChangelog: Int = 0,
    val notificationRationaleShown: Boolean = false,
    /**
     * Silence the phone while the noise plays (RF-22). On by default: an app whose
     * job is to stop noises waking you up has no business letting a notification do
     * it. It only takes effect once the user has granted Do Not Disturb access,
     * which no app can grant itself.
     */
    val doNotDisturbWhilePlaying: Boolean = true,
    /**
     * Whether the one-time explanation of that access has already been shown. Asked
     * once, at the first sound, and never again — see `DoNotDisturbSheet`.
     */
    val doNotDisturbAsked: Boolean = false,
    /**
     * Not a preference: a breadcrumb. True while the service is holding Do Not
     * Disturb, so that a process killed mid-session can hand it back on the next
     * start instead of leaving the phone silent for days.
     */
    val doNotDisturbHeld: Boolean = false
) {
    companion object {
        const val LANGUAGE_AUTO = "auto"
    }
}
