package com.jjrapps.sleepnoise.domain.model

/**
 * Everything the app remembers between sessions. Seven values, no database: there is
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
    val notificationRationaleShown: Boolean = false
) {
    companion object {
        const val LANGUAGE_AUTO = "auto"
    }
}
